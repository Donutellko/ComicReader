package xyz.camelteam.comicreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

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
        if (url == null || path == null || path.getPath().length() == 0) {
            Log.i("Downloading image", "url=\"" + url + "\", path=\"" + path + "\"");
            return null;
        }

        if (path.getParentFile().mkdirs())
            Log.i("FileWorker", "Directories for image created.");

        ImageDownloadListener listener = new ImageDownloadListener(path.getAbsolutePath());
        new BasicImageDownloader(listener).download(url, false);

        while (!listener.done);

        return getImage(path);
    }

    /**
     * Возвращает загруженный из памяти или интернета логотип дял переданного комикса
     */
    Bitmap getLogo(Comic comic) {
        File logo = new File(comic.logo_path);
        if (logo.exists())
            return getImage(logo);
        else
            return getImage(comic.logo_url, logo);
    }

    Bitmap getImage(Comic comic, Page p) {
        if (comic == null) return null;

        File file = new File(getPath(comic, p));

        if (file.exists())
            return getImage(file);
        else {
            return getImage(p.img_url, file);
        }
    }

    Bitmap getImage(Comic comic, int page) {
        if (comic == null) return null;
        Page p = ComicDBHelper.singletone.getPage(comic.getId(), page);

        return getImage(comic, p);
    }

    /**
     * Скачивает изображение, если его не существует, в папку соответствующего комикса
     */
    public void saveImage(Comic comic, Page page) {
        String path = FileWorker.singleton.getPath(comic, page);
        if (new File(path).exists())
            return;

        saveImage(page.img_url, path);
    }

    /**
     * Возвращает путь, по которому лежит или будет лежать главное изображение
     * @param comic объект комикса (оттуда берётся его id)
     * @param page страница (из неё забирается путь)
     * @return строка абсолютного пути
     */
    public String getPath(Comic comic, Page page) {
        if (page.img_path != null) return page.img_path;

        page.img_path = stripDir + File.separator
                + comic.getId() + File.separator
                + "page" + page.number + "." + page.img_url.substring(page.img_url.lastIndexOf('.'));
        return page.img_path;
    }

    /**
     * Возвращает реальный или предполагаемый путь для файла логотипа.
     * Возвращает new File(""), если не указан LOGO_URL комикса
     * @return объект файла
     */
    public File getLogoFile(Comic c) {
        if (c.logo_path != null)
            return new File(c.logo_path);

        if (c.logo_url == null || c.logo_url.length() == 0 || c.logo_url.indexOf('.') == -1)
            return new File("");

        c.logo_path = logoDir + File.separator + c.getId() + "." + c.logo_url.substring(c.logo_url.lastIndexOf('.'));

        return new File(c.logo_path);
    }

    static class ImageDownloadListener implements BasicImageDownloader.OnImageLoaderListener {
        String path;
        boolean done = false;

        public ImageDownloadListener(String path) {
            this.path = path;
        }

        @Override
        public void onError(BasicImageDownloader.ImageError error) {
            // TODO
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
                if (new File(file.getParent()).mkdirs())
                    Log.i("FileWorker", "Directories for image created.");

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
