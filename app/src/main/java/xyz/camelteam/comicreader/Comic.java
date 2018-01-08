package xyz.camelteam.comicreader;

import android.graphics.Bitmap;

import com.google.gson.Gson;

/** Класс хранит информацию о конкретном комиксе и объекты его страниц
 */
public class Comic {
    public String name, shortName, lang, description, link, logoUrl;
    int curpage = 0, length;
    long timestamp;
    public Bitmap logo;
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
        length = pages == null ? 0 : pages.length;
    }

    /** Сокращённый конструктор для краткой записи (без списка страниц) */
    public Comic(String name, String shortName, String lang, String description, String link) {
        this.name = name;
        this.shortName = shortName;
        this.lang = lang;
        this.description = description;
        this.link = link;

        logoUrl = DataWorker.server_url + "logo/" + shortName + ".png";
        timestamp = 0L;
        pages = null;
    }

    /** Возвращает массив объектов комикса из JSON */
    public static Comic[] arrayFromJson(String source) {
        return new Gson().fromJson(source, Comic[].class);
    }

    /** Возвращает объект комикса из JSON */
    public static Comic fromJson(String source) {
        return new Gson().fromJson(source, Comic.class);
    }

    /** Возвращает сериализованный кортеж из переданных комиксов */
    public static String toJson(Comic[] comics) {
        return new Gson().toJson(comics);
    }

    /** Возвращает объект текущей страницы. */
    public Page getPage() {
        return getPage(curpage);
    }

    /** Возвращает объект страницы с требуемым номером.
     * Подразумевается, что переданный номер становится текущим номером страницы.
     * Если такой страницы нет, возвращает null
     * @param number положительное число не более длины массива pages
     */
    Page getPage(int number) {
        curpage = number;
        if (pages != null && pages.length + 1 >= number)
            return pages[number];
        else return null;
    }

    /**
     * Проверка того, удовлетворяет ли этот комикс переданному фильтру
     * Пока что возвращает только true
     * @param filter ещё не знаю, какого типа будет этот параметр
     */
    public boolean matchesFilter(int[] filter) {
        return true; // TODO
    }

    public int getLength() {
        return pages == null ? 0 : pages.length;
    }

    /**
     * Класс страницы комикса.
     * Содержит информацию о себе, ссылку на её страницу, url изображения и локальный путь к изображению
     */
    public static class Page {
        String title, description, thisUrl, imgUrl, bonusUrl;

        // example: new Comic.Page(245, "Dad jokes", "I feel like we shouldn't consider bonobos as sapient until they can write something about human life as a sunset or the end of a long road or something.", "https://www.smbc-comics.com/comic/dad-jokes", "https://www.smbc-comics.com/comics/1450366623-20151217.png");
        public Page(String title, String description, String thisUrl, String imgUrl, String bonusUrl) {
            this.title       = title;
            this.description = description;
            this.thisUrl     = thisUrl;
            this.imgUrl      = imgUrl;
            this.bonusUrl    = bonusUrl;
        }
    }
}

