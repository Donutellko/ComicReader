package xyz.camelteam.comicreader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/** Обеспечивает простой доступ к файлам приложения
 */
public class FileWorker {
    public static FileWorker singleton;
    String rootDir;
    String logoDir;
    String stripDir;

    public FileWorker(Context context) {
        rootDir = context.getExternalFilesDir("Pictures").getAbsolutePath();
        logoDir = rootDir + "/logo/";
        stripDir = rootDir + "/strip/";

        if (!new File(logoDir).exists()) new File(logoDir).mkdirs();
        if (!new File(logoDir).exists()) new File(stripDir).mkdirs();

        singleton = this;
    }

    void saveImage(String url, String path) {
        new BasicImageDownloader(new ImageDownloadListener(path)).download(url, false);
    }

    Bitmap getImage(File path) {
        return BasicImageDownloader.readFromDisk(path);
    }

    Bitmap getImage(String url, File path) {
        ImageDownloadListener listener = new ImageDownloadListener(path.getAbsolutePath());
        new BasicImageDownloader(listener).download(url, false);
        while (!listener.done);
        return getImage(path);
    }

    void saveLogo(Comic comic) {
        saveImage(comic.logoUrl, logoDir);
    }

    void saveImage(Comic comic, int page) {
        String path = stripDir + comic.shortName;
        new File(path).mkdirs();
        saveImage(comic.getPage(page).imgUrl, path);
    }

    /**
     * Возвращает загруженный из памяти или интернета логотип дял переданного комикса
     * @param comic
     * @return
     */
    Bitmap getLogo(Comic comic) {
        File logo = new File(logoDir + "/" + comic.shortName + ".png");
        if (logo.exists())
            return getImage(logo);
        else
            return getImage(comic.logoUrl, logo);
    }

    Bitmap getImage(Comic comic, int page) {
        if (comic == null) return null;
        Comic.Page p = comic.getPage(page);
        if (p == null) return null;
        File file = new File(stripDir + "/" + comic.shortName + "/" + p.title + ".png");

        if (file.exists())
            return getImage(file);
        else
            return getImage(p.imgUrl, file);
    }

    static class ImageDownloadListener implements BasicImageDownloader.OnImageLoaderListener {
        String path;
        boolean done = false;

        public ImageDownloadListener(String path) {
            this.path = path;
        }

        @Override
        public void onError(BasicImageDownloader.ImageError error) {
            //TODO
            done = true;
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
                done = true;
            }

        }
    }

}
