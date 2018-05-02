package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import uk.co.senab.photoview.PhotoView;
import xyz.camelteam.comicreader.data.ComicDBHelper;

import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.DOWN;
import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.LEFT;
import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.RIGHT;
import static xyz.camelteam.comicreader.PageActivity.SwipeListener.Swipe.UP;

public class PageActivity extends AppCompatActivity {
    Comic comic;
    int pagesCount = 0;
    Page[] pages;
    AsyncPageFiller currentTask;
    boolean actionBarShown = true;

    /**
     * Activity отображения страницы комикса
     * В Intent передаётся название комикса, который нужно открыть: @see ComiclistActivity#openComic(String)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page);

        // int minH = (int) (0.8 * getWindowManager().getDefaultDisplay().getHeight());
        // findViewById(R.id.page_image).setMinimumHeight(minH);

        PhotoView photoView = findViewById(R.id.page_image);
        // При клике на PhotoView скрывать или показывать ActionBar
        photoView.setOnViewTapListener((view, x, y) -> toggleActionBar());

        int comicId = getIntent().getIntExtra("Comic id", -1);
        comic = ComicDBHelper.singletone.getComic(comicId);

        pages = ComicDBHelper.singletone.getAllPages(comicId);
        Log.i("PageActivity", "Got " + pages.length + " pages from DB");

        updatePage();

        setListeners();
    }

    private void setListeners() {

        findViewById(R.id.page_next).setOnClickListener(v -> nextPage()); // Переворот страницы по кнопке
        findViewById(R.id.page_prev).setOnClickListener(v -> prevPage()); // Переворот страницы по кнопке

        // Переход к нужной странице по номеру
        /*((EditText) findViewById(R.id.page_number)).setOnEditorActionListener((v, actionId, event) -> {
            if (EditorInfo.IME_ACTION_GO <= actionId && actionId <= EditorInfo.IME_ACTION_DONE) {
                String text = v.getText().toString();
                if (text.length() == 0)
                    return false;
                int val = -1 + Integer.parseInt(text);
                if (val >= 0 && val < pages.length) {
                    comic.curpage = val;
                    updatePage();
                }
                return true;
            }
            return false;
        });*/

        // Переключение страниц свайпом:
//      findViewById(R.id.page_content).setOnTouchListener(
        findViewById(R.id.page).setOnTouchListener(
                new SwipeListener(Math.min(getWindowManager().getDefaultDisplay().getWidth(),
                        getWindowManager().getDefaultDisplay().getHeight()) / 4) { // свайп на четверть ширины
                    @Override
                    void onSwipe(Swipe dir) {
                        if (dir == SwipeListener.Swipe.LEFT) prevPage();
                        else if (dir == SwipeListener.Swipe.RIGHT) nextPage();
                    }
                });
    }

    void toggleActionBar() {
        ActionBar bar = getSupportActionBar();
        assert bar != null;

        if (actionBarShown) bar.hide();
        else bar.show();

        actionBarShown = !actionBarShown;
    }

    void nextPage() {
        if (comic.curpage < pages.length - 2) {
            comic.curpage++;
            updatePage();
        }
    }

    void prevPage() {
        if (comic.curpage > 0) {
            comic.curpage--;
            updatePage();
        }
    }

    void updatePage() {
        Log.i("PageActivity", "Updating page: " + comic.curpage);

        if (comic.curpage != -1)
            updateSavedCurpage();

        // завершаем предыдущий процесс наполнения страницы:
        if (currentTask != null) currentTask.cancel(true);
        new AsyncPageFiller(findViewById(R.id.page), comic, pages).execute();
        // TODO: показывать прогресс загрузки изображения (например так: https://toster.ru/q/327193)
        // Отменяет появление клавиатуры
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

//        ProgressBar pb = findViewById(R.id.page_progress);
        SeekBar pb = findViewById(R.id.page_seek);
        pb.setMax(pages.length);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pb.setProgress(comic.curpage, true);
        } else {
            pb.setProgress(comic.curpage);
        }

