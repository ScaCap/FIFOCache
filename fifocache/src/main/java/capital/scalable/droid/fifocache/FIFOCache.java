package capital.scalable.droid.fifocache;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Manages the files inside a managed subfolder in the cache folder of an app,
 * as defined by {@link android.content.Context#getCacheDir()}
 * Automatically clears any files that are larger than the cache size.
 * This class does not support concurrent read/write operations.
 */
public class FIFOCache {

    /**
     * Default value for the size of the cache
     */
    public static final long DEFAULT_SIZE = 5242880L; //5MB

    /**
     * Default location for the managed cache.
     */
    public static final String DEFAULT_DIRECTORY = "managed";

    private long size = DEFAULT_SIZE;
    private String subdirectory = DEFAULT_DIRECTORY;

    @NonNull private final Context context;
    @NonNull private final Comparator<? super File> mostRecentComparator = new Comparator<File>() {
        @Override
        public int compare(File f1, File f2) {
            return Long.compare(f1.lastModified(), f2.lastModified());
        }
    };

    public FIFOCache(@NonNull Context context) {
        this.context = context;
    }

    public FIFOCache(@NonNull Context context, @NonNull String subdirectory) {
        this.context = context;
        setSubdirectory(subdirectory);
    }

    /**
     * Sets the relative path to the subfolder to be managed by FIFOCache.
     * If the provided path is an empty string, the whole cache folder is managed by this instance of FIFOCache
     * @throws NullPointerException if the provided path empty is null
     * @throws IllegalArgumentException if the provided cache directory is empty
     * @throws IllegalStateException if the currently managed cache directory is not empty
     */
    public void setSubdirectory(String path) {
        if (path == null) {
            throw new NullPointerException("Provided path was null");
        }

        File cacheDir = new File(context.getCacheDir(), getSubdirectory());
        long dirSize = getDirSize(cacheDir);
        if (dirSize > 0) {
            throw new IllegalStateException("Current cache directory is not empty");
        }
        subdirectory = path;
    }

    /**
     * Gets the relative path to the subfolder managed by FIFOCache
     * By default, this value is {@see FIFOCache.DEFAULT_DIRECTORY}
     * @return The relative path to the folder
     */
    public String getSubdirectory() {
        return subdirectory;
    }

    /**
     * Sets the size of the cache.
     * @param size The new size of the cache
     * @throws IllegalArgumentException if the provided cache size is 0.
     */
    public void setSize(long size) {
        if (size == 0) {
            throw new IllegalArgumentException("Cache size cannot be 0");
        }
        this.size = size;
    }

    /**
     * Gets the size of the cache.
     * By default, this value is {@see FIFOCache.DEFAULT_SIZE}
     * @return The size of the cache, in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Puts a Uri's content into the cache
     *
     * @param uri The Uri of the resource to cache. This is expected to be a Uri retrieved from a {@link android.content.ContentResolver}
     * @param name The name to associate with the cached data. This is the key used to retrieve the data
     * @param size The size of the inputStream, in bytes.
     * @return A File representing the cached file
     * @throws IOException if the provided Uri points to inaccessible content, or the cache folder cannot be opened.
     * @throws IllegalArgumentException if the provided content's size is 0, or is larger than the size of the cache.
     */
    public File cache(@NonNull Uri uri, String name, long size) throws IOException {
        return cache(context.getContentResolver().openInputStream(uri), name, size);
    }

    /**
     * Puts an InputStream into the cache
     *
     * @param inputStream The InputStream to cache
     * @param name The name to associate with the cached data. This is the key used to retrieve the data
     * @param streamSize The size of the inputStream, in bytes.
     * @return A File representing the cached file
     * @throws IOException if the provided inputStream is not accessible, or the cache folder cannot be opened.
     * @throws IllegalArgumentException if the provided inputStream's size is 0, or is larger than the size of the cache.
     */
    public File cache(@NonNull InputStream inputStream, @NonNull String name, long streamSize) throws IOException {
        if (streamSize < 0) {
            throw new IllegalArgumentException("The provided stream size is smaller than 0");
        }
        if (streamSize > size) {
            throw new IllegalArgumentException("The provided stream size is larger than the cache");
        }

        File managedCache = new File(context.getCacheDir(), getSubdirectory());
        if (!managedCache.exists()) {
            if (!managedCache.mkdir()) {
                throw new IOException("Cannot create managed cache folder");
            }
        }
        long dirSize = getDirSize(managedCache);
        long newSize = streamSize + dirSize;

        //Clear cache if too full
        if (newSize > getSize()) {
            cleanDir(managedCache, newSize - getSize());
        }

        File outputFile = new File(managedCache, name);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        copy(inputStream, outputStream);
        outputStream.close();

        return outputFile;
    }

    /**
     * Gets the File associated with a name from the cache
     *
     * @param name The key to retrieve the cached data.
     * @return The File associated with the given name, null if the file does not exist.
     */
    public @Nullable File retrieve(String name) {
        String parent = context.getCacheDir().getPath();
        if (!TextUtils.isEmpty(getSubdirectory())) {
            parent = parent + File.separator + getSubdirectory();
        }
        File managedCache = new File(parent, name);
        if (!managedCache.exists()) {
            // Data doesn't exist
            return null;
        }
        return managedCache;
    }

    /**
     * Clears all cached files in the currently managed subfolder
     */
    public void clear() {
        File managedCache = new File(context.getCacheDir(), getSubdirectory());
        if (managedCache.exists()) {
            cleanDir(managedCache, size);
        }
    }

    /**
     * Iterates through all the files in the app's cache,
     * deleting the least recent ones in terms of last date of modification, until there is enough room
     *
     * @param dir The directory to clean
     * @param bytes The number of bytes to clear
     */
    private void cleanDir(File dir, long bytes) {
        long bytesDeleted = 0;
        File[] files = dir.listFiles();

        //Oldest file first
        Arrays.sort(files, mostRecentComparator);

        //Delete until satisfied
        for (File file : files) {
            if (file.delete()) {
                bytesDeleted += file.length();
            }
            if (bytesDeleted >= bytes) {
                break;
            }
        }
    }

    /**
     * Iterates through all the files in the app's cache, and calculate its current size
     *
     * @param dir The Directory for which the size is calculated
     * @return The size of the directory, in bytes
     */
    private long getDirSize(File dir) {
        long size = 0;
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                }
            }
        }

        return size;
    }

    /**
     * Copies the inputStream into the outputStream
     * @param in The InputStream to copy from
     * @param out The OutputStream to copy to
     * @return the size of the copied data
     * @throws IOException if reading from the inputStream, and writing into the outputStream fails
     */
    private int copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int length;
        int total = 0;
        while ((length = in.read(buf)) > 0) {
            out.write(buf, 0, length);
            total += length;
        }
        return total;
    }
}
