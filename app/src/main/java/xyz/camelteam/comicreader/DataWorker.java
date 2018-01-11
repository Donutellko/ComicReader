package xyz.camelteam.comicreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/** Класс для работы с локально сохранёнными данными: загрузки и сохранения:
     * объектов комиксов
     * текстов
     * картинок
 */
public class DataWorker {

    final static String server_url = "http://donutellko.azurewebsites.net/";

    /**
     * Возвращает сериализованный кортеж из переданного массива объектов комиксов без объектов страниц (только базовая информация)
     */
    public static String comicsListToJson(Comic[] comics) {
        Comic[] simpleComics = comics.clone();
        for (int i = 0; i < comics.length; i++)
            simpleComics[i].pages = null;
        return Comic.toJson(simpleComics);
    }

    /**
     * Докачивает нужные страницы с сервера, если изменился timestamp
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("StaticFieldLeak")
    static void savePages(SharedPreferences sp, Comic comic) {
        // TODO: проверка timestamp
        new AsyncDownload(DataWorker.server_url + "pages/" + comic.shortName) {
            @Override
            void customOnPostExecute(String result) {
                comic.pagesFromJson(result);
                sp.edit().putString(comic.shortName, new Gson().toJson(comic.pages)).apply();
            }
        }.execute();
    }

    /**
     * Скачивает с сервера и сохраняет в SharedPreferences список комиксов
     */
    public static void updateComicsList(SharedPreferences sp) {
        @SuppressLint("StaticFieldLeak")
        AsyncDownload ad = new AsyncDownload(DataWorker.server_url + "comiclist") {
            @Override
            void customOnPostExecute(String result) {
                sp.edit().putString("Comics", result).apply();
            }
        };
        ad.execute();
    }

    /**
     * Cохраняет изображения для страниц с from по to комикса
     */
    public static void saveEntirePages(Comic comic, int from, int to) {
        // TODO: получить с сервера указанные страницы (и сохранить изображения для них) указанного комикса
        // если numbers==null, то получить все страницы.
    }

    /**
     * Обновляет номер страницы переданного комикса в SharedPreferences
     */
    public static void updateComic(SharedPreferences sp, Comic comic) {
        String json = sp.getString("Comic list", "");
        if (json.length() == 0) return;

        Comic[] comics = Comic.arrayFromJson(json);
        for(Comic c : comics) {
            if (c.equals(comic)) {
                Log.i("cstm: Updating curpage", "for " + c.shortName + ": " + comic.curpage);
                c.curpage = comic.curpage;
                sp.edit().putString("Comic list", new Gson().toJson(comics)).apply();
            }
        }
    }

    /**
     * Загружает из SharedPreferences список комиксов с базовой информацией
     * @return массив из комиксов с базовой информацией
     */
    public static Comic[] loadComicsList(SharedPreferences sp) {
        String json = sp.getString("Comic list", "");
        Comic[] result = Comic.arrayFromJson(json);

        if (json.length() > 0 && result.length > 0)
            return result;
        return null;
    }

    public static void saveComicsList(SharedPreferences sp, Comic[] comics) {
        sp.edit().putString("Comic list", new Gson().toJson(comics)).apply();
    }

    /** Загружает полный объект комикса из SharedPreferences
     * @return комикс включая страницы
     */
    public static Comic.Page[] getPages(SharedPreferences sp, String name) {
        Comic result;
        String json = sp.getString(name, "");

        return json.length() == 0 ? null : new Gson().fromJson(json, Comic.Page[].class);
    }

    /**
     * Ищет в переданном списке комикс с таким же именем
     */
    public static Comic findComic(Comic[] comics, String name) {
        for (Comic c : comics)
            if (c.name.equals(name) || c.shortName.equals(name))
                return c;
        return null;
    }

    // Не вызывать в Main потоке
    public static String getWebpage(String url_s) {
        String result = null;
        BufferedReader reader = null;
        URLConnection uc;

        try {
            try {
                URL url = new URL(url_s);
                uc = url.openConnection();
                uc.setConnectTimeout(1000);
                uc.connect();
                reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
                StringBuilder buffer = new StringBuilder();
                int read;
                char[] chars = new char[1024];
                while ((read = reader.read(chars)) != -1)
                    buffer.append(chars, 0, read);

                result = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null)
                    reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void saveAllImages(Comic current) {
        // TODO
    }

    /** Метод скачивает данные по URL
     *  В случае успеха передаёт управление абстрактному методу customOnPostExecute
     */
    abstract static class AsyncDownload extends AsyncTask {
        String url, result;

        /**
         * @param url URL-адрес, данные по которому нужно получить
         */
        public AsyncDownload(String url) {
            this.url = url;
        }

        /**
         * @param url URL-адрес, данные по которому нужно получить
         */
        public AsyncDownload(String url, Context context) {
            this.url = url;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            result = getWebpage(url);
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (result != null && result.length() > 0)
                customOnPostExecute(result);
            super.onPostExecute(o);
        }

        /**
         * Этот метод нужно унаследовать, чтобы обработать полученные данные
         * @param result Данные, полученные по переданному URL-адресу
         */
        abstract void customOnPostExecute(String result);
    }
}