package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.String.valueOf;

public class PageActivity extends AppCompatActivity {
    Comic current;

    /** Activity отображения страницы комикса
     * В Intent передаётся название комикса, который нужно открыть: @see ComiclistActivity#openComic(String)
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Отключает автоматическое появление клавиатуры

        String name = getIntent().getStringExtra("Comic name");

        Log.i("Searching for comic: ", name);
        current = DataWorker.getComic(getApplicationContext(), name);
        DataWorker.update(current);

        if (current == null) {
            showToast("Unable to load comic. Try refreshing it.");
            return;
        }

        setContentView(R.layout.activity_page);

        new AsyncPageFiller(findViewById(R.id.page), current).execute();
    }


    void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Заполняет полученный View информацией о текущей странице загружает туда Bitmap */
    static class AsyncPageFiller extends AsyncTask {
        View view;
        Comic comic;
        Comic.Page page;
        Bitmap bm;

        public AsyncPageFiller(View view, Comic comic) {
            this.view = view;
            this.comic = comic;
            page = comic.getPage();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }

        @Override
        protected void onPreExecute() {
            ((TextView) view.findViewById(R.id.page_name)).setText(page.name);
            ((TextView) view.findViewById(R.id.page_desc)).setText(page.description);
            ((EditText) view.findViewById(R.id.page_number)).setText(valueOf(page.number));
            ((ProgressBar) view.findViewById(R.id.page_progress)).setProgress(comic.curpage);
            ((ProgressBar) view.findViewById(R.id.page_progress)).setMax(comic.pages.length);
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(Object o) {
            ((ImageView) view.findViewById(R.id.page_image)).setImageBitmap(bm);
            super.onPostExecute(o);
        }
    }
}
