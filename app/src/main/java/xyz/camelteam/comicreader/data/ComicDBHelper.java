package xyz.camelteam.comicreader.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import xyz.camelteam.comicreader.Comic;
import xyz.camelteam.comicreader.Page;
import xyz.camelteam.comicreader.data.ComicsContract.*;

/**
 * Класс, обеспечивающий взаимодействие с БД.
 * Содержит обязательные методы onCreate и onUpdate, отдельные методы для создания новых таблиц,
 * поиска и изменения информации в них.
 */

public class ComicDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ComicDBHelper.class.getSimpleName();
    public static ComicDBHelper singletone;

    /** Имя файла базы данных */
    private static final String DATABASE_NAME = "comics.db";

    /** Версия базы данных. При изменении схемы увеличить на единицу */
    private static final int DATABASE_VERSION = 1;

    public ComicDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        singletone = this;
    }

    /** Вызывается при создании базы данных */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Строка для создания таблицы
        String SQL_CREATE_COMIC_TABLE = "CREATE TABLE " + ComicEntry.TABLE_NAME + " (" +
                ComicEntry.COLUMN_ID + " integer, " +
                ComicEntry.COLUMN_TITLE       + " text(30) not null, " +
                ComicEntry.COLUMN_DESCRIPTION + " text, " +
                ComicEntry.COLUMN_AUTHOR      + " text, " +
                ComicEntry.COLUMN_MAIN_URL    + " text, " +
                ComicEntry.COLUMN_ORIG_URL    + " text, " +
                ComicEntry.COLUMN_LOGO_URL    + " text, " +
                ComicEntry.COLUMN_LOGO_PATH   + " text, " +
                ComicEntry.COLUMN_LANG        + " text(4), " +
                ComicEntry.COLUMN_SOURCE      + " text, " +
                ComicEntry.COLUMN_TIMESTAMP   + " integer, " +
                ComicEntry.COLUMN_CURPAGE     + " integer, " +
                ComicEntry.COLUMN_PAGESCOUNT     + " integer, " +
                "primary key (" + ComicEntry.COLUMN_ID + ") " +
                "); ";

        String SQL_CREATE_CATEGORY_TABLE = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
