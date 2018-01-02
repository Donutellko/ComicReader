package xyz.camelteam.comicreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class ComiclistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comiclist);
        ListView listView = findViewById(R.id.comic_list);


        Comic[] comics = { // TODO: реальная подгрузка
                new Comic("Saturday Morning Breakfast Cereal", "page 237 out of 20!8", "", null),
                new Comic("XKCD", "page 276 out of 6543", "", null),
                new Comic("Freefall", "page 2065 out of 2066", "", null),
                new Comic("Gamercat", "page 271 out of 271", "", null),
        };

        for (Comic c : comics) {
            View item = View.inflate(this, R.layout.activity_comiclist, null);
            ((TextView) item.findViewById(R.id.name)).setText(c.name);
            ((TextView) item.findViewById(R.id.info)).setText(c.description);
            listView.addView(item);
        }

        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadComic((String) ((TextView) v.findViewById(R.id.name)).getText());
            }
        });
    }

    private void loadComic(String name) {
        // TODO:
    }
}
