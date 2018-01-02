package xyz.camelteam.comicreader;

/**
 * Класс, хранящий информацию о конкретном комиксе: название, описание и т.п., списки его страниц
 * А также информацию о том, как получать с сервера страницы и как их обрабатывать (например, CoTAN на comicslate)
 */

public class Comic {
    String name, description, link;
    Page[] pages;

    public Comic(String name, String description, String link, Page[] pages) {
        this.name = name;
        this.description = description;
        this.link = link;
        this.pages = pages;
    }

    public static class Page {
        int number;
        String name, description, link, local_path;

        public Page(int number, String name, String description, String link, String local_path) {
            this.number = number;
            this.name = name;
            this.description = description;
            this.link = link;
            this.local_path = local_path;
        }

        public Page(String html) {

        }
    }
}

