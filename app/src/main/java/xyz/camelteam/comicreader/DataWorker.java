package xyz.camelteam.comicreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import xyz.camelteam.comicreader.data.ComicDBHelper;

/** Класс для работы с локально сохранёнными данными: загрузки и сохранения:
     * объектов комиксов
     * текстов
     * картинок
 */
public class DataWorker {

    static ComicDBHelper DbHelper;

    /**
     * Докачивает нужные страницы с сервера, если изменился timestamp
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("StaticFieldLeak")
    static void savePages(Comic comic) {
        // TODO: проверка timestamp
        new AsyncDownload(HttpHelper.getPagesUrl(comic.getId(), comic.timestamp)) {
            @Override
            void customOnPostExecute(String result) {
                DbHelper.savePages(comic.getId(), result);
            }
        }.execute();
    }

    /**
     * Скачивает с сервера и сохраняет в БД список комиксов
     */
    public static void updateComicsList() {
        @SuppressLint("StaticFieldLeak")
        AsyncDownload ad = new AsyncDownload(HttpHelper.getComiclistUrl()) {
            @Override
            void customOnPostExecute(String result) {
                // TODO
            }
        };
        ad.execute();
    }

    /**
     * Cохраняет изображения для страниц с from по to комикса
     */
    public static void saveEntirePages(Comic comiс) {
        // TODO: получить с сервера указанные страницы и сохранить изображения для них указанного комикса
    }

    /** Получает информацию по переданной ссылке
     * Не вызывать в Main потоке!!!
     *
     */
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

                result = HttpHelper.unescapeUtf(buffer.toString()); // Заменяет всякие мерзкие \\u0027 на апострофы и типа того
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

    public static void updatePages() {
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