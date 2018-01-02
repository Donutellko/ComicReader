package xyz.camelteam.comicreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

public class PageActivity extends AppCompatActivity {
    Comic current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String name = getIntent().getStringExtra("Comic name");
        current = DataWorker.getComic(getApplicationContext(), name);

        setContentView(R.layout.activity_page);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_page, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
