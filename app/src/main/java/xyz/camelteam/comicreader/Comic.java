package xyz.camelteam.comicreader;

/**
 * Класс, хранящий информацию о конкретном комиксе: название, описание и т.п., списки его страниц
 * А также информацию о том, как получать с сервера страницы и как их обрабатывать (например, CoTAN на comicslate)
 */

public class Comic {
    String name, description, link;
    int curpage = 0;
    Page[] pages;

    public Comic(String name, String description, String link, Page[] pages) {
        this.name = name;
        this.description = description;
        this.link = link;
        this.pages = pages;
    }

    public static class Page {
        int number;
        String name, description, link, link_next, local_path;

        public Page(int number, String name, String description, String link, String link_next, String local_path) {
            this.number = number;
            this.name = name;
            this.description = description;
            this.link = link;
            this.local_path = local_path;
        }

        public Page(String html) {
            // TODO: распарсить и сохранить то, что нужно
        }
    }
}

