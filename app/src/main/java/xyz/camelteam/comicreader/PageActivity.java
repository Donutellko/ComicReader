package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.String.valueOf;
import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.*;

public class PageActivity extends AppCompatActivity {
    Comic current;
    AsyncPageFiller currentTask;

    /** Activity отображения страницы комикса
     * В Intent передаётся название комикса, который нужно открыть: @see ComiclistActivity#openComic(String)
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        String name = getIntent().getStringExtra("Comic title");
        current = DataWorker.getComic(getApplicationContext(), name);
        DataWorker.update(current);
        updatePage();

        findViewById(R.id.page_next).setOnClickListener(v -> nextPage());

        findViewById(R.id.page_prev).setOnClickListener(v -> {
            prevPage();
        });

        ((EditText) findViewById(R.id.page_number)).setOnEditorActionListener((v, actionId, event) -> {
            if (EditorInfo.IME_ACTION_GO <= actionId && actionId <= EditorInfo.IME_ACTION_DONE) {
                String text = v.getText().toString();
                if (text.length() == 0) return false;
                int val = -1 + Integer.parseInt(text);
                if (val > 0 && val < current.pages.length) current.curpage = val;
                updatePage();
                return true;
            }
            return false;
        });

        findViewById(R.id.page_content).setOnTouchListener(
                new SwipeListener(getWindowManager().getDefaultDisplay().getWidth() / 4) { // свайп на четверть ширины
            @Override
            void onSwipe(Swipe dir) {
                if (dir == SwipeListener.Swipe.LEFT) prevPage();
                else if (dir == SwipeListener.Swipe.RIGHT) nextPage();
            }
        });
    }

    void nextPage() {
        if (current.curpage < current.getLength() - 1) { current.curpage++; updatePage(); }
    }

    void prevPage() {
        if (current.curpage > 0) { current.curpage--; updatePage(); }
    }

    void updatePage() {
        updateSavedCurpage();
        if (currentTask != null) currentTask.cancel(true); // завершаем предыдущий процесс наполнения страницы
        new AsyncPageFiller(findViewById(R.id.page), current).execute(); // TODO: показывать прогресс загрузки изображения (например так: https://toster.ru/q/327193)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Отменяет автоматическое появление клавиатуры
    }

    /** Записывает в sharedPreferences номер текущей страницы комикса */
    public void updateSavedCurpage() {
        DataWorker.updateComicPage(getApplicationContext(), current);
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
            case R.id.action_goend: current.curpage = current.getLength() - 1; updatePage(); break;
            default: Log.e("Not working menu entry!", "Код для этого пункта меню ещё не написан");
        }
        return super.onOptionsItemSelected(item);
    }

    /** Заполняет полученный View информацией о текущей странице загружает туда Bitmap и скачивает изображения для соседних страниц */
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
            //imageView.setImageBitmap(strip_placeholder);
            imageView.setAlpha(0x33);

            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            bm = FileWorker.singleton.getImage(comic, comic.curpage);
            if (comic.curpage > 0)
                FileWorker.singleton.saveImage(comic, comic.curpage - 1);
            if (comic.curpage < comic.getLength() - 1)
                FileWorker.singleton.saveImage(comic, comic.curpage + 1);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (bm != null) {
                imageView.setImageBitmap(bm);
                imageView.setAlpha(0xFF);
            }
        }
    }

    abstract static class SwipeListener implements View.OnTouchListener {
        private float MIN_SWIPE = 200;
        private float startX = -1, startY = -1;
        enum Swipe { LEFT, RIGHT, UP, DOWN }

        public SwipeListener(float MIN_SWIPE) {
            this.MIN_SWIPE = MIN_SWIPE;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    startY = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if (startX >= 0 && startY >= 0) {
                        float dx = event.getX() - startX, dy = event.getY() - startY;
                        float adx = Math.abs(dx), ady = Math.abs(dy);
                        if (adx > MIN_SWIPE || ady > MIN_SWIPE) {
                            if (adx > ady) onSwipe(dx > 0 ? LEFT : RIGHT);
                            else onSwipe(dy > 0 ? DOWN : UP);
                        }
                        startX = startY = -1;
                    }
                    break;
            }
            return false;
        }

        abstract void onSwipe(Swipe dir);

    }
}
