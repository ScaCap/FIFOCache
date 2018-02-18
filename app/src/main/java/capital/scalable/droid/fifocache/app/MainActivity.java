package capital.scalable.droid.fifocache.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

import capital.scalable.droid.fifocache.FIFOCache;

public class MainActivity extends AppCompatActivity {

    private static final String PDF_SAMPLE_FILE = "pdf_sample.pdf";
    @NonNull private FIFOCache cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.cache = new FIFOCache(this);

        final EditText editText = findViewById(R.id.cacheSizeInput);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        long size = Long.valueOf(editText.getText().toString());
                        cache.setSize(size);
                    } catch (NumberFormatException e) {}
                }
                return false;
            }
        });

        Button fileButton = findViewById(R.id.cacheFileButton);
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    InputStream inputStream = getAssets().open(PDF_SAMPLE_FILE);
                    long size = getAssets().openFd(PDF_SAMPLE_FILE).getLength();
                    cache.cache(inputStream, PDF_SAMPLE_FILE, size);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button clearCache = findViewById(R.id.clearCacheButton);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cache.clear();
            }
        });
    }
}
