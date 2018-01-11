package xyz.camelteam.comicreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ComicActivity extends AppCompatActivity {

    Comic current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic);

        SharedPreferences sp = getSharedPreferences("Comics", MODE_PRIVATE);

        String shortName = getIntent().getStringExtra("Comic title");
        current = DataWorker.findComic(DataWorker.loadComicsList(sp), shortName);
        current.pages = DataWorker.getPages(sp, shortName);


        DataWorker.savePages(sp, current);

        ((TextView) findViewById(R.id.comic_name)).setText(current.name);
        ((TextView) findViewById(R.id.comic_description)).setText(current.description);
        ((TextView) findViewById(R.id.comic_curpage)).setText(current.getLength() == 0 ?
                "Нет доступных страниц. Попробуйте обновить их список."
                : (current.curpage == 0 ? "Всего " + current.getLength() + " страниц." : "Страница " + current.curpage + " из " + current.getLength()));

        findViewById(R.id.comic_button_open).setOnClickListener(v -> openComic(current.shortName));
        findViewById(R.id.comic_button_refresh).setOnClickListener(v -> DataWorker.savePages(sp, current));
        findViewById(R.id.comic_button_load_images).setOnClickListener(v -> DataWorker.saveAllImages(current));
    }


    /** Открывает PageActivity
     * помещает в Intent название комикса, который требуется открыть.
     * @see PageActivity#onCreate(Bundle)
     * @param name Название комикса (желательно краткое, но полное тоже работает) */
    private void openComic(String name) {
        Intent intent = new Intent(ComicActivity.this, PageActivity.class);
        intent.putExtra("Comic title", name);
        startActivity(intent);
    }
}
