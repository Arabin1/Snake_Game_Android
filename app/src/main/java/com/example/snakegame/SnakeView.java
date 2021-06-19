package com.example.snakegame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@SuppressLint("ViewConstructor")
public class SnakeView extends SurfaceView implements Runnable{

    private Thread thread = null;
    private Canvas canvas;
    private final SurfaceHolder surfaceHolder;
    private final Paint paint;
    private final int height, width;
    private final float left, top, right, bottom;
    private final int borderSize;
    private final int SIZE; // will hold the size of images
    private final int[] snakePositionX, snakePositionY; // coordinate for snake
    private int tailSize; // initial size. will grow as needed
    private boolean inGame;
    private Bitmap headImage, tailImage, fruitImage, bonusImage;
    private Bitmap rightButton, leftButton, upButton, downButton;
    private Direction direction;
    private int fruitX, fruitY; // position of fruit
    private int bonusX, bonusY; // position of bonus
    private final Random random; // to generate random position for fruit and bonus
    private int score, highScore;
    private char bonusStart; // will start the bonus
    private char bonusControl; // will control the bonus period
    private final char bonusValue; // controlling for bonus and bonus border
    private boolean bonus; // will control for bonus and drawing
    private final int level;
    private final boolean isSoundOn;
    private int restart; // will help to restart the game
    private final DataHelper dataHelper; // get the data which was saved.
    private int DELAY; // Delay based on level.
    private int valueOfScore; // value of score.
    private final int TOTAL_SNAKE_SIZE; // this will be the total snake size
    private int fruitSound, bonusSound, gameOverSound;
    private SoundPool soundPool;
    private final Context context;
    private final int SIZE_OF_BUTTON, SIZE_OF_SNAKE;
    private boolean isMoved; // control touch event. is it is true then process touch event

    public SnakeView(Context context, int height, int width, SharedPreferences sharedPreferences) {
        super(context);

        this.context = context;
        this.height = height;
        this.width = width;

        borderSize = height / 120; // size of border
        left = (float)width / 7 + borderSize; // left point of border
        top = (float)height / 30 + borderSize; // top point of border
        right = width - (float)width / 7 - borderSize; // right point of border
        bottom = height - (float)height / 5 - borderSize; // bottom point of border

        bonusValue = (char) (right / 6); // this is the bonusValue

        SIZE_OF_SNAKE = 25;
        SIZE_OF_BUTTON = 100;
        SIZE = 22; // make picture size 24
        restart = 0;

        random = new Random();

        TOTAL_SNAKE_SIZE = (int) (((right - left) * (bottom - top)) / (SIZE * SIZE)
                        - ((right - left) / SIZE) * 3);

        snakePositionX = new int[TOTAL_SNAKE_SIZE];
        snakePositionY = new int[TOTAL_SNAKE_SIZE];

        surfaceHolder = getHolder();
        paint = new Paint();
        dataHelper = new DataHelper(sharedPreferences);

        level = dataHelper.getLevel();
        setValuesOfGame(); // set the values of game

        isSoundOn = dataHelper.getSound();

        initGame(); // initialize the game
        loadBitmap();
        loadButton();
        if(isSoundOn) loadSound();
    }


    private void initGame() {
        inGame = true;
        score = 0;
        bonus = false;
        bonusStart = 0;

        snakePositionX[0] = (int) ((left + right) / 2) / SIZE * SIZE; // making multiple of SIZE
        snakePositionY[0] = (int) ((top + bottom) / 2) / SIZE * SIZE; // making multiple of SIZE

        tailSize = 5; // initial tail size
        for(int i = 1; i < tailSize; i++){
            snakePositionX[i] = snakePositionX[0] - (SIZE * i);
            snakePositionY[i] = snakePositionY[0];
        }

        isMoved = true;

        // get fruit position.
        setPositionForFruit();

        // initial direction will be right.
        direction = Direction.RIGHT;
    }

    private void loadSound() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

        soundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(5).build();
        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            descriptor = assetManager.openFd("fruit_sound.ogg");
            fruitSound = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("bonus_sound.mp3");
            bonusSound = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("game_over_sound.mp3");
            gameOverSound = soundPool.load(descriptor, 0);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setValuesOfGame() {
        switch (level) {
            case 1 : // level 1
                DELAY = 110;
                valueOfScore = 5;
                highScore = dataHelper.getHighScore1();
                break;
            case 2 : // level 2
                DELAY = 70;
                valueOfScore = 8;
                highScore = dataHelper.getHighScore2();
                break;
            case 3 : // level 3
                DELAY = 40;
                valueOfScore = 10;
                highScore = dataHelper.getHighScore3();
        }
    }

