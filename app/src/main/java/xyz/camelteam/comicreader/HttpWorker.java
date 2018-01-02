package xyz.camelteam.comicreader;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс предназначен для работы с интернетом.
 * Методы класса статичны, предполагается их вызов из отдельного потока (иначе Internet in main thread exception)
 */

public class HttpWorker {

    // Сохраняет по пути картинку по ссылке
    public static void saveImage(String url, String path) {
        //TODO:
    }

    // передаёт HTML-код страницы по переданному адресу
    public static void getHtml(String url){
        // TODO
    }
}

class HtmlLoader extends AsyncTask<String, Void, List<String>> {

    @Override
    protected void onPreExecute() { // выполняется перед doInBackground, имеет доступ к UI
        super.onPreExecute();
    }

    @Override
    protected List<String> doInBackground(String... urls) {  // выполняется в фоне, не имеет доступ к UI
        List<String> result = new ArrayList<>();

        for (String url : urls) {
            try {
                result.add(DataWorker.getFromUrl(url));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<String> result) { // выполняется после doInBackground, имеет доступ к UI
        DataWorker.saveHtmls(result);
        // TODO: использовать как-то, наверн...
        super.onPostExecute(result);
    }
}
