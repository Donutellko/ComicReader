package xyz.camelteam.comicreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileNotFoundException;

/** Обеспечивает простой доступ к файлам приложения
 */
public class FileWorker {
    Context context;
    File path;

    enum Category {ICON, STRIP}

    /**
     * @param name определяет название директории */
    public FileWorker(Context context, String name) {
        this.path = context.getExternalFilesDir(name);
        this.context = context;
    }

    /** Возвращает изображение по указанному пути, сохраняет прозрачность
     * @param category Название категории: icon, strip
     * */
    public Bitmap getImage(Category category, String name) {
        File file = new File(new File(path, categoryName(category)), name);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return bitmap;
    }

    /** Сохраняет изображение по указанному пути, сохраняет прозрачность */
    public void saveImage(String category, String name, Bitmap bm) throws FileNotFoundException {
        File file = new File(new File(path, category), name);
        bm.compress(Bitmap.CompressFormat.PNG, 95, context.openFileOutput(file.getAbsolutePath(), Context.MODE_PRIVATE));
    }

    /** Загружает картинку из памяти устройства */
    public static Bitmap getImage(String path, Context... context) {
        // TODO
        // Temporary:
        Bitmap bm = null;

        if ((path == null || path.length() == 0) && context.length > 0)
            bm = BitmapFactory.decodeResource(context[0].getResources(), R.raw.test_page);
        else if (path != null)
            bm = BasicImageDownloader.readFromDisk(new File(path));
        return bm;
    }

    String categoryName(Category category) {
        switch (category) {
            case ICON: return "logo";
            case STRIP: return "strip";
            default: throw new RuntimeException("Unknown category!");
        }
    }

}
