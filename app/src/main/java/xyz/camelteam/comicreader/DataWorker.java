package xyz.camelteam.comicreader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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

    @Deprecated
    final static String[] descriptions = {
            "by Zach Weinersmith. Recurring themes include atheism, God, superheroes, romance, dating, science, research, parenting and the meaning of life.", // SMBC
            "by Randall Munroe. A webcomic of romance, sarcasm, math, and language.", //XKCD
            "by Рэндел Манро. Это вебкомикс о любви, сарказме, математике и языке. ", //XKCD
            "by Mark Stanley. Научно-фантастический веб-комикс о злоключениях экипажа космического корабля «Свирепая курица»", // Freefall
            "by Mark Stanley.  Set on a planet in the early stages of terraforming, the strip deals with the antics of alien spaceship \"captain\" Sam Starfall, his robot friend Helix, and their Bowman's Wolf engineer Florence Ambrose.", // Eng Freefall
            "by Tim Buckley. A gaming-related webcomic and animated series.", //CAD
            "Webcomics about life, science and other stuff I guess.", //TAY
            "He's a cat. He plays video games.",//Gamercat
            "He's a cat. He plays video games.",//Gamercat
            "by Jago Dibuja.", // LWHAG
            "page 271 out of 271", // SeqArt
            "page 271 out of 271", // Sabrina
    };


    // Временный (или дефолтный, например) набор комиков
    @SuppressWarnings("deprecation")
    @Deprecated
    static Comic[] comicsList = {
            new Comic("Saturday Morning Breakfast Cereal", "SMBC", "EN", descriptions[0], "smbc-comics.com"),
            new Comic("XKCD", "XKCD", "EN", descriptions[1], "xkcd.com"),
            new Comic("XKCD", "XKCD", "RU", descriptions[2], "xkcd.ru"),
            new Comic("Freefall", "Freefall", "RU", descriptions[3], "comicslate.org/sci-fi/freefall"),
            new Comic("Freefall", "Freefall", "EN", descriptions[4], "http://freefall.purrsia.com"),
            new Comic("Ctrl+Alt+Del", "CAD", "EN", descriptions[5], "cad-comic.com"),
            new Comic("The Awkward Yeti", "TAY", "EN", descriptions[6], "theawkwardyeti.com/"),
            new Comic("The GaMERCaT", "GaMERCaT", "RU", descriptions[7], "comicslate.org/gamer/gamercat"),
            new Comic("The GaMERCaT", "GaMERCaT", "EN", descriptions[8], "thegamercat.com/"),
            new Comic("Living with hipstergirl and gamergirl", "LWHAG", "RU", descriptions[9], "comicslate.org/gamer/lwhag"),
            new Comic("Sequential Art", "SeqArt", "EN", descriptions[10], "collectedcurios.com/sequentialart.php"),
            new Comic("Sabrina Online", "Sabrina", "EN", descriptions[11], "sabrina-online.com"),
    };

    /**
     * Возвращает сериализованный объект комикса
     */
    public static String comicToJson(Comic comics) {
        return new Gson().toJson(comics);
    }

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
    static void update(Comic comic) {
        // TODO: проверка timestamp
        @SuppressLint("StaticFieldLeak")
        AsyncTask downloader = new AsyncDownload(DataWorker.server_url + "pages/" + comic.shortName) {
            @Override
            void customOnPostExecute(String result) {
                try {
                    if (result.charAt(0) != '[')
                        result = "[\n" + result + "\n]";
                    Comic.Page[] pages = new Gson().fromJson(result, Comic.Page[].class);
                    comic.pages = pages;
                } catch (IllegalStateException e) {
                    Log.e("Ошибка в JSON-файле: ", e.getMessage() + " " + result);
                }
            }
        };
        downloader.execute();
    }

    /**
     * Скачивает с сервера и сохраняет в SharedPreferences список комиксов
     */
    public static void updateComicsList(Context context) {
        // TODO: получить и сохранить список с сервера
        new AsyncDownload(DataWorker.server_url + "comiclist", context) {
            @Override
            void customOnPostExecute(String result) {
                SharedPreferences sp = context.getSharedPreferences("Comics", Context.MODE_PRIVATE);
                SharedPreferences.Editor se = sp.edit();
                se.putString("Comics", result);
                //Comic[] pages = new Gson().fromJson(result, Comic[].class);
            }
        }.execute();
    }

    /**
     * Получает с сервера указанные страницы и сохраняет изображения для них
     * по умолчанию сохраняет все страницы
     */
    public static void saveEntirePages(Comic comic, int from, int to) {
        // TODO: получить с сервера указанные страницы (и сохранить изображения для них) указанного комикса
        // если numbers==null, то получить все страницы.
    }

    public static void saveEntirePages(Comic comic) {
        if (comic != null && comic.getLength() > 0)
            saveEntirePages(comic, 0, comic.getLength());
    }

    /**
     * Загружает из SharedPreferences список комиксов с базовой информацией
     *
     * @return массив из комиксов с базовой информацией
     */
    public static Comic[] getComicsList(Context context /* getApplicationContext() */) {
        SharedPreferences sp = context.getSharedPreferences("Comics", Context.MODE_PRIVATE);
        String json = sp.getString("Comic list", "");
        Comic[] result = Comic.arrayFromJson(json);

        if (json.length() == 0 || result.length == 0)
            return comicsList; // tmp default list
        else
            return result;
    }

    /** Загружает полный объект комикса из SharedPreferences
     * @return комикс включая страницы
     */
    public static Comic getComic(Context context /* getApplicationContext() */, String name) {
        Comic result;
        SharedPreferences sp = context.getSharedPreferences("Comics", Context.MODE_PRIVATE);
        String json = sp.getString(name, "");
        if (json.length() == 0)
            return findComic(comicsList, name);

        result = Comic.fromJson(json);
        return result;
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

        try {
            BufferedReader reader = null;
            URLConnection uc = null;

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

    /** Метод скачивает данные по URL
     *  В случае успеха передаёт управление абстрактному методу customOnPostExecute
     */
    abstract static class AsyncDownload extends AsyncTask {
        String url, result;
        Context context;

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
            this.context = context;
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