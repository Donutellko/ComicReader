package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.String.valueOf;

public class PageActivity extends AppCompatActivity {
    Comic current;
    private static Bitmap strip_placeholder;

    /** Activity отображения страницы комикса
     * В Intent передаётся название комикса, который нужно открыть: @see ComiclistActivity#openComic(String)
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        strip_placeholder = BitmapFactory.decodeResource(getResources(), R.mipmap.strip_placeholder);

        String name = getIntent().getStringExtra("Comic title");
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
        new AsyncPageFiller(findViewById(R.id.page), current).execute();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Отменяет автоматическое появление клавиатуры
    }

    void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /** Отвечает за обработку выбора элемента меню на данной активности
     * Возможные itemId (R.id.action_*):
     * * reload (обновить список), quit (выйти),
     * * day_night (переключение внешнего вида), filter (открыть панель с фильтрами для списка комиксов)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_quit: System.exit(0); break;
            case R.id.action_reload: //TODO
            case R.id.action_load_next: //TODO: Открытие окна с предложением сохранить следующие/предыдущие N страниц
            case R.id.action_day_night: //TODO
            case R.id.action_open: //TODO
            case R.id.action_goend: //TODO
            default: Log.e("Not working menu entry!", "Код для этого пункта меню ещё не написан");
        }
        return super.onOptionsItemSelected(item);
    }

    /** Заполняет полученный View информацией о текущей странице загружает туда Bitmap */
    static class AsyncPageFiller extends AsyncTask {
        View view;
        Comic comic;
        Comic.Page page;
        Bitmap bm;
        ImageView imageView;

        public AsyncPageFiller(View view, Comic comic) {
            this.view = view;
            this.comic = comic;
            page = comic.getPage();
            imageView = view.findViewById(R.id.page_image);
        }

        @Override
        protected void onPreExecute() {
            if (page == null) {
                page = new Comic.Page("Page is not found", "Page is not found", null, null, null);
            }

            ((TextView) view.findViewById(R.id.page_name)).setText(page.title);
            ((TextView) view.findViewById(R.id.page_desc)).setText(page.description);
            ((EditText) view.findViewById(R.id.page_number)).setText(valueOf(comic.curpage + 1));

            ((ProgressBar) view.findViewById(R.id.page_progress)).setProgress(comic.curpage);
            ((ProgressBar) view.findViewById(R.id.page_progress)).setMax(comic.getLength());
            imageView.setImageBitmap(strip_placeholder);

            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            bm = FileWorker.singleton.getImage(comic, comic.curpage);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (bm != null) {
                imageView.setImageBitmap(bm);
            }
        }
    }
}
