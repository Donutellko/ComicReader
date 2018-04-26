package xyz.camelteam.comicreader;

import android.util.Log;

import com.google.gson.Gson;

import xyz.camelteam.comicreader.HttpHelper.*;

/**
 * Класс упрощает взаимодействие с объектами JSON.
 * Использует библиотеку Gson для сериализации и десериализации.
 */

public class JsonHelper {

    public static ComiclistResponse getComiclistResponse(String json) {
        try {
            return new Gson().fromJson(json, ComiclistResponse.class);
        } catch (Exception e) {
            Log.i("Json exception", "Error while parsing PageslistResponse: \n" + json);
            e.printStackTrace();
        }
        return null;
    }

    public static PageslistResponse getPageslistResponse(String json) {
        try {
            return new Gson().fromJson(json, PageslistResponse.class);
        } catch (Exception e) {
            Log.i("Json exception", "Error while parsing PageslistResponse: \n" + json);
            e.printStackTrace();
        }
        return null;
    }

}
