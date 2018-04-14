package xyz.camelteam.comicreader;

import android.annotation.SuppressLint;
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import xyz.camelteam.comicreader.data.ComicDBHelper;


/** Активность общего списка комиксов
 * Использует layout activity_comiclist
 * При запуске получает сохранённый список комиксов и инициирует фоновое обновление информации о них с сервера
 * Использует кастомный адаптер для заполнения списка
 */
public class ComiclistActivity extends AppCompatActivity {

    /**
     * При запуске приложение загружает из БД список доступных комиксов,
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

        new FileWorker(getApplicationContext());
        new ComicDBHelper(getApplicationContext());

        ListView listView = findViewById(R.id.comic_list);
        Comic[] comics = ComicDBHelper.singletone.getComiclist();
        if (comics != null) {
            DataWorker.updateComicsList();
            setListViewAdapter(comics, listView);
        } else {
            @SuppressLint("StaticFieldLeak")
            DataWorker.AsyncDownload ad = new DataWorker.AsyncDownload(HttpHelper.getComiclistUrl()) {
                @Override
                void customOnPostExecute(String result) {
                    Comic[] comics = Comic.arrayFromJson(result);
                    ComicDBHelper.singletone.saveComiclist(comics);
                    setListViewAdapter(comics, listView);
                }
            };
            ad.execute();
        }
    }

    private void setListViewAdapter(Comic[] comics, ListView listView) {
        Bitmap logo_placeholder = BitmapFactory.decodeResource(getResources(), R.mipmap.logo_placeholder);

        Map<Integer, Bitmap> logos = new HashMap<>();
        ComiclistAdapter adapter = new ComiclistAdapter(comics, logo_placeholder, logos);
        new AsyncLogoGetter(comics, adapter, logos).execute();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> openComic(comics[position].getId()));
        listView.setOnLongClickListener(v -> {
            Comic comic = comics[v.getId()];
            DataWorker.saveEntirePages(comic);
            //TODO: Открывать меню с предложением: удалить комикс из памяти, загрузить в память полностью, отметить как избранное
            return false;
        });
    }

    /** Открывает ComicActivity
     * помещает в Intent название комикса, который требуется открыть.
     * @see ComicActivity#onCreate(Bundle)
     * @param id id нужного комикса */
    private void openComic(int id) {
        Intent intent = new Intent(ComiclistActivity.this, ComicActivity.class);
        intent.putExtra("Comic id", id);
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
            default:

                Log.d("Not working menu entry!", "Код для этого пункта меню ещё не написан");
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
        Map<Integer, Bitmap> logos; // логотипы комиксов

        public ComiclistAdapter(Comic[] comics, Bitmap logo_placeholder, Map<Integer, Bitmap> logos) {
            this.logo_placeholder = logo_placeholder;
            this.comics = comics;
            this.logos = logos;
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
            icon.setImageBitmap(logos.containsKey(c.getId()) ? logos.get(c.getId()) : logo_placeholder);

            String title = "[" + c.lang + "] " + c.title;
            if (title.length() > 33)
                title = title.substring(0, 31).trim() + "...";

            String desc = c.description;
            if (desc.length() > 280)
                desc = desc.substring(0, 287).trim() + "...";
//            if (c.pages != null) desc = c.curpage + "/" + c. + "; " + desc;

            ((TextView) convertView.findViewById(R.id.comic_name)).setText(title);
            ((TextView) convertView.findViewById(R.id.comic_info)).setText(desc);

            return convertView;
        }

        @Override
        public int getCount() {
            return comics == null ? 0 : comics.length;
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
        Map<Integer, Bitmap> logos;

        public AsyncLogoGetter(Comic[] comics, ComiclistAdapter adapter, Map<Integer, Bitmap> logos) {
            this.comics = comics;
            this.adapter = adapter;
            this.logos = logos;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // c.logo = FileWorker.singleton.getLogo(c);
            // publishProgress();

            // Сначала загружаем из памяти, отображаем их
            for (Comic c : comics) {
                File logo = new File(c.logo_path);
                if (logo.exists())
                    logos.put(c.getId(), FileWorker.singleton.getImage(logo));
            }
            publishProgress();

            // Только затем пытаемся добыть из инета
            for (Comic c : comics) {
                File logo = new File(c.logo_path);
                if (!logo.exists())
                    logos.put(c.getId(), FileWorker.singleton.getImage(c.logo_url, logo));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Object o) {
            // Всё и так делается в onProgressUpdate
        }
    }
}