    // load the button
    private void loadButton() {
        rightButton = BitmapFactory.decodeResource(getResources(), R.drawable.right_arrow);
        rightButton = Bitmap.createScaledBitmap(rightButton, SIZE_OF_BUTTON, SIZE_OF_BUTTON, true);

        leftButton = BitmapFactory.decodeResource(getResources(), R.drawable.left_arrow);
        leftButton = Bitmap.createScaledBitmap(leftButton, SIZE_OF_BUTTON, SIZE_OF_BUTTON, true);

        upButton = BitmapFactory.decodeResource(getResources(), R.drawable.up_arrow);
        upButton = Bitmap.createScaledBitmap(upButton, SIZE_OF_BUTTON, SIZE_OF_BUTTON, true);

        downButton = BitmapFactory.decodeResource(getResources(), R.drawable.down_arrow);
        downButton = Bitmap.createScaledBitmap(downButton, SIZE_OF_BUTTON, SIZE_OF_BUTTON, true);
    }

    private void loadBitmap() {
        headImage = BitmapFactory.decodeResource(getResources(), R.drawable.head);
        headImage = Bitmap.createScaledBitmap(headImage, SIZE_OF_SNAKE, SIZE_OF_SNAKE, true);

        tailImage = BitmapFactory.decodeResource(getResources(), R.drawable.tail);
        tailImage = Bitmap.createScaledBitmap(tailImage, SIZE_OF_SNAKE, SIZE_OF_SNAKE, true);

        fruitImage = BitmapFactory.decodeResource(getResources(), R.drawable.fruit);
        fruitImage = Bitmap.createScaledBitmap(fruitImage, SIZE_OF_SNAKE, SIZE_OF_SNAKE, true);

        bonusImage = BitmapFactory.decodeResource(getResources(), R.drawable.bonus);
        bonusImage = Bitmap.createScaledBitmap(bonusImage, SIZE_OF_SNAKE, SIZE_OF_SNAKE, true);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    synchronized public boolean onTouchEvent(MotionEvent event) {
        if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP){
            // if not moved then wait until moved
            while(!isMoved) {
                try {
                    wait();
                }catch (InterruptedException | IllegalMonitorStateException e) {
                    e.printStackTrace();
                }
            }

            float x = event.getX();
            float y = event.getY();

            if(x > left && x < right) { // bonus with touch event
                if(direction == Direction.LEFT || direction == Direction.RIGHT) {
                    if(y > snakePositionY[0]) direction = Direction.DOWN;
                    else direction = Direction.UP;
                }
                else {
                    if(x > snakePositionX[0]) direction = Direction.RIGHT;
                    else direction = Direction.LEFT;
                }

            } else // otherwise control with button
                controlWithButton(x);

            if(inGame) isMoved = false;

            // if game is over then restart the game
            if(!inGame) restartGame();
        }

        return true;
    }

