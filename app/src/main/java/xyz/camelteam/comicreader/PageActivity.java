package xyz.camelteam.comicreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.constraint.solver.widgets.ConstraintWidget;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
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
        setContentView(R.layout.activity_page);

        String name = getIntent().getStringExtra("Comic name");
        current = DataWorker.getComic(getApplicationContext(), name);
        DataWorker.update(current);
        updatePage();

        findViewById(R.id.page_next).setOnClickListener(v -> {
            if (current.curpage < current.pages.length - 1) { current.curpage++; updatePage(); }
        });

        findViewById(R.id.page_prev).setOnClickListener(v -> {
            if (current.curpage > 0) { current.curpage--; updatePage(); }
        });

        ((EditText) findViewById(R.id.page_number)).setOnEditorActionListener((v, actionId, event) -> {
            if (EditorInfo.IME_ACTION_GO <= actionId && actionId <= EditorInfo.IME_ACTION_DONE) {
                String text = v.getText().toString();
                if (text.length() == 0) return false;
                int val = -1 + Integer.parseInt(text); if (val > 0 && val < current.pages.length) current.curpage = val; updatePage();
                return true;
            }
            return false;
        });
    }

    void updatePage() {
        new AsyncPageFiller(findViewById(R.id.page), current, getApplicationContext()).execute();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Отключает автоматическое появление клавиатуры
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
        Context context;
        View view;
        Comic comic;
        Comic.Page page;
        Bitmap bm;

        public AsyncPageFiller(View view, Comic comic, Context context) {
            this.context = context;
            this.view = view;
            this.comic = comic;
            page = comic.getPage();
        }

        @Override
        protected void onPreExecute() {
            ((TextView) view.findViewById(R.id.page_name)).setText(page.name);
            ((TextView) view.findViewById(R.id.page_desc)).setText(page.description);
            ((EditText) view.findViewById(R.id.page_number)).setText(valueOf(comic.curpage + 1));

            ((ProgressBar) view.findViewById(R.id.page_progress)).setProgress(comic.curpage);
            ((ProgressBar) view.findViewById(R.id.page_progress)).setMax(comic.pages.length - 1);
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String path = page.image_path = context.getExternalFilesDir("strip").getAbsolutePath() + "/" + comic.shortName + "/" + page.name;
            Log.i("Loading image", "for " + page.name + ": " + path);

            bm = DataWorker.getImage(path); // Пробуем получить из локального хранилища
            if (bm == null) { // если нет локально, пробуем скачать из инета:
                HttpWorker.saveImage(page.image_link, path);
                bm = DataWorker.getImage(path);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            ((ImageView) view.findViewById(R.id.page_image)).setImageBitmap(bm);
            super.onPostExecute(o);
        }
    }
}
