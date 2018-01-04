package xyz.camelteam.comicreader;

/** Класс хранит информацию о конкретном комиксе и объекты его страниц
 * Содержит название, язык, описание,
 */
public class Comic {
    public String name, shortName, lang, description, link, logoUrl;
    int curpage = 0;
    long timestamp;
    Page[] pages;

    /**
     * @param name Название
     * @param shortName Краткое название (не более 10 символов), используется в именах папок и файлов
     * @param lang Язык комикса
     * @param description Описание
     * @param link Ссылка на главную страницу комикса в интернете
     * @param pages Массив страниц комикса
     * @param icon_url URL его иконки
     * @param timestamp Отметка времени последней синхронизации
     */
    public Comic(String name, String shortName, String lang, String description, String link, Page[] pages, String icon_url, long timestamp) {
        this.name = name;
        this.shortName = shortName;
        this.lang = lang;
        this.description = description;
        this.link = link;
        this.pages = pages;
        this.logoUrl = icon_url;
        this.timestamp = timestamp;
    }

    public Page getPage() {
        return getPage(curpage);
    }

    private Page getPage(int curpage) {
        if (pages != null && pages.length + 1 >= curpage)
            return pages[curpage];
        else return null;
    }

    public static class Page {
        int number;
        String name, description, link, image_link, image_path;

        // example: new Comic.Page(245, "Dad jokes", "I feel like we shouldn't consider bonobos as sapient until they can write something about human life as a sunset or the end of a long road or something.", "https://www.smbc-comics.com/comic/dad-jokes", "https://www.smbc-comics.com/comics/1450366623-20151217.png");
        public Page(int number, String name, String description, String link, String image_link) {
            this.number = number;
            this.name = name;
            this.description = description;
            this.link = link;
            this.image_link = image_link;
        }
    }
}

