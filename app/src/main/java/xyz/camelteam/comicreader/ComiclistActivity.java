package xyz.camelteam.comicreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ComiclistActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comiclist);
        ListView listView = findViewById(R.id.comic_list);

        Comic[] comics = DataWorker.getComicsList(getApplicationContext());

        ComiclistAdapter adapter = new ComiclistAdapter(comics);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> openComic(comics[position].shortName));
    }

    /** Открывает PageActivity
     * помещает в Intent объект комикса, который требуется открыть.
     * @see PageActivity#onCreate(Bundle) */
    private void openComic(String name) {
        Intent intent = new Intent(ComiclistActivity.this, PageActivity.class);
        intent.putExtra("Comic name", name);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comiclist, menu);
        return super.onCreateOptionsMenu(menu);
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

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.comiclist_item, container, false);
            }

            Comic c = (Comic) getItem(position);

            String name = "[" + c.lang + "] " + c.name;
            if (name.length() > 35)
                name = name.substring(0, 32).trim() + "...";

            String desc = c.description;
            if (name.length() > 140)
                name = name.substring(0, 137).trim() + "...";

            ((TextView) convertView.findViewById(R.id.comic_name)).setText(name);
            ((TextView) convertView.findViewById(R.id.comic_info)).setText(desc);

            new AsyncLogoSetter(convertView.findViewById(R.id.comic_icon), comics[position]).execute();

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
        ImageView imageView;
        Comic comic;

        /**
         * @param imageView ImageView, в который нужно поместить загруженный логотип
         * @param comic Объект комикса, для которого это нужно */
        public AsyncLogoSetter(ImageView imageView, Comic comic) {
            this.imageView = imageView;
            this.comic = comic;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String path = "logo/" + comic.shortName;
            Bitmap bm = DataWorker.getImage(path);
            if (bm == null) {
                HttpWorker.saveImage(comic.logoUrl, path);
                bm = DataWorker.getImage(path);
            }
            return bm;
        }
    }
}