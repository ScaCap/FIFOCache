package capital.scalable.droid.fifocache;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
public class FIFOCacheTest {

    private static final String TEST_FILE = "file.test";
    private static final String TEST_FILE2 = "file2.test";
    private static final int TEST_FILE_SIZE = 4;  //The file is 4 bytes long

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private InputStream inputStream;
    private FIFOCache cache;

    @Before
    public void setUp() throws Exception {
        inputStream = InstrumentationRegistry.getContext()
                .getResources()
                .getAssets()
                .open(TEST_FILE);
        cache = new FIFOCache(new FIFOCacheMockContext(folder));
    }

    @After
    public void tearDown() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Test
    public void setDirectoryOldDirectoryNonEmpty() throws IOException {
        cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);

        try {
            cache.setSubdirectory("newDir");
            Assert.fail("Exception was not thrown when changing the directory");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Current cache directory is not empty"));
        }
    }

    @Test
    public void cacheAndRetrieve() throws Exception {
        File fileAtCache = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        File fileAtRetrieve = cache.retrieve(TEST_FILE);
        assertThat(fileAtRetrieve, equalTo(fileAtCache));
    }

    @Test
    public void sameNameSameUris() throws Exception {
        File file1 = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        File file2 = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        assertThat(file1, equalTo(file2));
    }

    @Test
    public void differentNamesDifferentUris() throws Exception {
        File file1 = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        File file2 = cache.cache(inputStream, TEST_FILE2, TEST_FILE_SIZE);
        assertThat(file1, is(not(equalTo(file2))));
    }

    @Test
    public void clearCacheTestOverflow() throws Exception {
        //We will cache two files, overflowing the cache,
        //and causing it to delete the first cached file.

        cache.setSize(TEST_FILE_SIZE);
        File file1 = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        assertThat(file1, is(notNullValue()));

        File file2 = cache.cache(inputStream, TEST_FILE2, TEST_FILE_SIZE);
        assertThat(file2, is(notNullValue()));

        //At this point, the cache should have cleared the first file
        //Retrieving it should return null
        assertThat(cache.retrieve(TEST_FILE), is(nullValue()));
    }

    @Test
    public void testClearCache() throws Exception {
        File file = cache.cache(inputStream, TEST_FILE, TEST_FILE_SIZE);
        assertThat(file, is(notNullValue()));
        cache.clear();
        assertThat(file.exists(), is(false));
    }
}