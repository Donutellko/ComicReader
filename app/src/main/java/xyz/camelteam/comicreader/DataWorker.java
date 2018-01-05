package xyz.camelteam.comicreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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

    final static String server_url = "http://donutellko.azurewebsites.net/";

    // Временный (или дефолтный, например) набор комиков
    @Deprecated
    static Comic[] comicsList = {
            new Comic("Saturday Morning Breakfast Cereal"     , "SMBC"     , "EN", descriptions[0 ], "smbc-comics.com"                       ),
            new Comic("XKCD"                                  , "XKCD"     , "EN", descriptions[1 ], "xkcd.com"                              ),
            new Comic("XKCD"                                  , "XKCD"     , "RU", descriptions[2 ], "xkcd.ru"                               ),
            new Comic("Freefall"                              , "Freefall" , "RU", descriptions[3 ], "comicslate.org/sci-fi/freefall"        ),
            new Comic("Freefall"                              , "Freefall" , "EN", descriptions[4 ], "http://freefall.purrsia.com"           ),
            new Comic("Ctrl+Alt+Del"                          , "CAD"      , "EN", descriptions[5 ], "cad-comic.com"                         ),
            new Comic("The Awkward Yeti"                      , "TAY"      , "EN", descriptions[6 ], "theawkwardyeti.com/"                   ),
            new Comic("The GaMERCaT"                          , "GaMERCaT" , "RU", descriptions[7 ], "comicslate.org/gamer/gamercat"         ),
            new Comic("The GaMERCaT"                          , "GaMERCaT" , "EN", descriptions[8 ], "thegamercat.com/"                      ),
            new Comic("Living with hipstergirl and gamergirl" , "LWHAG"    , "RU", descriptions[9 ], "comicslate.org/gamer/lwhag"            ),
            new Comic("Sequential Art"                        , "SeqArt"   , "EN", descriptions[10], "collectedcurios.com/sequentialart.php" ),
            new Comic("Sabrina Online"                        , "Sabrina"  , "EN", descriptions[11], "sabrina-online.com"                    ),

    };

    /** Возвращает сериализованный объект комикса */
    public static String comicToJson(Comic comics) {
        return new Gson().toJson(comics);
    }

    /** Возвращает сериализованный кортеж из переданного массива объектов комиксов без объектов страниц (только базовая информация) */
    public static String comicsListToJson(Comic[] comics) {
        Comic[] simpleComics = comics.clone();
        for (int i = 0; i < comics.length; i++)
            simpleComics[i].pages = null;
        return Comic.toJson(simpleComics);
    }

    /** Докачивает нужные страницы с сервера, если изменился timestamp */
    static void update(Comic comic) {
        // TODO:
        // temporary:
        comic.pages = new Comic.Page[]{
                new Comic.Page("Licorice", "Not to mention the difficulty of synchronizing your efforts with my once-per-solstice state of carnal arousal!", "https://www.smbc-comics.com/comic/licorice", "https://www.smbc-comics.com/comics/1450454206-20151218.png"),
                new Comic.Page("Quantum mechanics is weird", "And lo, The Lord spake, saying, Let the fundamental equations contain an imaginary component.", "https://www.smbc-comics.com/comic/quantum-mechanics-is-weird", "https://www.smbc-comics.com/comics/1450539983-20151219.png"),
                new Comic.Page("Dad jokes", "I feel like we shouldn't consider bonobos as sapient until they can write something about human life as a sunset or the end of a long road or something.", "https://www.smbc-comics.com/comic/dad-jokes", "https://www.smbc-comics.com/comics/1450366623-20151217.png"),
                new Comic.Page("Thank you for the sex", "PS: Make America Great Again", "https://www.smbc-comics.com/comic/thank-you-for-the-sex", "https://www.smbc-comics.com/comics/1450624616-20151220.png"),
                new Comic.Page("E-stalking", "All old ladies wear pink Mother Hubbard dresses and sit in rocking chairs all the time.", "https://www.smbc-comics.com/comic/e-stalking", "https://www.smbc-comics.com/comics/1450711961-20151221.png"),
                new Comic.Page("God", "Hallowed be thy name, Steve.", "https://www.smbc-comics.com/comic/god", "https://www.smbc-comics.com/comics/1450799655-20151222.png"),
                new Comic.Page("Other riddles of sphinx", "With apologies to anyone of good taste.", "https://www.smbc-comics.com/comic/other-riddles-of-the-sphinx", "https://www.smbc-comics.com/comics/1450886738-20151223.png"),
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
        Comic[] result = Comic.arrayFromJson(json);

        if (json.length() == 0 || result.length == 0)
            return comicsList; // tmp default list
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

        result = Comic.fromJson(json);
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
