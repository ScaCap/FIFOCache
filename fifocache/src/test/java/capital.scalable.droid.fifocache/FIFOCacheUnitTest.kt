package capital.scalable.droid.fifocache

import android.content.Context
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.InputStream

@RunWith(MockitoJUnitRunner.StrictStubs::class)
class FIFOCacheUnitTest {

    companion object {
        private val TEST_FILE = "TEST_FILE"
    }

    @Mock
    internal lateinit var context: Context

    @Mock
    internal lateinit var inputStream: InputStream

    private lateinit var cache: FIFOCache

    @Before
    fun setup() {
        cache = FIFOCache(context)
    }

    @Test
    fun `by default, subdirectory is DEFAULT_DIRECTORY`() {
        assertThat(cache.subdirectory, equalTo(FIFOCache.DEFAULT_DIRECTORY))
    }

    @Test
    fun `by default, size is DEFAULT_SIZE`() {
        assertThat(cache.size, `is`(FIFOCache.DEFAULT_SIZE))
    }

    @Test
    fun `set subdirectory to null throws an NullPointerException`() {
        try {
            cache.subdirectory = null
            Assert.fail("setSubdirectory with null directory did not throw an exception")
        } catch (e: NullPointerException) {
            assertThat(e.message, `is`("Provided path was null"))
        }
    }

    @Test
    fun `set size to 0 throws an IllegalArgumentException`() {
        try {
            cache.size = 0
            Assert.fail("set size to 0 did not throw an exception")
        } catch (e: NullPointerException) {
            assertThat(e.message, `is`("Cache size cannot be 0"))
        }
    }

    @Test
    fun `caching a file of negative size throws an IllegalArgumentException`() {
        try {
            cache.cache(inputStream, TEST_FILE, -1)
            Assert.fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message, `is`("The provided stream size is smaller than 0"))
        }
    }

    @Test
    fun `caching a file larger than the cache size throws an IllegalArgumentException`() {
        try {
            cache.cache(inputStream, TEST_FILE, cache.size + 1)
            Assert.fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertThat(e.message, `is`("The provided stream size is larger than the cache"))
        }
    }
}
