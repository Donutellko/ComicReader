package xyz.camelteam.comicreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/** Класс для работы с локально сохранёнными данными: загрузки и сохранения:
     * объектов комиксов
     * текстов
     * картинок
 */
public class DataWorker {

    // TODO: Временный (или дефолтный) набор комиков
    static Comic[] comicsList = {
            new Comic("Saturday Morning Breakfast Cereal"     , "SMBC"     , "EN", "page 237 out of 20!8" , "smbc-comics.com"                        , null, "[logourl]", 0L),
            new Comic("XKCD"                                  , "XKCD"     , "EN", "page 276 out of 6543" , "xkcd.com"                               , null, "[logourl]", 0L),
            new Comic("XKCD"                                  , "XKCD"     , "RU", "page 276 out of 6543" , "xkcd.ru"                                , null, "[logourl]", 0L),
            new Comic("Ctrl+Alt+Del"                          , "CAD"      , "EN", "page 271 out of 271"  , "cad-comic.com"                          , null, "[logourl]", 0L),
            new Comic("Freefall"                              , "Freefall" , "RU", "page 2065 out of 2066", "comicslate.org/sci-fi/freefall"         , null, "[logourl]", 0L),
            new Comic("GaMERCaT"                              , "GamerCat" , "RU", "page 271 out of 271"  , "comicslate.org/gamer/lwhag"             , null, "[logourl]", 0L),
            new Comic("Living with hipstergirl and gamergirl" , "LWHAG"    , "RU", "page 271 out of 271"  , "comicslate.org/gamer/gamercat"          , null, "[logourl]", 0L),
            new Comic("Sequential Art"                        , "SeqArt"   , "EN", "page 271 out of 271"  , "collectedcurios.com/sequentialart.php"  , null, "[logourl]", 0L),
            new Comic("Sabrina Online"                        , "Sabrina"  , "EN", "page 271 out of 271"  , "sabrina-online.com"                     , null, "[logourl]", 0L),

    };

    /** Возвращает массив объектов комикса из JSON */
    public static Comic[] comicsFromJson(String source) {
        return new Gson().fromJson(source, Comic[].class);
    }

    /** Возвращает объект комикса из JSON */
    public static Comic comicFromJson(String source) {
        return new Gson().fromJson(source, Comic.class);
    }

    /** Возвращает сериализованный кортеж из переданных комиксов */
    public static String comicsToJson(Comic[] comics) {
        return new Gson().toJson(comics);
    }

    /** Возвращает сериализованный объект комикса */
    public static String comicToJson(Comic comics) {
        return new Gson().toJson(comics);
    }

    /** Возвращает сериализованный кортеж из переданного массива объектов комиксов без объектов страниц (только базовая информация) */
    public static String comicsListToJson(Comic[] comics) {
        Comic[] simpleComics = comics.clone();
        for (int i = 0; i < comics.length; i++)
            simpleComics[i].pages = null;
        return comicsToJson(simpleComics);
    }

    /** Загружает картинку из памяти устройства */
    public static Bitmap getImage(String path, Context... context) {
        // TODO
        // Temporary:
        if (path == null || path.length() == 0)
            return BitmapFactory.decodeResource(context[0].getResources(), R.mipmap.test_page);
        else
            return BasicImageDownloader.readFromDisk(new File(path));
    }

    /** Докачивает нужные страницы с сервера, если изменился timestamp */
    static void update(Comic comic) {
        // TODO:
        // temporary:
        comic.pages = new Comic.Page[]{
                new Comic.Page(1, "Licorice", "Not to mention the difficulty of synchronizing your efforts with my once-per-solstice state of carnal arousal!", "https://www.smbc-comics.com/comic/licorice", "https://www.smbc-comics.com/comics/1450454206-20151218.png"),
                new Comic.Page(2, "Quantum mechanics is weird", "And lo, The Lord spake, saying, Let the fundamental equations contain an imaginary component.", "https://www.smbc-comics.com/comic/quantum-mechanics-is-weird", "https://www.smbc-comics.com/comics/1450539983-20151219.png"),
                new Comic.Page(3, "Dad jokes", "I feel like we shouldn't consider bonobos as sapient until they can write something about human life as a sunset or the end of a long road or something.", "https://www.smbc-comics.com/comic/dad-jokes", "https://www.smbc-comics.com/comics/1450366623-20151217.png"),
                new Comic.Page(4, "Thank you for the sex", "PS: Make America Great Again", "https://www.smbc-comics.com/comic/thank-you-for-the-sex", "https://www.smbc-comics.com/comics/1450624616-20151220.png"),
                new Comic.Page(5, "E-stalking", "All old ladies wear pink Mother Hubbard dresses and sit in rocking chairs all the time.", "https://www.smbc-comics.com/comic/e-stalking", "https://www.smbc-comics.com/comics/1450711961-20151221.png"),
                new Comic.Page(6, "God", "Hallowed be thy name, Steve.", "https://www.smbc-comics.com/comic/god", "https://www.smbc-comics.com/comics/1450799655-20151222.png"),
                new Comic.Page(7, "Other riddles of sphinx", "With apologies to anyone of good taste.", "https://www.smbc-comics.com/comic/other-riddles-of-the-sphinx", "https://www.smbc-comics.com/comics/1450886738-20151223.png"),
        };
        comic.curpage = 2;
    }

    /** Скачивает с сервера и сохраняет в SharedPreferences список комиксов */
    public static void saveComicsList() {
        // TODO: получить и сохранить список с сервера
    }

    /** Получает с сервера указанные страницы (но не сохранять изображения для них) указанного комикса
     * по умолчанию сохраняет все страницы */
    public static void savePagesList(String name, int[] numbers) {
        // TODO: получить с сервера указанные страницы
        // если numbers==null, то получить все страницы.
    }

    /** Получает с сервера указанные страницы и сохраняет изображения для них
     * по умолчанию сохраняет все страницы */
    public static void savePages(String name, int[] numbers) {
        // TODO: получить с сервера указанные страницы (и сохранить изображения для них) указанного комикса
        // если numbers==null, то получить все страницы.
    }

    /** Загружает из SharedPreferences список комиксов с базовой информацией
     * @return массив из комиксов с базовой информацией */
    public static Comic[] getComicsList(Context context /* getApplicationContext() */) {
        SharedPreferences sp = context.getSharedPreferences("Comics", Context.MODE_PRIVATE);
        String json = sp.getString("Comic list", "");
        Comic[] result = comicsFromJson(json);

        if (json.length() == 0 || result.length == 0)
            return  comicsList; // tmp default list
        else
            return result;
    }

    /** Загружает полный объект комикса из SharedPreferences
     * @return комикс включая страницы */
    public static Comic getComic(Context context /* getApplicationContext() */, String name) {
        Comic result;
        SharedPreferences sp = context.getSharedPreferences("Comics", Context.MODE_PRIVATE);
        String json = sp.getString(name, "");
        if (json.length() == 0)
            return getComic(comicsList, name);

        result = comicFromJson(json);
        return result;
    }

    /** Ищет в переданном списке комикс с таким же именем */
    public static Comic getComic(Comic[] comics, String name) {
        for (Comic c : comics)
            if (c.name.equals(name) || c.shortName.equals(name))
                return c;
        return null;
    }
}
