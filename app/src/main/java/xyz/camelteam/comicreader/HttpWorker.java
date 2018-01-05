package xyz.camelteam.comicreader;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс предназначен для работы с интернетом.
 * Методы класса статичны, предполагается их вызов из отдельного потока (иначе Internet in main thread exception)
 */

public class HttpWorker {

    /** Не вызывать из основного потока **/
    static String getHtml(String url_s) throws IOException {
        String result = null;

        BufferedReader reader = null;
        URLConnection uc = null;

        try {
            URL url = new URL(url_s);
            uc = url.openConnection();
            uc.setConnectTimeout(1000);
            uc.connect();
            reader = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            result = buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }

        return result;
    }

    // Сохраняет картинку по ссылке
    public static void saveImage(String url, String path) {
        new BasicImageDownloader(new ImageDownloader(path)).download(url, false);
    }

    static class ImageDownloader implements BasicImageDownloader.OnImageLoaderListener {
        String path;

        public ImageDownloader(String path) {
            this.path = path;
        }

        @Override
        public void onError(BasicImageDownloader.ImageError error) {
            //TODO
        }

        @Override
        public void onProgressChange(int percent) {
            // нужен ли ??
        }

        @Override
        public void onComplete(Bitmap result) {
            FileOutputStream out = null;
            File file = new File(path);
            if (!file.exists())
                new File(file.getParent()).mkdirs();

            try {
                out = new FileOutputStream(file.getAbsolutePath());
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) { try { out.close(); } catch (IOException e) { e.printStackTrace(); } }
            }

        }
    }
}