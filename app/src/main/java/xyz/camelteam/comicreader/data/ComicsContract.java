package xyz.camelteam.comicreader.data;

import android.provider.BaseColumns;

/**
 * "Контракт" для взаимодействия с БД.
 * Определяет названия столбцов и таблиц.
 * Подробнее об этом во второй леции об SQLite от Александра Климова.
 */

public class ComicsContract {

    private ComicsContract() {

    }

    public static final class ComicEntry implements BaseColumns {
        public final static String
                TABLE_NAME         = "COMIC",
//                _ID = BaseColumns._ID,
                COLUMN_ID          = "COMIC_ID",
                COLUMN_TITLE       = "TITLE",
                COLUMN_DESCRIPTION = "DESCRIPTION",
                COLUMN_AUTHOR      = "AUTHOR",
                COLUMN_MAIN_URL    = "MAIN_URL",
                COLUMN_ORIG_URL    = "ORIG_URL",
                COLUMN_LOGO_URL    = "LOGO_URL",
                COLUMN_LOGO_PATH    = "LOGO_PATH",
                COLUMN_LANG        = "LANG",
                COLUMN_SOURCE      = "SOURCE",
                COLUMN_TIMESTAMP   = "TIMESTAMP",
                COLUMN_CURPAGE     = "CURPAGE",
                COLUMN_PAGESCOUNT  = "PAGESCOUNT";

    }

    public static final class ComicpageEntry implements BaseColumns {
        public final static String
                TABLE_PREFIX = "COMIC_",
//                _ID = BaseColumns._ID,
                COLUMN_NUMBER      = "NUMBER",
                COLUMN_TITLE       = "TITLE",
                COLUMN_DESCRIPTION = "DESCRIPTION",
                COLUMN_IMAGE_URL   = "IMAGE_URL",
                COLUMN_IMAGE_PATH  = "IMAGE_PATH",
                COLUMN_PAGE_URL    = "PAGE_URL",
                COLUMN_BONUS_URL   = "BONUS_URL",
                COLUMN_BONUS_PATH  = "BONUS_PATH";
    }

    public static final class CategoryEntry implements BaseColumns {
        public final static String
                TABLE_NAME  = "CATEGORY",
                _ID = BaseColumns._ID,
                COLUMN_NAME = "NAME",
                COLUMN_TYPE = "TYPE";
    }

    public static final class ComicCategoryEntry implements BaseColumns {
        public final static String
                TABLE_NAME         = "COMIC_CATEGORY",
                COLUMN_COMIC_ID    = "COMIC_ID",
                COLUMN_CATEGORY_ID = "CATEGORY_ID";
    }

}