//        PhotoView photoView = findViewById(R.id.page_image);
//        String s = FileWorker.singleton.getPath(comic, pages[0]);
//        photoView.setImageDrawable(new BitmapDrawable(s));
    }

    /**
     * Записывает в БД номер текущей страницы комикса
     */
    public void updateSavedCurpage() {
        ComicDBHelper.singletone.updateCurpage(comic.getId(), comic.curpage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Отвечает за обработку выбора элемента меню на данной активности
     * Возможные itemId (R.id.action_*):
     * * reload (обновить список), quit (выйти),
     * * day_night (переключение внешнего вида), filter (открыть панель с фильтрами для списка комиксов)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_quit:
                System.exit(0);
                break;
            case R.id.action_reload: //TODO
                //TODO: Открытие окна с предложением сохранить следующие/предыдущие N страниц
            case R.id.action_load_next:
            case R.id.action_day_night: //TODO
            case R.id.action_open: //TODO
            case R.id.action_goend:
                comic.curpage = pagesCount - 1;
                updatePage();
                break;
            default:
                Log.d("Not working menu entry!", "Код для этого пункта меню не написан");
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Заполняет полученный View информацией о текущей странице загружает туда Bitmap
     * и скачивает изображения для соседних страниц
     */
    static class AsyncPageFiller extends AsyncTask<Void, Void, Void> {
        protected WeakReference<View> viewWeakRef; // WeakReference не препятствует удалению объекта
        protected WeakReference<PhotoView> photoWeakRef;
        protected Comic comic;
        protected Page[] pages;
        protected Bitmap bm;

        public AsyncPageFiller(View view, Comic comic, Page[] pages) {
            this.viewWeakRef = new WeakReference<>(view);
            this.comic = comic;
            this.pages = pages;
            photoWeakRef = new WeakReference<>(view.findViewById(R.id.page_image));
        }

        @Override
        protected void onPreExecute() {
            Page p;
            if (pages == null || pages.length < comic.curpage || pages[comic.curpage] == null)
                p = Page.getFiller();
            else
                p = pages[comic.curpage];

            // TODO: заменить изображение на ProgressBar

            View view = this.viewWeakRef.get();

            ((TextView) view.findViewById(R.id.page_title)).setText(p.title);
            ((TextView) view.findViewById(R.id.page_desc)).setText(p.description);
//            ((EditText) view.findViewById(R.id.page_number)).setText(valueOf(comic.curpage + 1));

            // ((ProgressBar) view.findViewById(R.id.page_progress)).setProgress(comic.curpage);
            // ((ProgressBar) view.findViewById(R.id.page_progress)).setMax(comic.pagescount);
            ((SeekBar) view.findViewById(R.id.page_seek)).setProgress(comic.curpage);
            ((SeekBar) view.findViewById(R.id.page_seek)).setMax(comic.pagescount);
            photoWeakRef.get().setAlpha(0x33);

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int a = comic.curpage;
            if (a >= 0 && a < pages.length - 2)
                FileWorker.singleton.saveImage(comic, pages[a + 1]);
            if (a > 0)
                FileWorker.singleton.saveImage(comic, pages[a - 1]);
            bm = FileWorker.singleton.getImage(comic, pages[a]); // Дожидается выполнения
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (bm != null) {
                PhotoView photoView = photoWeakRef.get();
                View view = viewWeakRef.get();

                photoView.setImageBitmap(bm);
                // PhotoView должен быть в длину не меньше, чем:
                // размер картинки
                int min1 = bm.getHeight();
                // всю свободную часть высоты экрана с учётом размера описания и кнопок
                int min2 = (int) (0.80 * (view.getHeight()
                        - view.findViewById(R.id.page_desc).getHeight()
                        - ((ViewGroup.MarginLayoutParams) view.findViewById(R.id.page_desc).getLayoutParams()).bottomMargin
                        - view.findViewById(R.id.page_title).getHeight()
                ));
                // чтобы в ширину занимал 100%, не обрезаясь в длину
                int min3 = (int) (bm.getHeight() * ( (double) viewWeakRef.get().getWidth() / bm.getWidth()));

                photoView.setMinimumHeight(Math.max(Math.max(min1, min2), min3));
                photoView.setAlpha(0xFF);
            }
        }
    }

    abstract static class SwipeListener implements View.OnTouchListener {
        private float MIN_SWIPE; // 200
        private float startX = -1, startY = -1;

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

        enum Swipe {LEFT, RIGHT, UP, DOWN}
    }
}
