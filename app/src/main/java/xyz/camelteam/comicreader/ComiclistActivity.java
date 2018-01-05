package xyz.camelteam.comicreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/** Активность общего списка комиксов
 * Использует layout activity_comiclist
 * При запуске получает сохранённый список комиксов и инициирует фоновое обновление информации о них с сервера
 * Использует кастомный адаптер для заполнения списка
 */
public class ComiclistActivity extends AppCompatActivity {
    private static Bitmap logo_placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comiclist);
        ListView listView = findViewById(R.id.comic_list);

        Comic[] comics = DataWorker.getComicsList(getApplicationContext());

        ComiclistAdapter adapter = new ComiclistAdapter(comics);
        listView.setAdapter(adapter);

        logo_placeholder = BitmapFactory.decodeResource(getResources(), R.mipmap.logo_placeholder);
        listView.setOnItemClickListener((parent, view, position, id) -> openComic(comics[position].shortName));
        listView.setOnLongClickListener(v -> {
            //TODO: Открывать меню с предложением: удалить комикс из памяти, загрузить в память полностью, отметить как избранное
            return false;
        });
    }

    /** Открывает PageActivity
     * помещает в Intent название комикса, который требуется открыть.
     * @see PageActivity#onCreate(Bundle)
     * @param name Название комикса (желательно краткое, но полное тоже работает) */
    private void openComic(String name) {
        Intent intent = new Intent(ComiclistActivity.this, PageActivity.class);
        intent.putExtra("Comic name", name);
        startActivity(intent);
    }

    /** Задаёт res/menu/menu_comiclist в качестве меню
     * @see this#onOptionsItemSelected(MenuItem)
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comiclist, menu);
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
            case R.id.action_day_night: //TODO
            case R.id.action_filter: //TODO
            default: Log.e("Not working menu entry!", "Код для этого пункта меню ещё не написан");
        }
        return super.onOptionsItemSelected(item);
    }

    /** Используется для создания списка из комиксов
     * Использует Context для подгрузки изображений
     * Об использовании здесь: http://developer.alexanderklimov.ru/android/theory/adapters.php
     */
    private class ComiclistAdapter extends BaseAdapter {
        Comic[] comics;

        public ComiclistAdapter(Comic[] comics) {
            this.comics = comics;
        }

        public ComiclistAdapter(Comic[] comics, int[] filter) {
            List<Comic> tmp = new ArrayList<>(comics.length);
            for (Comic comic : comics) {
                if (comic.matchesFilter(filter))
                    tmp.add(comic);
            }
            this.comics = (Comic[]) tmp.toArray();
        }

        /** Возвращает View для элемента списка комиксов
         * Используется layout comiclist_item, в котором имеется ImageView для иконки, которая подгружается асинхронно,
         * поле для имени (с языком в скобках), описания и номера текущей страницы и общего числа страниц
         * @param position Номер заполняемой позиции в списке
         * @param convertView View, который нужно изменять и вернуть
         */
        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) convertView = getLayoutInflater().inflate(R.layout.comiclist_item, container, false);

            ImageView icon = convertView.findViewById(R.id.comic_icon);
            icon.setImageBitmap(logo_placeholder);

            Comic c = (Comic) getItem(position);

            String name = "[" + c.lang + "] " + c.name;
            if (name.length() > 35)
                name = name.substring(0, 32).trim() + "...";

            String desc = c.description;
            if (c.pages != null) desc = c.curpage + "/" + c.pages.length + "; " + desc;
            if (name.length() > 140)
                name = name.substring(0, 137).trim() + "...";

            ((TextView) convertView.findViewById(R.id.comic_name)).setText(name);
            ((TextView) convertView.findViewById(R.id.comic_info)).setText(desc);

            new AsyncLogoSetter(icon, c, getApplicationContext()).execute();

            return convertView;
        }

        @Override
        public int getCount() {
            return comics.length;
        }

        @Override
        public Object getItem(int position) {
            return comics[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    /** Загружает из памяти (сохраняет из интернета, если отсутствует) и подставляет логотип комикса в переданный ImageView */
    static class AsyncLogoSetter extends AsyncTask {
        Context context;
        ImageView imageView;
        Comic comic;
        Bitmap bm;

        /**
         * @param imageView ImageView, в который нужно поместить загруженный логотип
         * @param comic Объект комикса, для которого это нужно */
        public AsyncLogoSetter(ImageView imageView, Comic comic, Context context) {
            this.imageView = imageView;
            this.comic = comic;
            this.context = context;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String filename = comic.shortName + ".png";
            String path = context.getExternalFilesDir("logo").getAbsolutePath() + "/" + filename;
            Log.i("Loading image", "for " + comic.shortName + ": " + path);

            bm = DataWorker.getImage(path); // Пробуем получить из локального хранилища
            if (bm == null) { // если нет локально, пробуем скачать из инета:
                HttpWorker.saveImage(comic.logoUrl, path);
                bm = DataWorker.getImage(path);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (bm != null)
                imageView.setImageBitmap(bm);
            super.onPostExecute(o);
        }
    }
}