//                CategoryEntry._ID         + " integer, " +
                CategoryEntry.COLUMN_NAME + " text not null, " +
                CategoryEntry.COLUMN_TYPE + " text, " +
                " primary key (" + CategoryEntry.COLUMN_NAME + ")" +
                "); ";

        String SQL_CREATE_COMIC_CATEGORY_TABLE = "CREATE TABLE " + ComicCategoryEntry.TABLE_NAME + " (" +
                ComicCategoryEntry.COLUMN_COMIC_ID    + " integer, " +
                ComicCategoryEntry.COLUMN_CATEGORY_ID + " integer, " +
                "primary key (" + ComicCategoryEntry.COLUMN_COMIC_ID + ", " + ComicCategoryEntry.COLUMN_CATEGORY_ID + "), " +
                "foreign key (" + ComicCategoryEntry.COLUMN_COMIC_ID + ") references COMIC(" + ComicCategoryEntry.COLUMN_COMIC_ID + "), " +
                "foreign key (" + ComicCategoryEntry.COLUMN_CATEGORY_ID + ") references CATEGORY(" + ComicCategoryEntry.COLUMN_CATEGORY_ID + ")" +
                "); ";

        // Запускаем создание таблиц
        db.execSQL(SQL_CREATE_COMIC_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_COMIC_CATEGORY_TABLE);


        Log.i("DATABASE", "Put default values into COMIC table.");
        putDefaultComiclist(db); // TODO: убрать после тестирования
    }

    /**
     * Вызывается при обновлении схемы базы данных
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: следи за этим!
    }

    /**
     * Создаёт таблицу, которая содержит описания всех страниц комикса с переданным ID
     * Формат названия создаваемой таблицы: "COMIC_" + comic_id
     * @param comic_id Индивидуальный номер комикса из таблицы COMIC
     */
    public void createComicpages(int comic_id) {
        String SQL_CREATE_COMICPAGE_TABLE = "CREATE TABLE " + ComicpageEntry.TABLE_PREFIX + comic_id + " (" +
                ComicpageEntry.COLUMN_NUMBER      + " integer, " +
                ComicpageEntry.COLUMN_TITLE       + " text, " +
                ComicpageEntry.COLUMN_DESCRIPTION + " text, " +
                ComicpageEntry.COLUMN_IMAGE_URL   + " text, " +
                ComicpageEntry.COLUMN_IMAGE_PATH  + " text, " +
                ComicpageEntry.COLUMN_PAGE_URL    + " text, " +
                ComicpageEntry.COLUMN_BONUS_URL   + " text, " +
                ComicpageEntry.COLUMN_BONUS_PATH  + " text, " +
                "primary key (" + ComicpageEntry.COLUMN_NUMBER +
                ");";

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_CREATE_COMICPAGE_TABLE);
    }

    public Comic[] getComiclist() {
        return getComiclist(null, null);
    }

    /**
     * Возвращает список информации о комиксах, соответствующих переданным фильтрам.
     * @param lang Язык или null, если любой
     * @param category Категория или null, если все
     * @return Список объектов Comic, содержащих название, язык, описание, имя автора.
     */
    public Comic[] getComiclist(String lang, String category) { // TODO: Категории
        String query =
                "SELECT "
                        + ComicEntry.COLUMN_ID + ", "
                        + ComicEntry.COLUMN_TITLE + ", "
                        + ComicEntry.COLUMN_DESCRIPTION + ", "
                        + ComicEntry.COLUMN_AUTHOR + ", "
                        + ComicEntry.COLUMN_LANG + ", "
                        + ComicEntry.COLUMN_LOGO_PATH + ", "
                        + ComicEntry.COLUMN_LOGO_URL +
                        " FROM " + ComicEntry.TABLE_NAME;

        if (lang != null) query += "WHERE " + ComicEntry.COLUMN_LANG + "=\'" + lang + "\'";
        query += ";";

        ArrayList<Comic> comics = new ArrayList<>();

        Cursor cursor = getReadableDatabase().rawQuery(query, null);
        int
                idIndex     = cursor.getColumnIndex(ComicEntry.COLUMN_ID),
                titleIndex  = cursor.getColumnIndex(ComicEntry.COLUMN_TITLE),
                descrIndex  = cursor.getColumnIndex(ComicEntry.COLUMN_DESCRIPTION),
                authorIndex = cursor.getColumnIndex(ComicEntry.COLUMN_AUTHOR),
                langIndex   = cursor.getColumnIndex(ComicEntry.COLUMN_LANG),
                logoUIndex  = cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_URL),
                logoPIndex  = cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_PATH);


        while (cursor.moveToNext()) {
            Comic c = new Comic(
                    cursor.getInt(idIndex),
                    cursor.getString(titleIndex),
                    cursor.getString(langIndex),
                    cursor.getString(descrIndex),
                    cursor.getString(authorIndex),
                    cursor.getString(logoUIndex),
                    cursor.getString(logoPIndex),
                    -1 //TODO
                    );
            comics.add(c);
        }

        Comic[] result = new Comic[comics.size()];
        comics.toArray(result);
        return result;
    }

    /**
     * Сохраняет переданный номер в качестве текущего номера страницы для комикса с переданным id
     * @param id
     * @param curpage
     */
    public void updatePage(int id, int curpage) {
        // TODO: обновление номера текущей страницы
    }

    /**
     *
     * @param id
     * @param json массив объектов Page
     */
    public void savePages(int id, String json) {
        clearPages(id);
        // TODO: десериализация json, сохранение его элементов в COMIC_<id>
    }

    /**
     * Удаляет информацию о комиксе с переданным id
     * @param id
     */
    public void clearPages(int id) {
        // TODO: drop table COMIC_<id>
    }

    public Comic getComic(int id) {
        String query = "SELECT " +
                " * " +
                // ComicEntry.COLUMN_TITLE       + ", " +
                // ComicEntry.COLUMN_DESCRIPTION + ", " +
                // ComicEntry.COLUMN_LANG        + ", " +
                // ComicEntry.COLUMN_AUTHOR      + ", " +
                // ComicEntry.COLUMN_CURPAGE     + ", " +
                // ComicEntry.COLUMN_SOURCE      + ", " +
                // ComicEntry.COLUMN_MAIN_URL    + ", " +
                // ComicEntry.COLUMN_ORIG_URL    + ", " +
                // ComicEntry.COLUMN_LOGO_URL    + ", " +
                // ComicEntry.COLUMN_TIMESTAMP   + ", " +
                " FROM " + ComicEntry.TABLE_NAME +
                " WHERE " + ComicEntry.COLUMN_ID + "=" + id + ";";
        Cursor cursor = getReadableDatabase().rawQuery(query, null);

        int titleInd   = cursor.getColumnIndex(ComicEntry.COLUMN_TITLE);
        int descInd    = cursor.getColumnIndex(ComicEntry.COLUMN_DESCRIPTION);
        int langInd    = cursor.getColumnIndex(ComicEntry.COLUMN_LANG);
        int authorInd  = cursor.getColumnIndex(ComicEntry.COLUMN_AUTHOR);
        int mainUInd   = cursor.getColumnIndex(ComicEntry.COLUMN_MAIN_URL);
        int origUInd   = cursor.getColumnIndex(ComicEntry.COLUMN_ORIG_URL);
        int logoUInd   = cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_URL);
        int logoPInd   = cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_PATH);
        int sourceInd  = cursor.getColumnIndex(ComicEntry.COLUMN_SOURCE);
        int timestInd  = cursor.getColumnIndex(ComicEntry.COLUMN_TIMESTAMP);
        int curpageInd = cursor.getColumnIndex(ComicEntry.COLUMN_CURPAGE);

        Comic comic = new Comic(id,
                cursor.getString(titleInd),
                cursor.getString(descInd),
                cursor.getString(authorInd),
                cursor.getString(mainUInd),
                cursor.getString(origUInd),
                cursor.getString(logoUInd),
                cursor.getString(logoPInd),
                cursor.getString(langInd),
                cursor.getString(sourceInd),
                cursor.getInt(timestInd),
                cursor.getInt(curpageInd),
                -1 // TODO!!!
        );
        return comic;
    }

    /**
     *  Возвращает объект страницы под номером n комикса с переданным COMIC_ID
     */
    public Page getPage(int id, int n) {
        return null; // TODO
    }

    public int pagesCount(int id) {
        // TODO
        return 0;
    }

    public void updateCurrentPage(int curpage) {
        // TODO
    }

    public void saveComiclist(Comic[] comics) {
        // TODO
    }

    private void putDefaultComiclist(SQLiteDatabase db) {
        putComic(db, new Comic(0, "SMBC", "lol", "Zach", "smbc-comics.com", null, null, null, "RU", "enSMBC", -1, 3500, 4500));
        putComic(db, new Comic(1, "XKCD", "kek", "Randall", "http://xkcd.com", null, null, null, "EN", "enXKCD", -1, 5, 3000));
        putComic(db, new Comic(2, "XKCD rus", "kek rus", "Рэндалл", "http://xkcd.ru", null, null, null, "EN", "enXKCD", -1, 5, 3000));
    }

    private void putComic(SQLiteDatabase db, Comic comic) {
        Log.i("DATABASE", "Insert into COMIC");

        String SQL_PUT_COMIC =
            "INSERT INTO " + ComicEntry.TABLE_NAME + " (\n" +
                ComicEntry.COLUMN_ID          + ", " +
                ComicEntry.COLUMN_TITLE       + ", " +
                ComicEntry.COLUMN_DESCRIPTION + ", " +
                ComicEntry.COLUMN_AUTHOR      + ", " +
                ComicEntry.COLUMN_MAIN_URL    + ", " +
                ComicEntry.COLUMN_ORIG_URL    + ", " +
                ComicEntry.COLUMN_LOGO_URL    + ", " +
                ComicEntry.COLUMN_LOGO_PATH   + ", " +
                ComicEntry.COLUMN_LANG        + ", " +
                ComicEntry.COLUMN_SOURCE      + ", " +
                ComicEntry.COLUMN_TIMESTAMP   + ", " +
                ComicEntry.COLUMN_CURPAGE     + ", " +
                ComicEntry.COLUMN_PAGESCOUNT  + "" +
            ") VALUES (\n" +
                "\'" + comic.getId()     + "\', " +
                "\'" + comic.title       + "\', " +
                "\'" + comic.description + "\', " +
                "\'" + comic.author      + "\', " +
                "\'" + comic.main_url    + "\', " +
                "\'" + comic.orig_url    + "\', " +
                "\'" + comic.logo_url    + "\', " +
                "\'" + comic.logo_path   + "\', " +
                "\'" + comic.lang        + "\', " +
                "\'" + comic.source      + "\', " +
                "\'" + comic.timestamp   + "\', " +
                "\'" + comic.curpage     + "\', " +
                "\'" + comic.pagescount  + "\' " +
            ");";

        db.execSQL(SQL_PUT_COMIC);
    }
}
