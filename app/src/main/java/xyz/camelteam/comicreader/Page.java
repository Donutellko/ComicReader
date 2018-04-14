package xyz.camelteam.comicreader;

/**
 * Created by donat on 4/3/18.
 */

public class Page {
    int number;
    String title, description, image_url, image_path, page_url, bonus_url, bonus_path;

    public Page(int number, String title, String description, String image_url, String image_path, String page_url, String bonus_url, String bonus_path) {
        this.number = number;
        this.title = title;
        this.description = description;
        this.image_url = image_url;
        this.image_path = image_path;
        this.page_url = page_url;
        this.bonus_url = bonus_url;
        this.bonus_path = bonus_path;
    }

    public static Page getFiller() {
        Page filler = new Page(-1, "Page is not available", "", null, null, null, null, null);
        return filler;
    }
}
