package capital.scalable.droid.fifocache;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class CacheManagerMockContext extends MockContext {

    private static final String TEST_CACHE_FOLDER = "cache";
    private final File cacheDir;
    private final ContentResolver contentResolver = new MockContentResolver();

    public CacheManagerMockContext(TemporaryFolder temporaryFolder) throws IOException {
        this.cacheDir = temporaryFolder.newFolder(TEST_CACHE_FOLDER);
    }

    /**
     * Instead of returning the target context's cache directory,
     * we return a file on a temporary folder
     */
    @Override
    public File getCacheDir() {
        return cacheDir;
    }

    /**
     * Instead of retuning an actual ContentResolver, return a mock one
     */
    @Override
    public ContentResolver getContentResolver() {
        return contentResolver;
    }
}
