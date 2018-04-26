package xyz.camelteam.comicreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import xyz.camelteam.comicreader.data.ComicDBHelper;

public class ComicActivity extends AppCompatActivity {
    static final String intentName = "Comic id";
    Comic current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic);

        int comicId = getIntent().getIntExtra(intentName, -1);

        current = ComicDBHelper.singletone.getComic(comicId);
        int pagescount = ComicDBHelper.singletone.getPagesCount(comicId);

        DataWorker.updatePages(current);

        ((TextView) findViewById(R.id.comic_name)).setText(current.title);
        ((TextView) findViewById(R.id.comic_description)).setText(current.description);
        ((TextView) findViewById(R.id.comic_curpage)).setText(pagescount == 0 ?
                "Нет доступных страниц. Попробуйте обновить их список."
                : (current.curpage == 0 ? "Всего " + pagescount + " страниц." :
                "Страница " + current.curpage + " из " + pagescount));

        findViewById(R.id.comic_button_open).setOnClickListener(v -> openComic(comicId));
        findViewById(R.id.comic_button_refresh).setOnClickListener(v -> DataWorker.savePages(current));
        findViewById(R.id.comic_button_load_images).setOnClickListener(v -> DataWorker.saveAllImages(current));
    }


    /** Открывает PageActivity
     * помещает в Intent название комикса, который требуется открыть.
     * @see PageActivity#onCreate(Bundle)
     * @param name Название комикса (желательно краткое, но полное тоже работает)  */
    private void openComic(int name) {
        Intent intent = new Intent(ComicActivity.this, PageActivity.class);
        intent.putExtra(intentName, name);
        startActivity(intent);
    }
}
