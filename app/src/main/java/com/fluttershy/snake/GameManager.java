package com.fluttershy.snake;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Chen on 2/13/2016.
 */
public class GameManager {

    final Object boardLock = new Object();
    private int[][] board;
    private int xHead;
    private int yHead;

    private InputStream in;
    private OutputStream out;
    private SurfaceHolder holder;

    private int status = 0;
    private int currentDirection = 1;
    private int mode;
    private int points = 1;


    public GameManager(int x, int y, int xStart, int yStart, InputStream in, OutputStream out, int mode, final SurfaceView surfaceView) {
        board = new int[x][y];
        xHead = xStart;
        yHead = yStart;
        this.in = in;
        this.mode = mode;
        this.out = out;
        this.holder = surfaceView.getHolder();
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getX() * 1f / surfaceView.getWidth() > event.getY() * 1f / surfaceView.getHeight()) {
                        if (event.getX() * 1f / surfaceView.getWidth() > (surfaceView.getHeight() - event.getY()) * 1f / surfaceView.getHeight()) {
                            currentDirection = 2;
                        } else {
                            currentDirection = 3;
                        }
                    } else {
                        if (event.getX() * 1f / surfaceView.getWidth() > (surfaceView.getHeight() - event.getY()) * 1f / surfaceView.getHeight()) {
                            currentDirection = 1;
                        } else {
                            currentDirection = 4;
                        }

                    }
                }
                return false;
            }
        });
    }

    public void runGame() {
        status = 1;
        if (mode == 1) {
            board[xHead][yHead] = 1;
            addApple();
        }
        else{
            board[xHead][yHead] = 1;
            Thread updateLoop = new Thread(new UpdateLoop());
            updateLoop.start();
        }
        startGameAnimation();
        Thread gLoop = new Thread(new Runnable() {

            @Override
            public void run() {
                while (status > 0) {
                    update();
                    display();
                    try {
                        if(mode == 1) {
                            Thread.sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                endGame();
            }
        });
        gLoop.start();
    }

    public void endGame(){
        Canvas c;
        while((c = holder.lockCanvas()) == null);
        c.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setTextSize(500);
        c.drawText("YOU LOST NOOOOBB", 0, c.getHeight(), p);
        holder.unlockCanvasAndPost(c);
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
                status = -1;
            }
            if (bufferedStatus > 0) {
                addApple();
            }
        }
    }

    private int nextIteration(int dir) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
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
            byte[] check = new byte[4];
            try {
                in.read(check);
                if (convertToInt(check, 0) == Integer.MAX_VALUE) {
                    while (in.read(buffer) != -1) {
                        synchronized (boardLock) {
                            for (int i = 0; i < board.length; i++) {
                                for (int j = 0; j < board[0].length; j++) {
                                    board[i][j] = convertToInt(buffer, i * board[0].length + j);
                                }
                            }
                        }
                    }
                } else if (convertToInt(check, 0) == Integer.MIN_VALUE) {
                    out.write((byte) currentDirection);
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

    private void addApple() {
        int spaces = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
                    spaces++;
                }
            }
        }
        double rng = Math.random() * (board.length * board[0].length - spaces) + 1;
        spaces = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] > 0) {
                    continue;
                }
                if (rng - spaces < 1) {
                    board[i][j] = -1;
                    return;
                }
                spaces++;
            }
            if (i == board.length - 1)
                i = 0;
        }
    }


}
