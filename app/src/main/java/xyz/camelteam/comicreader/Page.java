package xyz.camelteam.comicreader;

/**
 * Created by donat on 4/3/18.
 */

public class Page {
    public int number, timestamp;
    public String title, description, img_url, img_path, page_url, bonus_url, bonus_path;

    public Page(int number, String title, String description, String img_url, String img_path, String page_url, String bonus_url, String bonus_path, int timestamp) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.img_url = img_url;
        this.img_path = img_path;
        this.page_url = page_url;
        this.bonus_url = bonus_url;
        this.bonus_path = bonus_path;
        this.timestamp = timestamp;
    }

    public static Page getFiller() {
        Page filler = new Page(-1, "Page is not available", "", null, null, null, null, null, -1);
        return filler;
    }
}
