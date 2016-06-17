package com.glebit.gameoflife;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Itenberg on 25.05.2016.
 */
public class LifeGrid extends View implements Observer
{
    private static final int CELL_SIZE=16;

    private float mActualCellWidth;
    private float mActualCellHeight;

    private int mWidth =40;
    private int mHeight =40;
    private byte[][] fieldArray=new byte[40][40];

    private float mPosX;
    private float mPosY;
    private float mScaleFactor = 1.f;
    private Canvas mCanvas;

    public float getActualCellWidth() {
        return mActualCellWidth;
    }

    public float getActualCellHeight() {
        return mActualCellHeight;
    }

    @Override
    public void update(Observable observable, Object data)
    {
        if(data instanceof byte[][])
        {
            fieldArray = (byte[][]) data;
            mWidth =((byte[][]) data).length;
            mHeight =((byte[][]) data)[0].length;
            this.invalidate();
        }
    }

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
        // теперь размер ячеек меняется
        mActualCellWidth=CELL_SIZE+(getWidth()- mWidth *CELL_SIZE)/(float) mWidth;
        mActualCellHeight=CELL_SIZE+(getHeight()- mHeight *CELL_SIZE)/(float) mHeight;
    }

    public Rect getGridRect()
    {
        return mCanvas.getClipBounds();
    }

    // рисуем на канве поле и живые клетки
    @Override
    protected void onDraw(Canvas canvas) {
        Paint background = new Paint();
        // фон
        background.setColor(getResources().getColor(R.color.background_color));
        canvas.drawRect(0, 0, getWidth(), getHeight(), background);
        canvas.save();

        // двигаем грид только если он увеличен
        if(mScaleFactor>1)
            canvas.translate(mPosX, mPosY);

        // увеличиваем
        canvas.scale(mScaleFactor, mScaleFactor);

        drawBorders(canvas);

        Paint aliveCell=new Paint();
        aliveCell.setColor(getResources().getColor(R.color.cell_color));

        // отрисовка живых клеток
        for(int i=0; i<fieldArray.length; i++)
            for(int j=0; j<fieldArray[i].length; j++)
                if(fieldArray[i][j]==Game.ALIVE)
                    drawCell(canvas, i, j);

        mCanvas=canvas;
    }

    // отрисовка ячеек
    private void drawBorders(Canvas canvas)
    {
        Paint line=new Paint();
        line.setColor(getResources().getColor(R.color.line_color));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(1);
        // вертикальные
        mActualCellWidth=(float)getWidth()/fieldArray.length;
        mActualCellHeight=(float)getHeight()/fieldArray[0].length;
        for(int i=0; i<fieldArray.length; i++)
            canvas.drawLine(i*mActualCellWidth, 0, i*mActualCellWidth, getHeight(), line);
        // горизонтальные
        for(int i=0; i<fieldArray[0].length; i++)
            canvas.drawLine(0, i*mActualCellHeight, getWidth(), i*mActualCellHeight, line);
    }

    // отрисовывем живую клетку
    private void drawCell(Canvas canvas, int x, int y)
    {
        Paint aliveCell=new Paint();
        aliveCell.setColor(getResources().getColor(R.color.cell_color));

        canvas.drawRect(x * mActualCellWidth,
                        y * mActualCellHeight,
                        (x * mActualCellWidth) + (mActualCellWidth - 2),
                        (y * mActualCellHeight) + (mActualCellHeight - 2),
                                                                aliveCell);
    }

    // вызываем при маштабировании или движении
    public void onChanged(float posX, float posY, float scaleFactor)
    {
        float dx=getWidth()*mScaleFactor-getWidth();
        float dy=getHeight()*mScaleFactor-getHeight();

        if(posX<0 && posX>-dx)
            mPosX=posX;
        if(posY<0 && posY>-dy)
            mPosY=posY;

        mScaleFactor=scaleFactor;
        invalidate();
    }
}
