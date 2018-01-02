package xyz.camelteam.comicreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс предназначен для работы с локально сохранёнными данными: загрузки и сохранения:
     * объектов комиксов
     * текстов
     * картинок
 */

public class DataWorker {

    // Возвращает список из классов имеющихся комиксов (хранятся локально или в сети в JSON-файле)
    public static Comic[] comicsFromJson(String source) {
        return new Gson().fromJson(source, Comic[].class);
    }

    public static String comicsToJson(Comic[] comics) {
        return new Gson().toJson(comics);
    }


    /** Не вызывать из основного потока **/
    static String getFromUrl(String url_s) throws IOException {
        String result = null;

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
        } finally {
            if (reader != null)
                reader.close();
        }

        return result;
    }

    public static void saveHtmls(List<String> htmls) {
        for (String s : htmls) {
            Comic.Page tmp = new Comic.Page(s);
            //TODO:
        }
    }

    public static Comic getComic(Context context /*getApplicationContext()*/, String name) {
        SharedPreferences sp = context.getSharedPreferences("Comics", 0);
        String json = sp.getString(name, "");
        if (json.length() == 0)
            return null;

        Comic comic =  new Gson().fromJson(json, Comic.class);
        update(comic);
        return comic;
    }

    private static void update(Comic comic) {
        // TODO: найти ссылку
    }

}
