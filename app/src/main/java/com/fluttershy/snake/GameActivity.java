package com.fluttershy.snake;

import android.app.Activity;
import android.os.Bundle;

public class GameActivity extends Activity {

    GameManager gm;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_game);

        mode = getIntent().getIntExtra("mode", 1);

        gm = new GameManager(10, 10, 0, 0, null, null);
    }
}
