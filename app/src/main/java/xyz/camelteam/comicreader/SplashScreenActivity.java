package xyz.camelteam.comicreader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("SplashScreen", "SplashScreen launched.");

        Intent intent = new Intent(this, ComiclistActivity.class);
        startActivity(intent);
        finish();
    }
}
