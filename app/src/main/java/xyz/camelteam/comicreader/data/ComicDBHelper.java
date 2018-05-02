package xyz.camelteam.comicreader.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
    public static final String LOG_TAG = "% " + ComicDBHelper.class.getSimpleName();
    public static ComicDBHelper singletone;

    private static SQLiteDatabase writableDb, readableDb;

    /** Имя файла базы данных */
    private static final String DATABASE_NAME = "comics.db";

    /** Версия базы данных. При изменении схемы увеличить на единицу */
    private static final int DATABASE_VERSION = 1;

    public ComicDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        singletone = this;
        if (writableDb == null) writableDb = getWritableDatabase();
        if (readableDb == null) readableDb = getReadableDatabase();
    }

    /** Вызывается при создании базы данных */
    @Override
    public void onCreate(SQLiteDatabase db) {
        writableDb = db;

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
                CategoryEntry.COLUMN_NAME + " text not null, " +
                CategoryEntry.COLUMN_TYPE + " text, " +
                " primary key (" + CategoryEntry.COLUMN_NAME + ")" +
                "); ";

        String SQL_CREATE_COMIC_CATEGORY_TABLE = "CREATE TABLE " + ComicCategoryEntry.TABLE_NAME + " (" +
                ComicCategoryEntry.COLUMN_COMIC_ID    + " integer, " +
                ComicCategoryEntry.COLUMN_CATEGORY_ID + " integer, " +
                "primary key (" + ComicCategoryEntry.COLUMN_COMIC_ID + ", "
                    + ComicCategoryEntry.COLUMN_CATEGORY_ID + "), " +
                "foreign key (" + ComicCategoryEntry.COLUMN_COMIC_ID + ") references COMIC("
                    + ComicCategoryEntry.COLUMN_COMIC_ID + "), " +
                "foreign key (" + ComicCategoryEntry.COLUMN_CATEGORY_ID + ") references CATEGORY("
                    + ComicCategoryEntry.COLUMN_CATEGORY_ID + ")" +
                "); ";

        // Запускаем создание таблиц
        db.execSQL(SQL_CREATE_COMIC_TABLE);
        db.execSQL(SQL_CREATE_CATEGORY_TABLE);
        db.execSQL(SQL_CREATE_COMIC_CATEGORY_TABLE);


        Log.i(LOG_TAG, "Put default values into COMIC table.");
    }

    /**
     * Вызывается при обновлении схемы базы данных
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        writableDb = db;
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
                ComicpageEntry.COLUMN_TIMESTAMP   + " integer, " +
                "primary key (" + ComicpageEntry.COLUMN_NUMBER + ")" +
                ");";

        try {
            writableDb.execSQL(SQL_CREATE_COMICPAGE_TABLE);
        } catch (SQLiteException e) {
            if (!e.getMessage().contains("already exists")) // Игнорируем сообщение о том, что уже существует
                throw e;
        }
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
        String[] columns = new String[] {
                ComicEntry.COLUMN_ID,
                ComicEntry.COLUMN_TITLE,
                ComicEntry.COLUMN_LANG,
                ComicEntry.COLUMN_DESCRIPTION,
                ComicEntry.COLUMN_AUTHOR,
                ComicEntry.COLUMN_LOGO_URL,
                ComicEntry.COLUMN_LOGO_PATH
        };

        String selection = null;
        if (lang != null) selection = ComicEntry.COLUMN_LANG + "=\'" + lang + "\'";

        Cursor cursor = readableDb.query(ComicEntry.TABLE_NAME, columns, selection,
                null, null, null, null, null);

        ArrayList<Comic> comics = new ArrayList<>();

        int[] indexes = new int[columns.length];
        for(int i = 0; i < indexes.length; i++)
            indexes[i] = cursor.getColumnIndex(columns[i]);

        while (cursor.moveToNext()) {
            Comic c = new Comic(
                    cursor.getInt   (indexes[0]), // ID
                    cursor.getString(indexes[1]), // TITLE
                    cursor.getString(indexes[2]), // LANG
                    cursor.getString(indexes[3]), // DESCRIPTION
                    cursor.getString(indexes[4]), // AUTHOR
                    cursor.getString(indexes[5]), // LOGO_URL
                    cursor.getString(indexes[6]), // LOGO_PATH
                    -1 //TODO
                    );
            comics.add(c);
        }

        cursor.close();

        Comic[] result = new Comic[comics.size()];
        comics.toArray(result);
        return result;
    }

    /**
     * Сохраняет переданный номер в качестве текущего номера страницы для комикса с переданным id
     * @param comic_id COMIC_ID комикса
     * @param curpage Новая текущая страница
     */
    public void updateCurpage(int comic_id, int curpage) {
        writableDb.execSQL(
                "UPDATE " + ComicEntry.TABLE_NAME +
                " SET " + ComicEntry.COLUMN_CURPAGE + "=" + curpage +
                " WHERE " + ComicEntry.COLUMN_ID + "=" + comic_id +
                ";");
    }

    /** Метод сохраняет переданные страницы комикса с переданным COMIC_ID.
     */
    public void putPages(int id, Page[] pages) {
        if (pages == null || pages.length == 0) return;

        createComicpages(id);

        String query = "INSERT INTO " + ComicpageEntry.TABLE_PREFIX + id + " (" +
                ComicpageEntry.COLUMN_NUMBER      + ", " +
                ComicpageEntry.COLUMN_TITLE       + ", " +
                ComicpageEntry.COLUMN_DESCRIPTION + ", " +
                ComicpageEntry.COLUMN_PAGE_URL    + ", " +
                ComicpageEntry.COLUMN_IMAGE_URL   + ", " +
                ComicpageEntry.COLUMN_BONUS_URL   + ", " +
                ComicpageEntry.COLUMN_TIMESTAMP   + " " +
                ") VALUES (?, ?, ?, ?, ?, ?, ?);";

        SQLiteStatement ps = writableDb.compileStatement(query);

        for (Page p : pages) {
            ps.bindLong(1, p.number);
            ps.bindString(2, p.title       == null ? "''" : p.title);
            ps.bindString(3, p.description == null ? "''" : p.description);
            ps.bindString(4, p.page_url    == null ? "''" : p.page_url);
            ps.bindString(5, p.img_url == null ? "''" : p.img_url);
            ps.bindString(6, p.bonus_url   == null ? "''" : p.bonus_url);
            ps.bindLong(7, p.timestamp);

            try {
                ps.execute();
            } catch (Exception e) {
                Log.i(LOG_TAG, "error while putting page");
            }
        }
    }

    /**
     * Удаляет страницы комикса с переданным COMIC_ID
     */
    public void clearPages(int comic_id) {
        String query = "DROP TABLE COMIC_" + comic_id + " CASCADE CONSTRAINTS;";

        writableDb.execSQL(query);
    }


    public Comic getComic(int id) {
        Cursor cursor = readableDb.query(ComicEntry.TABLE_NAME, null,
                ComicEntry.COLUMN_ID + "=" + id, null, null,
                null, null);
        return cursor.moveToFirst() ? getComic(cursor) : null;
    }

    private Comic getComic(Cursor cursor) {
        Comic comic = new Comic(
                cursor.getInt   (cursor.getColumnIndex(ComicEntry.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_TITLE)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_DESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_LANG)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_AUTHOR)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_MAIN_URL)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_ORIG_URL)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_URL)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_LOGO_PATH)),
                cursor.getString(cursor.getColumnIndex(ComicEntry.COLUMN_SOURCE)),
                cursor.getInt   (cursor.getColumnIndex(ComicEntry.COLUMN_TIMESTAMP)),
                cursor.getInt   (cursor.getColumnIndex(ComicEntry.COLUMN_CURPAGE)),
                -1 // TODO
        );

        return comic;
    }

    /**
     *  Возвращает объект страницы под номером n комикса с переданным COMIC_ID
     */
    public Page getPage(int id, int n) {
        try {
            Cursor cursor = readableDb.query("COMIC_" + id, null,
                    //ComicpageEntry.COLUMN_NUMBER + "=" + n
                    null
                    , null,
                    null, null, null);
            // Log.i(LOG_TAG, "No page with number=" + n);
            return cursor.moveToFirst() ? getPage(cursor) : null;
        } catch (SQLiteException e) {
            if (e.getMessage().startsWith("no such table")) {
                createComicpages(id);
                Log.i(LOG_TAG, "Creating table COMIC_" + id);
            } else {
                e.printStackTrace();
            }
            return null;
        }
    }

    public Page getPage(Cursor cursor) {
        String[] columns = new String[] {
                ComicpageEntry.COLUMN_NUMBER,
                ComicpageEntry.COLUMN_TITLE,
                ComicpageEntry.COLUMN_DESCRIPTION,
                ComicpageEntry.COLUMN_IMAGE_URL,
                ComicpageEntry.COLUMN_IMAGE_PATH,
                ComicpageEntry.COLUMN_PAGE_URL,
                ComicpageEntry.COLUMN_BONUS_URL,
                ComicpageEntry.COLUMN_BONUS_PATH,
                ComicpageEntry.COLUMN_TIMESTAMP
        };
        int[] indexes = new int[columns.length];
        for (int i = 0; i < indexes.length; i++)
            indexes[i] = cursor.getColumnIndex(columns[i]);

        Page page = new Page(
                cursor.getInt(indexes[0]),
                cursor.getString(indexes[1]),
                cursor.getString(indexes[2]),
                cursor.getString(indexes[3]),
                cursor.getString(indexes[4]),
                cursor.getString(indexes[5]),
                cursor.getString(indexes[6]),
                cursor.getString(indexes[7]),
                cursor.getInt(indexes[8])
        );

        return page;
    }

    public int getPagesCount(int comic_id) {
        // TODO
        return 0;
    }

    public void putComics(Comic[] comics) {
        Log.i(LOG_TAG, "Inserting into COMIC...");
        long time = System.currentTimeMillis();

        for (Comic c : comics) {


            // Отвратительный костыль: сначала втыкаем в таблицу только COMIC_ID и TIMESTAMP=-1,
            // чтобы затем обновить
            try {
                writableDb.execSQL(
                        "INSERT INTO " + ComicEntry.TABLE_NAME + " ("
                                + ComicEntry.COLUMN_ID + ", " + ComicEntry.COLUMN_TIMESTAMP
                                + ", " + ComicEntry.COLUMN_TITLE
                                + ") VALUES ("
                                + c.getId() + ", -1, \'" + escape(c.title) + "\');"
                );
            } catch (SQLiteConstraintException e) {
                Log.i(LOG_TAG, "Constraint failed while inserting " + c.title + ".");
            }



            String tmp = "UPDATE " + ComicEntry.TABLE_NAME + " SET \n";
            tmp += ComicEntry.COLUMN_TIMESTAMP   + "="   + c.timestamp ;

            tmp += concatSet(ComicEntry.COLUMN_ID         , c.getId());
            tmp += concatSet(ComicEntry.COLUMN_PAGESCOUNT , c.pagescount);
            tmp += concatSet(ComicEntry.COLUMN_CURPAGE    ,
                    c.curpage == 0 ? 1: c.curpage); // TODO дичайший костыль, я хз откуда берётся curpage=0

            tmp += concatSet(ComicEntry.COLUMN_TITLE      , c.title      );
            tmp += concatSet(ComicEntry.COLUMN_DESCRIPTION, c.description);
            tmp += concatSet(ComicEntry.COLUMN_AUTHOR     , c.author     );
            tmp += concatSet(ComicEntry.COLUMN_MAIN_URL   , c.main_url   );
            tmp += concatSet(ComicEntry.COLUMN_ORIG_URL   , c.orig_url   );
            tmp += concatSet(ComicEntry.COLUMN_LOGO_URL   , c.logo_url   );
            tmp += concatSet(ComicEntry.COLUMN_LOGO_PATH  , c.logo_path  );
            tmp += concatSet(ComicEntry.COLUMN_LANG       , c.lang       );
            tmp += concatSet(ComicEntry.COLUMN_SOURCE     , c.source     );

            tmp += " WHERE " + ComicEntry.COLUMN_ID + " = " + c.getId() +
                    " AND " + ComicEntry.COLUMN_TIMESTAMP  + " < " + c.timestamp + ";";

            writableDb.execSQL(tmp);
        }

        Log.i(LOG_TAG, "Comiclist updated: добавлены или обновлены " + comics.length
                + " комиксов за " + (System.currentTimeMillis() - time) + " мс.");
    }

    private String concatSet(String name, String s) {
        if (s == null || s.equals("")) return "";
        else {
            s = escape(s);
            return ", " + name + "=\'" + s + "\'";
        }
    }

    private String concatSet(String name, int s) {
        return ", " + name + "=\'" + s + "\'";
    }

    private String escape(String s) {
        return s.replace("\'", "\'\'");
    }

    public Page[] getAllPages(int comicId) {
        ArrayList<Page> list = new ArrayList<>();

        String sql = "SELECT * FROM " + ComicpageEntry.TABLE_PREFIX + comicId + ";";

        Cursor cursor = readableDb.rawQuery(sql, null);
        while (cursor.moveToNext())
            list.add(getPage(cursor));

        Page[] result = new Page[list.size()];
        list.toArray(result);
        return result;
    }
}
