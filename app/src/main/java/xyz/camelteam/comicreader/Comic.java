package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;

/** Класс хранит информацию о конкретном комиксе и объекты его страниц
 */
public class Comic {
    private int comic_id;
    public String title, description, author, main_url, orig_url, logo_url, logo_path, lang, source;
    public int timestamp = -1, curpage = 1, pagescount = -1;

    public Comic(int comic_id, String title, String lang, String description, String author, String logo_url, String logo_path, int pagescount) {

        this.comic_id = comic_id;
        this.title = title;
        this.lang = lang;
        this.description = description;
        this.author = author;
        this.logo_url = logo_url;
        this.logo_path = logo_path;
        this.pagescount = pagescount;
    }

    public Comic(int comic_id, String title, String description, String author, String main_url, String orig_url, String logo_url, String logo_path, String lang, String source, int timestamp, int curpage, int pagescount) {
        if (curpage == 0)
            Log.i("jASHDFLIDAHFI", "curpage=0 in " + title);
        this.comic_id = comic_id;
        this.title = title;
        this.description = description;
        this.author = author;
        this.main_url = main_url;
        this.orig_url = orig_url;
        this.logo_url = logo_url;
        this.logo_path = logo_path;
        this.lang = lang;
        this.source = source;
        this.timestamp = timestamp;
        this.curpage = curpage;
        this.pagescount = pagescount;
    }

    public int getId() {
        return comic_id;
    }

    /** Возвращает объект комикса из JSON */
    public static Comic fromJson(String source) {
        return new Gson().fromJson(source, Comic.class);
    }

    /** Возвращает сериализованный кортеж из переданных комиксов */
    public static String toJson(Comic[] comics) {
        return new Gson().toJson(comics);
    }

    /**
     * Считаем ссылку уникальным идентификатором комикса
     * */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Comic) && main_url.equals(((Comic) obj).main_url);
    }
}

