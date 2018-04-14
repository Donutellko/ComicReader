package xyz.camelteam.comicreader;

/**
 * Класс определяет формат взаимодействия с сервером
 */

class HttpHelper {
    private static final String server_url = "http://donutellko.azurewebsites.net/";

    public static String getComiclistUrl() {
        return server_url + "comiclist";
    }


    public static String getPagesUrl(int id, int timestamp) {
        return server_url + "/pageslist?timestamp=" + timestamp +"&comicid=" + id + "&lastpage=1";
    }


    /**
     * Метод заменяет коды символов на сами символы, например, \u0026#39 на '
     */
    static String unescapeUtf(String s) {
        String r;
        //r = s.replaceAll("\\u0026", "&");
        r = s.replaceAll("\\u0027", "'");
        //r = r.replaceAll("&#39;", "'");
        //r = r.replaceAll("&quot;", "\"");
        return r;
    }

    static class PageslistResponse {
        int selectedPagesCount;
        Page[] pages;
        ResponseCode responseCode;
    }

    static class ComiclistResponse {
        ResponseCode responseCode;
        Comic[] comics;
    }

    class ResponseCode {
        int responseCode;
        String responseMessage;
    }
}
