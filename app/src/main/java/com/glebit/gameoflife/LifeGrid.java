package com.glebit.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Itenberg on 25.05.2016.
 */
public class LifeGrid extends View
{
    private static final int CELL_SIZE=16;

    private Mode mCurrentMode;
    private Game mGame;
    private int mDelay=100;

    // время обновления в мс
    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    // игровое поле
    public byte[][] getField()
    {
        return mGame.getGameGrid();
    }

    public void setField(byte[][] field)
    {
        if(mCurrentMode!=Mode.PAUSE)
            mCurrentMode=Mode.PAUSE;

        mGame.setGameGrid(field);
        invalidate();
    }

    private RefreshHandler mRedrawHandler = new RefreshHandler();
    // отлавливаем обновления
    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            if(mCurrentMode==Mode.RUNNING)
            {
                LifeGrid.this.update();
                LifeGrid.this.invalidate();
            }
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    public LifeGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
    }

    public LifeGrid(Context context) {
        super(context);
        setFocusable(true);
    }

    /*
    Создаем Game здесь т.к. в конструкторе у View еще нет размеров.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width=getWidth()/CELL_SIZE;
        int height=getHeight()/CELL_SIZE;
        if(mGame==null)
            mGame = new Game(width, height);
    }

    // рисуем на канве поле и живые клетки
    @Override
    protected void onDraw(Canvas canvas) {
        Paint background = new Paint();
        // фон
        background.setColor(getResources().getColor(R.color.background_color));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);

        drawBorders(canvas);

        Paint aliveCell=new Paint();
        aliveCell.setColor(getResources().getColor(R.color.cell_color));

        // отрисовка живых клеток
        for(int i=0; i<mGame.getWidth(); i++)
            for(int j=0; j<mGame.getHeight(); j++)
                if(mGame.getGameGrid()[i][j]==Game.ALIVE)
                    drawCell(canvas, i, j);
    }

    // отрисовка ячеек
    private void drawBorders(Canvas canvas)
    {
        Paint line=new Paint();
        line.setColor(getResources().getColor(R.color.line_color));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(1);
        // вертикальные
        for(int i=0; i<mGame.getWidth(); i++)
            canvas.drawLine(i*CELL_SIZE, 0, i*CELL_SIZE, getHeight(), line);
        // горизонтальные
        for(int i=0; i<mGame.getHeight(); i++)
            canvas.drawLine(0, i*CELL_SIZE, getWidth(), i*CELL_SIZE, line);
    }

    // отрисовывем живую клетку
    private void drawCell(Canvas canvas, int x, int y)
    {
        Paint aliveCell=new Paint();
        aliveCell.setColor(getResources().getColor(R.color.cell_color));

        canvas.drawRect(x * CELL_SIZE,
                y * CELL_SIZE,
                (x * CELL_SIZE) + (CELL_SIZE - 2),
                (y * CELL_SIZE) + (CELL_SIZE - 2),
                aliveCell);
    }

    public void start()
    {
        if(mCurrentMode!=Mode.RUNNING)
        {
            mCurrentMode=Mode.RUNNING;
            update();
        }
    }

    public void pause()
    {
        if(mCurrentMode!=Mode.PAUSE)
            mCurrentMode=Mode.PAUSE;
    }

    public void reset()
    {
        if(mCurrentMode!=Mode.PAUSE)
            mCurrentMode=Mode.PAUSE;

            mGame.reset();
            invalidate();
    }

    public void randomField()
    {
        reset();
        mGame.generateRandomField();
        invalidate();
    }

    private void update()
    {
        if(mGame!=null)
            mGame.nextGeneration();

        if(!mGame.isContainsAlive())
            mCurrentMode=Mode.PAUSE;

        mRedrawHandler.sleep(mDelay);
    }

    public void nextGeneration()
    {
        if(mCurrentMode!=Mode.PAUSE)
            mCurrentMode=Mode.PAUSE;

        update();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x=(int)event.getX()/CELL_SIZE;
        int y=(int)event.getY()/CELL_SIZE;
        mGame.changeCellStatus(x, y);
        invalidate();
        return super.onTouchEvent(event);
    }
}
