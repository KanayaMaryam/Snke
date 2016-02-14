package com.fluttershy.snake;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Chen on 2/13/2016.
 */
public class GameManager {

    final Object boardLock = new Object();
    int[][] board;
    int xHead;
    int yHead;

    InputStream in;
    OutputStream out;
    SurfaceHolder holder;

    int status = 0;
    int currentDirection = 1;
    int mode;
    int points = 0;


    public GameManager(int x, int y, int xStart, int yStart, InputStream in, OutputStream out, int mode, final SurfaceView surfaceView) {
        board = new int[x][y];
        xHead = xStart;
        yHead = yStart;
        this.in = in;
        this.mode = mode;
        this.out = out;
        this.holder = surfaceView.getHolder();
        if (mode == 1) {
            surfaceView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (event.getX() * 1f / surfaceView.getWidth() > event.getY() * 1f / surfaceView.getHeight()) {
                            if (event.getX() * 1f / surfaceView.getWidth() > (surfaceView.getHeight() - event.getY()) * 1f / surfaceView.getHeight()) {
                                currentDirection = 2;
                            } else {
                                currentDirection = 1;
                            }
                        } else {
                            if (event.getX() * 1f / surfaceView.getWidth() > (surfaceView.getHeight() - event.getY()) * 1f / surfaceView.getHeight()) {
                                currentDirection = 3;
                            } else {
                                currentDirection = 4;
                            }

                        }
                    }
                    return false;
                }
            });
        }
    }

    public void runGame() {
        status = 1;
        if (mode == 1) {
            board[xHead][yHead] = 1;
        }
        startGameAnimation();
        Thread gLoop = new Thread(new Runnable() {

            @Override
            public void run() {
                while (status > 0) {
                    update();
                    display();
                }
            }
        });
        gLoop.start();
    }

    public void display() {
        Canvas canvas = holder.lockCanvas();
        while (canvas == null) {
            canvas = holder.lockCanvas();
        }
        if (mode == 1) {
            canvas.drawColor(Color.WHITE);
            Paint p = new Paint();
            p.setColor(Color.BLACK);
            displayBoard(canvas, board, boardLock, p);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void update() {
        if (mode == 1) {
            int bufferedStatus = nextIteration(currentDirection);
            if (bufferedStatus < 0) {
                points = -bufferedStatus;
                status = -1;
            }
            if (bufferedStatus > 0) {
                points = bufferedStatus;
            }
        }
    }

    private int nextIteration(int dir){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] > 0){
                    board[i][j]--;
                }
            }
        }
        int size = points;
        int xSize = board.length;
        int ySize = board[0].length;
        switch (dir) {
            case 1:
                if (yHead + 1 >= ySize) {
                    yHead = 0;
                } else {
                    yHead++;
                }
                break;
            case 2:
                if (xHead + 1 >= xSize) {
                    xHead = 0;
                } else {
                    xHead++;
                }
                break;
            case 3:
                if (yHead - 1 < 0) {
                    yHead = ySize - 1;
                } else {
                    yHead--;
                }
                break;
            case 4:
                if (xHead - 1 < 0) {
                    xHead = ySize - 1;
                } else {
                    xHead--;
                }
                break;
        }
        if (board[xHead][yHead] == -1) {
            board[xHead][yHead] = size + 1;
            points++;
            return size;
        } else if (board[xHead][yHead] > 0) {
            return -size;
        } else {
            board[xHead][yHead] = size;
        }
        return 0;
    }

    public void startGameAnimation() {
    }

    public static void displayBoard(Canvas c, int[][] board, final Object lock, Paint p) {
        synchronized (lock) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
                    if (board[i][j] > 0) {
                        c.drawRect(i * 1.0f / board.length * c.getWidth(),
                                j * 1.0f / board[i].length * c.getHeight(),
                                (i + 1.0f) / board.length * c.getWidth(),
                                (j + 1.0f) / board[i].length * c.getHeight(), p);
                    }
                    if (board[i][j] < 0) {
                        int color = p.getColor();
                        p.setColor(Color.RED);
                        c.drawRect(i * 1.0f / board.length * c.getWidth(),
                                j * 1.0f / board[i].length * c.getHeight(),
                                (i + 1.0f) / board.length * c.getWidth(),
                                (j + 1.0f) / board[i].length * c.getHeight(), p);
                        p.setColor(color);
                    }
                }
            }
        }
    }

    private class UpdateLoop implements Runnable {

        @Override
        public void run() {
            byte[] buffer = new byte[board.length * board[0].length * 4];
            try {
                while (in.read(buffer) != -1) {
                    synchronized (boardLock) {
                        for (int i = 0; i < board.length; i++) {
                            for (int j = 0; j < board[0].length; j++) {
                                board[i][j] = convertToInt(buffer, i * board[0].length + j);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Start is inclusive
     *
     * @param a
     * @param start
     * @return
     */
    private static int convertToInt(byte[] a, int start) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value += ((a[i] & 0x00000FF) << (i * 8));
        }
        return value;
    }

    private static byte[] convertToByteArray(int x) {
        byte[] ret = new byte[4];
        ret[0] = (byte) (x & 0xFF);
        ret[1] = (byte) ((x >> 8) & 0xFF);
        ret[2] = (byte) ((x >> 16) & 0xFF);
        ret[3] = (byte) ((x >> 24) & 0xFF);

        return ret;
    }


}
