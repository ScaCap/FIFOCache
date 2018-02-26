# FIFOCache

A first-in first-out file cache that uses your app's internal cache directory. More details [here](https://engineering.scalable.capital/2018/02/20/fifocache-a-self-managing-cache-for-android.html).

## Usage

```
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //...
        FIFOCache cache = new FIFOCache(this);
		
        InputStream inputStream = getAssets().open("sample.pdf");
        long size = getAssets().openFd("sample.pdf").getLength();
        cache.cache(inputStream, "sample", size);
        //...
        File cachedFile = cache.retrieve("sample");
    }
}
```

## Installation

``` groovy
repositories {
    maven { url "https://jitpack.io" }
}


dependencies {
    implementation "com.github.ScaCap:FIFOCache:1.0.0"
}
```

## License

FIFOCache is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).