    private void restartGame() {
        restart++;
        if(restart == 4) {
            try {
                thread.join(); // wait for thread to finish;
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            restart = 0;

            initGame(); // initialize the game
            thread = new Thread(this);
            thread.start(); // at last start the game again.
        }
        drawGame();
    }

    private void setHighScore() {
        switch (level) {
            case 1 :
                dataHelper.setHighScore1(highScore);
                break;
            case 2 :
                dataHelper.setHighScore2(highScore);
                break;
            case 3 :
                dataHelper.setHighScore3(highScore);
        }
    }

    private void controlWithButton(float x) {
        if(x <= left) {
            if (direction == Direction.DOWN || direction == Direction.UP)
                direction = Direction.LEFT;
            else direction = Direction.UP;
        }
        else {
            if (direction == Direction.DOWN || direction == Direction.UP)
                direction = Direction.RIGHT;
            else direction = Direction.DOWN;
        }
    }

    // Draw the snake.
    public void drawGame() {
        if(surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();

            drawBorder(); // Draw the border.
            drawButton(); // draw the buttons;

            // draw snake tails
            for(int i = 1; i < tailSize; i++)
                canvas.drawBitmap(tailImage, snakePositionX[i], snakePositionY[i], null);

            canvas.drawBitmap(headImage, snakePositionX[0], snakePositionY[0], null);

            // Draw the fruits
            canvas.drawBitmap(fruitImage, fruitX, fruitY, null);

            // Draw bonus
            if(bonus) {
                if(bonusControl % 5 != 0)
                    canvas.drawBitmap(bonusImage, bonusX, bonusY, null);

                int bonusBorderSize = borderSize / 2; // the bonus border size
                // First draw a black rect
                paint.setColor(Color.BLACK);
                canvas.drawRect(right - bonusValue - bonusBorderSize, bottom + 10,
                        right + bonusBorderSize, bottom + 30 + bonusBorderSize, paint);
                // now draw a rect with white color, which will control with bonusControl.
                paint.setColor(Color.WHITE);
                canvas.drawRect(right - bonusControl, bottom + 10 + bonusBorderSize,
                        right, bottom + 30, paint);
            }

            // draw the scores
            paint.setTypeface(Typeface.create("Arial", Typeface.BOLD));
            paint.setColor(Color.RED);
            paint.setTextSize(25);
            canvas.drawText("Score : ", left + 20, bottom + 30, paint);
            paint.setTextSize(21);
            paint.setColor(Color.BLACK);
            canvas.drawText("" + score, left + 115, bottom + 30, paint);

            paint.setColor(Color.RED);
            paint.setTextSize(25);
            canvas.drawText("High Score : ", left + 185, bottom + 30, paint);
            paint.setTextSize(21);
            paint.setColor(Color.BLACK);
            canvas.drawText("" + highScore, left + 335, bottom + 30, paint);

            if(!inGame)
                gameOver();

            // Draw the whole frame.
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    // draw gameOver
    private void gameOver() {
        if(restart == 0) {
            if(score == highScore) setHighScore(); // store the high score.
            if(isSoundOn) soundPool.play(gameOverSound, 1, 1, 0, 0, 1);
        }

        paint.setColor(Color.RED);
        paint.setTextSize(30);
        canvas.drawText("Game Over", (float)width / 2 - 80, (float) (height / 2.5), paint);

        String message;
        if(4 - restart > 1) message = "times";
        else message = "time";

        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        canvas.drawText("Touch " + (4 - restart) + " more " + message + " to restart the game",
                (float) width / 2 - 250, (float) (height / 2.5) + 35, paint);
    }

    // Draw the border
    private void drawBorder() {
        // First draw a entire screen with white
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, width, height, paint);

        // Now draw a a specific screen with red for snake border
        paint.setColor(Color.RED);
        canvas.drawRect(left - borderSize, top - borderSize, right + borderSize, bottom + borderSize, paint);

        // This will help to draw the border
        paint.setColor(Color.WHITE);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    private void drawButton() {
        switch (direction) {
            case UP:
            case DOWN:
                canvas.drawBitmap(leftButton, left - SIZE_OF_BUTTON - 20, (float) ((float)height / 2.5), null);
                canvas.drawBitmap(rightButton, right + 20, (float) ((float) height / 2.5), null);
                break;
            case LEFT:
            case RIGHT:
                canvas.drawBitmap(upButton, left - SIZE_OF_BUTTON - 20, (float) ((float)height / 2.5), null);
                canvas.drawBitmap(downButton, right + 20, (float) ((float) height / 2.5), null);
        }
    }

    // This function will move the snake
    synchronized private void moveSnake() {
        // move the tail first
        for(int i = tailSize - 1; i > 1; i--) {
            snakePositionX[i] = snakePositionX[i - 1];
            snakePositionY[i] = snakePositionY[i - 1];
        }

        snakePositionX[1] = snakePositionX[0];
        snakePositionY[1] = snakePositionY[0];

        switch (direction) {
            case UP:
                snakePositionY[0] -= SIZE;
                break;
            case DOWN:
                snakePositionY[0] += SIZE;
                break;
            case RIGHT:
                snakePositionX[0] += SIZE;
                break;
            case LEFT:
                snakePositionX[0] -= SIZE;
        }

        if(!isMoved) {
            isMoved = true;
            notify(); // notify
        }
    }

    // check for collision
    private void checkCollision() {
        // Check collision.
        for(int i = 1; i < tailSize; i++) {
            if (snakePositionX[0] == snakePositionX[i] && snakePositionY[0] == snakePositionY[i]) {
                inGame = false;
                break;
            }
        }
    }

    // check border and cross the border.
    private void checkBorder() {
        if(snakePositionX[0] >= right - SIZE) {
            snakePositionX[0] = (int) left / SIZE;
            snakePositionX[0] = snakePositionX[0] * SIZE + SIZE; // making multiple of SIZE
        } else if(snakePositionX[0] < left) {
            snakePositionX[0] = (int) right / SIZE;
            snakePositionX[0] = snakePositionX[0] * SIZE - SIZE; // making multiple of SIZE
        } else if(snakePositionY[0] >= bottom - SIZE) {
            snakePositionY[0] = (int) top / SIZE;
            snakePositionY[0] =  snakePositionY[0] * SIZE + SIZE; // making multiple of SIZE
        } else if(snakePositionY[0] < top) {
            snakePositionY[0] = (int) bottom / SIZE;
            snakePositionY[0] = snakePositionY[0] * SIZE - SIZE; // making multiple of SIZE
        }
    }

    // check for fruits
    private void checkFruit() {
        // Check for fruit.
        if (snakePositionX[0] == fruitX && snakePositionY[0] == fruitY) {
            // play the sound
            if(isSoundOn) soundPool.play(fruitSound, 1, 1, 0, 0, 1);
            setPositionForFruit(); // getting fruit position
            // if tailSize is less than TOTAL_SNAKE_SIZE then increase it. otherwise not.
            if(tailSize < TOTAL_SNAKE_SIZE) tailSize++;
            bonusStart++; // start bonus after four fruits.
            if(bonusStart == 4) { // set the bonus point
                setPositionForBonus();
                bonusStart = 0;
                bonus = true;
                bonusControl = 0;
            }
            score += valueOfScore;
            if(score > highScore)
                highScore = score;
        }

        // Check for bonus.
        if(bonus){
            // check if eat the fruit.
            if(bonusX == snakePositionX[0] && bonusY == snakePositionY[0]) {
                // play the sound
                if(isSoundOn) soundPool.play(bonusSound, 1, 1, 0, 0, 1);
                score += (level * 100 * ((bonusValue + 5) / bonusControl)); // increment with a big value
                if(score > highScore) highScore = score;
                bonus = false;
            }
            bonusControl += 3;
            if(bonusControl >= bonusValue)
                bonus = false;
        }
    }

    private void setPositionForFruit() {
        // get fruit position
        fruitX = getRandomPositionX();
        fruitY = getRandomPositionY();

        // This will ensure that the fruit is not inside the snake.
        boolean isInsightSnake = true;
        while (isInsightSnake) {
            int i;
            for(i = 0; i < tailSize; i++){
                if(snakePositionX[i] == fruitX && snakePositionY[i] == fruitY) {
                    fruitX = getRandomPositionX();
                    fruitY = getRandomPositionY();
                    break;
                }
            }

            // it means the loop has been completed.
            if(i == tailSize) isInsightSnake = false;
        }
    }

    private void setPositionForBonus() {
        // get fruit position
        bonusX = getRandomPositionX();
        bonusY = getRandomPositionY();

        // This will ensure that the fruit is not inside the snake.
        boolean isInsightSnake = true;
        while (isInsightSnake) {
            int i;
            for(i = 0; i < tailSize; i++){
                if(snakePositionX[i] == bonusX && snakePositionY[i] == bonusY) {
                    bonusX = getRandomPositionX();
                    bonusY = getRandomPositionY();
                    break;
                }
            }

            // it means the loop has been completed.
            if(i == tailSize) isInsightSnake = false;

            // check if inside the fruit.
            if(fruitX == bonusX && fruitY == bonusY) {
                bonusX = getRandomPositionX();
                bonusY = getRandomPositionY();
                isInsightSnake = true;
            }
        }
    }

    // Generate random position for x
    private int getRandomPositionX() {
        int x = (int) ((random.nextInt((int) (right - SIZE - left)) + left) / SIZE);
        x *= SIZE; //making multiple of SIZE.
        if( x <= left) x += SIZE;
        return x;
    }

    // Generate random position for y
    private int getRandomPositionY() {
        int y = (int) ((random.nextInt((int) (bottom - SIZE - left)) + left) / SIZE);
        y *= SIZE; // making multiple of SIZE.
        if(y <= top) y += SIZE;
        return y;
    }

    @Override
    public void run() {
        while (inGame) {
            checkFruit();
            moveSnake();
            checkCollision();
            checkBorder();
            drawGame();
            try {
                TimeUnit.MILLISECONDS.sleep(DELAY);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        synchronized (this){
            if(!isMoved) {
                isMoved = true;
                notify();
            }
        }
    }


    public void onResume() {
        inGame = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPause() {
        inGame = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void onDestroy() {
        // release the soundPool
        if(isSoundOn) soundPool.release();
    }
}
