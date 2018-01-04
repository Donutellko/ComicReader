package xyz.camelteam.comicreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Здесь происходит обработка HTML страниц: находжение на них ссылок, изображений и прочего, загрузка файлов и самого кода HTML
 */

/** TODO! ЭТО КЛАСС БУДЕТ ПЕРЕНЕСЁН В СЕРВЕРНУЮ ЧАСТЬ! **/

public class HtmlWorker {

    public static void cleaner(List<String> source, List<String> ignore) {
        for (String s : source)
            for (String i : ignore)
                if (s.contains(i)) {
                    source.remove(s);
                    break;
                }
    }

    /** Убирает лишние символы (повторяющиеся пробелы, табуляции...), заменяя их на одинарный пробел */
    public static String clean(String sourceStr) {
        char[] source = sourceStr.toCharArray();
        StringBuilder result = new StringBuilder(source.length / 2);
        boolean prevIsWhite = false;
        for (char c : source) {
            boolean curIsWhite = Character.isWhitespace(c);
            if (!(prevIsWhite && curIsWhite)) {
                result.append(curIsWhite ? ' ' : c);
            }
            prevIsWhite = curIsWhite;
        }
        return result.toString();
    }

    public static List<String> findLinks(String source) {
        List<String> result =  findBetween(source, "src=\"", "\"");
        result.addAll(findBetween(source, "href=\"", "\""));

        return  result;
    }

    public static List<Link> findImages(String source) {
        List<String> tags = findBetween(source, "<img", "</img>");
        List<Link> result = new ArrayList<>(tags.size());

        for (String tag : tags) {
            String id = findFirstBetween(source, "src=\"", "\"");
            String link = findFirstBetween(source, "id=\"", "\"");
            String title = findFirstBetween(source, "title=\"", "\"");
            result.add(new Link(id, link, title));
        }

        return  result;
    }
    // Возращает список всех фрагментов между указанными подстроками
    public static String findFirstBetween(String source, String from, String to) {
        int begin =  source.indexOf(from);
        int end = source.indexOf(to, begin);
        if (begin == -1 || end == -1) return null;
        return source.substring(begin + from.length(), end).trim().toLowerCase();
    }

    // Возращает список всех фрагментов между указанными подстроками
    public static List<String> findBetween(String source, String from, String to) {
        List<String> result = new ArrayList<>();
        int begin = -1;
        while (true) {
            begin = source.indexOf(from, begin + 1);
            int end = source.indexOf(to, begin);
            if (begin == -1 || end == -1) break;
            result.add(source.substring(begin + from.length(), end).trim().toLowerCase());
        }
        return result;
    }

    static class Link {
        String id;
        String link;
        String title;

        public Link(String id, String link, String title) {
            this.id = id;
            this.link = link;
            this.title = title;
        }

        public String getExtention() {
            int begin = link.lastIndexOf('.');
            int tmp = link.lastIndexOf('/');
            if (begin != 0 && begin > tmp && link.length() - begin <= 4)
                return link.substring(begin);
            else return "";
        }
    }

    // некоторые ссылки бывают вообще ненужны (во всяких рекламах и прочем), это дефолтный список их вхождений
    public static List<String> defaultIgnoreList() {
        String[] result = {"google", "patreon", "amazon", "facebook", "tumblr", "twitter", "random", ".css", ".js"};
        return Arrays.asList(result);
    }
}
