package xyz.camelteam.comicreader;

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

    /**
     * При запуске приложение загружает из SharedPreferences список доступных комиксов,
     * пытается получить получить с сервера обновлённый список, загружает в память
     * (при отсутствии, из интернета) логотипы комиксов для отображения в списке.
     * Создаёт список, адаптер и логотип по умолчанию для него:
     * @see ComiclistAdapter
     * Устанавливает действия для элементов списка
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comiclist);
        ListView listView = findViewById(R.id.comic_list);

        new FileWorker(getApplicationContext());

        DataWorker.updateComicsList(getApplicationContext());

        Comic[] comics = DataWorker.getComicsList(getApplicationContext());

        Bitmap logo_placeholder = BitmapFactory.decodeResource(getResources(), R.mipmap.logo_placeholder);

        ComiclistAdapter adapter = new ComiclistAdapter(comics, logo_placeholder);
        new AsyncLogoGetter(comics, adapter).execute();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> openComic(comics[position].shortName));
        listView.setOnLongClickListener(v -> {
            Comic comic = comics[v.getId()];
            Comic.Page[] pages = comic.pages;
            if (pages != null && pages.length > 0) {
                for (int i = 0; i < pages.length; i++) { // сохраняет все страницы
                    FileWorker.singleton.saveImage(comic, i);
                }
            }
            //TODO: Открывать меню с предложением: удалить комикс из памяти, загрузить в память полностью, отметить как избранное
            return false;
        });
    }

    /** Открывает ComicActivity
     * помещает в Intent название комикса, который требуется открыть.
     * @see ComicActivity#onCreate(Bundle)
     * @param name Название комикса (желательно краткое, но полное тоже работает) */
    private void openComic(String name) {
        Intent intent = new Intent(ComiclistActivity.this, ComicActivity.class);
        intent.putExtra("Comic title", name);
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
        private final Bitmap logo_placeholder;
        Comic[] comics;

        public ComiclistAdapter(Comic[] comics, Bitmap logo_placeholder) {
            this.logo_placeholder = logo_placeholder;
            this.comics = comics;
        }

        public ComiclistAdapter(Comic[] comics, Bitmap logo_placeholder, int[] filter) {
            this.logo_placeholder = logo_placeholder;
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
            convertView = convertView != null ? convertView : getLayoutInflater().inflate(R.layout.comiclist_item, container, false);

            Comic c = (Comic) getItem(position);

            ImageView icon = convertView.findViewById(R.id.comic_icon);
            icon.setImageBitmap(c.logo != null ? c.logo : logo_placeholder);

            String name = "[" + c.lang + "] " + c.name;
            if (name.length() > 35)
                name = name.substring(0, 32).trim() + "...";

            String desc = c.description;
            if (c.pages != null) desc = c.curpage + "/" + c.pages.length + "; " + desc;
            if (name.length() > 140)
                name = name.substring(0, 137).trim() + "...";

            ((TextView) convertView.findViewById(R.id.comic_name)).setText(name);
            ((TextView) convertView.findViewById(R.id.comic_info)).setText(desc);

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

    /**
     * Загружает из памяти или интернета логотипы комиксов и уведомляет переданный адаптер об изменении
     */
    static class AsyncLogoGetter extends AsyncTask {
        Comic[] comics;
        ComiclistAdapter adapter;

        public AsyncLogoGetter(Comic[] comics, ComiclistAdapter adapter) {
            this.comics = comics;
            this.adapter = adapter;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            for (Comic c : comics) {
                c.logo = FileWorker.singleton.getLogo(c);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }
}
