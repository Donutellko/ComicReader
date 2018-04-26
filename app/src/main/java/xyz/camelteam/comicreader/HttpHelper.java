package xyz.camelteam.comicreader;

/**
 * Класс определяет формат взаимодействия с сервером
 */

class HttpHelper {
    private static final String server_url = "http://donutellko.azurewebsites.net/";

    public static String getComiclistUrl() {
        return server_url + "comiclist";
    }

    public static String getPagesUrl(int id) {
        return getPagesUrl(id, -1);
    }

    public static String getPagesUrl(int id, int timestamp) {
        return server_url + "/pageslist?timestamp=" + timestamp +"&comicid=" + id + "&lastpage=1";
    }

    /**
     * Метод заменяет коды символов на сами символы, например, \u0026#39 на '
     */
    static String unescapeUtf(String s) {
        String r = s;
        //r = s.replaceAll("\\u0026", "&");
        r = r.replaceAll("u00\\d\\d", "'");
        //r = r.replaceAll("&#39;", "'");
        //r = r.replaceAll("&quot;", "\"");
        return r;
    }

    abstract static class Response {
        ResponseCode responseCode;

        public boolean isSuccess() {
            return responseCode.responseCode == ResponseCode.SUCCESS_RESPONSE;
        }
    }

    static class PageslistResponse extends Response {
        int selectedPagesCount;
        Page[] pages;
    }

    static class ComiclistResponse extends Response {
        Comic[] comics;
    }

    class ResponseCode {
        int responseCode;
        String responseMessage;

        static final int SUCCESS_RESPONSE = 0;
    }
}
