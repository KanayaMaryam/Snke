package com.fluttershy.snake;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;

public class GameActivity extends Activity {

    GameManager gm;
    int mode;
    SurfaceView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        mode = getIntent().getIntExtra("mode", 1);

        view = (SurfaceView) findViewById(R.id.gameCanvas);
        gm = new GameManager(10, 10, 0, 0, null, null, mode, view.getHolder());
    }
}
