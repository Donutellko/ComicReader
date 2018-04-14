package xyz.camelteam.comicreader;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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

import xyz.camelteam.comicreader.data.ComicDBHelper;

import static java.lang.String.valueOf;
import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.*;

public class PageActivity extends AppCompatActivity {
    Comic current;
    int pagesCount = 0;
    Page[] pages;
    AsyncPageFiller currentTask;

    /** Activity отображения страницы комикса
     * В Intent передаётся название комикса, который нужно открыть: @see ComiclistActivity#openComic(String)
     * */
    @Override
    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        int comicId = getIntent().getIntExtra("Comic id", -1);
        current = ComicDBHelper.singletone.getComic(comicId);

        Page curpage = ComicDBHelper.singletone.getPage(comicId, current.curpage); // Изначально берём только одну из БД

        DataWorker.updatePages();

        findViewById(R.id.page_next).setOnClickListener(v -> nextPage());
        findViewById(R.id.page_prev).setOnClickListener(v -> prevPage());

        // Переход к нужной странице по номеру
        ((EditText) findViewById(R.id.page_number)).setOnEditorActionListener((v, actionId, event) -> {
            if (EditorInfo.IME_ACTION_GO <= actionId && actionId <= EditorInfo.IME_ACTION_DONE) {
                String text = v.getText().toString();
                if (text.length() == 0) return false;
                int val = -1 + Integer.parseInt(text);
                pagesCount = ComicDBHelper.singletone.pagesCount(comicId);
                if (val >= 0 && val < pagesCount) current.curpage = val;
                updatePage();
                return true;
            }
            return false;
        });

        // Переключение страниц свайпом:
        findViewById(R.id.page_content).setOnTouchListener(
                new SwipeListener(Math.min(getWindowManager().getDefaultDisplay().getWidth(),
                        getWindowManager().getDefaultDisplay().getHeight()) / 4) { // свайп на четверть ширины
            @Override
            void onSwipe(Swipe dir) {
                if (dir == SwipeListener.Swipe.LEFT) prevPage();
                else if (dir == SwipeListener.Swipe.RIGHT) nextPage();
            }
        });
    }

    void nextPage() { if (current.curpage <  - 1) { current.curpage++; updatePage(); } }

    void prevPage() {
        if (current.curpage > 0) { current.curpage--; updatePage(); }
    }

    void updatePage() {
        if (current.curpage != -1)
            updateSavedCurpage();
        if (currentTask != null) currentTask.cancel(true); // завершаем предыдущий процесс наполнения страницы
        currentTask = new AsyncPageFiller(findViewById(R.id.page), current, pages);
        currentTask.execute(); // TODO: показывать прогресс загрузки изображения (например так: https://toster.ru/q/327193)
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); // Отменяет появление клавиатуры
    }

    /** Записывает в БД номер текущей страницы комикса */
    public void updateSavedCurpage() {
        ComicDBHelper.singletone.updateCurrentPage(current.curpage);
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
            case R.id.action_goend: current.curpage = pagesCount - 1; updatePage(); break;
            default: Log.d("Not working menu entry!", "Код для этого пункта меню ещё не написан");
        }
        return super.onOptionsItemSelected(item);
    }

    /** Заполняет полученный View информацией о текущей странице загружает туда Bitmap
     * и скачивает изображения для соседних страниц */
    static class AsyncPageFiller extends AsyncTask {
        View view;
        Comic comic;
        Page[] pages;
        Bitmap bm;
        ImageView imageView;

        public AsyncPageFiller(View view, Comic comic, Page[] pages) {
            this.view = view;
            this.comic = comic;
            this.pages = pages;
            imageView = view.findViewById(R.id.page_image);
        }

        @Override
        protected void onPreExecute() {
            Page p = pages[comic.curpage];
            if (p == null)
                p = Page.getFiller(); // TODO: заменить на полосу загрузки или крутилку

            ((TextView) view.findViewById(R.id.page_name)).setText(p.title);
            ((TextView) view.findViewById(R.id.page_desc)).setText(p.description);
            ((EditText) view.findViewById(R.id.page_number)).setText(valueOf(comic.curpage + 1));

            ((ProgressBar) view.findViewById(R.id.page_progress)).setProgress(comic.curpage);
            ((ProgressBar) view.findViewById(R.id.page_progress)).setMax(comic.pagescount);
            imageView.setAlpha(0x33);

            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            int a = comic.curpage;
            if (a < pages.length)
                FileWorker.singleton.saveImage(comic, pages[a + 1]); // Выполняется асинхронно
            if (a > 0)
                FileWorker.singleton.saveImage(comic, pages[a - 1]); // Выполняется асинхронно
            bm = FileWorker.singleton.getImage(comic, a); // Дожидается выполнения
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
