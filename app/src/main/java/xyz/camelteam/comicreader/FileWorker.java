package xyz.camelteam.comicreader;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.camelteam.comicreader.data.ComicDBHelper;

/** Обеспечивает простой доступ к файлам приложения
 */
public class FileWorker {
    public static FileWorker singleton;
    String rootDir;
    String logoDir;
    String stripDir;

    /** При создании объекта инициализирует рабочие директории и создаёт синглтон для упрощения работы с файлами */
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

    /**
     * Возвращает загруженный из памяти или интернета логотип дял переданного комикса
     * @param comic
     * @return
     */
    Bitmap getLogo(Comic comic) {
        File logo = new File(comic.logo_path);
        if (logo.exists())
            return getImage(logo);
        else
            return getImage(comic.logo_url, logo);
    }

    Bitmap getImage(Comic comic, int page) {
        if (comic == null) return null;
        Page p = ComicDBHelper.singletone.getPage(comic.getId(), page);

        String ext = p.image_url.substring(1 + p.image_url.lastIndexOf('.'));
        File file = new File(stripDir + "/" + comic.getId() + "/" + p.number + '.' + ext);

        if (file.exists())
            return getImage(file);
        else
            return getImage(p.image_url, file);
    }

    public void saveImage(Comic comic, Page page) {
        saveImage(page.image_url, FileWorker.singleton.getPath(comic, page));
    }

    /**
     * Возвращает путь, по которому лежит или будет лежать главное изображение
     * @param comic объект комикса (оттуда берётся его id)
     * @param page страница (из неё забирается путь)
     * @return
     */
    public String getPath(Comic comic, Page page) {
        if (page.image_path != null) return page.image_path;

        page.image_path = stripDir + File.separator
                + comic.getId() + File.separator
                + "page" + page.number + "." + page.image_url.substring(page.image_url.lastIndexOf('.'));
        return page.image_path;
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
