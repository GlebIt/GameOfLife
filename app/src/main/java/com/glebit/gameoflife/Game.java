package com.glebit.gameoflife;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Random;

/**
 * Created by Itenberg on 25.05.2016.
 */
public class Game extends Observable{

    private byte[][] mGameGrid;
    private int mDelay=100;
    private Mode mCurrentMode=Mode.PAUSE;
    private int mHeight=5;
    private int mWidth=5;
    private ArrayList<LifeObserver> subscribers=new ArrayList<>();

    public static final int ALIVE=1;
    public static final int DEAD=0;

    // игровое поле
    public byte[][] getGameGrid() {
        return mGameGrid;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setGameGrid(byte[][] gameGrid) {
        mGameGrid = gameGrid;
        setChanged();
        notifyObservers(gameGrid);
    }

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public Mode getCurrentMode() {
        return mCurrentMode;
    }

    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            if(mCurrentMode==Mode.RUNNING)
                update();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    public void changeSize(int newWidth, int newHeight)
    {
        byte[][] newGrid=new byte[newWidth][newHeight];

        System.arraycopy(mGameGrid, 0, newGrid, 0, mGameGrid.length);

        for(int i=0; i<newGrid.length; i++)
        newGrid[i]=Arrays.copyOf(newGrid[i], newHeight);

        mWidth=newWidth;
        mHeight=newHeight;
        mGameGrid=newGrid;
        gridChanged();
    }

    public Game(int width, int height)
    {
        mWidth=width;
        mHeight=height;
        mGameGrid=new byte[mWidth][mHeight];
        gridChanged();
    }

    private void gridChanged() {
        setChanged();
        notifyObservers(mGameGrid);
    }

    // сбросить поле
    public void reset()
    {
        pause();
        mGameGrid=new byte[mWidth][mHeight];
        gridChanged();
    }

    public void oneStepForward()
    {
        pause();
        nextGeneration();
    }

    public void start()
    {
        if(mCurrentMode!=Mode.RUNNING)
            changeMode(Mode.RUNNING);

        update();
    }

    public void pause()
    {
        if(mCurrentMode!=Mode.PAUSE)
            changeMode(Mode.PAUSE);
    }

    private void update()
    {
        if(!isContainsAlive())
            changeMode(Mode.PAUSE);
        else {
            nextGeneration();
            mRedrawHandler.sleep(mDelay);
        }
    }

    public boolean isContainsAlive()
    {
        for(int i=0; i<mWidth; i++)
            for(int j=0; j<mHeight; j++)
                if(mGameGrid[i][j]==ALIVE)
                    return true;

        return false;
    }

    // создаем следующие поколение
    private void nextGeneration()
    {
        byte[][] newGenerationGrid=new byte[mWidth][mHeight];
        int neighborsCount=0;
        for(int i=0; i<mWidth; i++)
            for(int j=0; j<mHeight; j++)
            {
                neighborsCount=countNeighbors(i, j);
                if(neighborsCount==3)
                    newGenerationGrid[i][j]=ALIVE;
                else if(neighborsCount<2 || neighborsCount>3)
                    newGenerationGrid[i][j]=DEAD;
                else
                    newGenerationGrid[i][j]=mGameGrid[i][j];
            }
        mGameGrid=newGenerationGrid;
        gridChanged();
    }

    // считаем соседей(возможно есть способ лучше?)
    private int countNeighbors(int x, int y)
    {
        int count=0;

        int xp1 = (x+1)%mWidth;
        int xm1 = (x-1)==-1 ? mWidth-1 : x-1;
        int yp1 = (y+1)%mHeight;
        int ym1 = (y-1)==-1 ? mHeight-1 : y-1;
        count=mGameGrid[xp1][y]==ALIVE?count+1:count;
        count=mGameGrid[xp1][yp1]==ALIVE?count+1:count;
        count=mGameGrid[xp1][ym1]==ALIVE?count+1:count;
        count=mGameGrid[x][yp1]==ALIVE?count+1:count;
        count=mGameGrid[x][ym1]==ALIVE?count+1:count;
        count=mGameGrid[xm1][y]==ALIVE?count+1:count;
        count=mGameGrid[xm1][yp1]==ALIVE?count+1:count;
        count=mGameGrid[xm1][ym1]==ALIVE?count+1:count;

        return count;
    }

    // рандомно заполняем поле
    public void generateRandomField()
    {
        reset();
        int fieldSize=mWidth*mHeight;
        // min ~10% от общего количества клеток
        int min=(int)Math.round(fieldSize*0.1);
        // max ~90%  от общего количества клеток
        int max=fieldSize-(int)Math.round(fieldSize*0.1);

        Random rnd=new Random();
        int count=rnd.nextInt(max-min)+min;
        int x=0, y=0;
        for(int i=0; i<count; i++)
        {
            x=rnd.nextInt(mWidth);
            y=rnd.nextInt(mHeight);
            makeCellAlive(x, y);
        }
        gridChanged();
    }

    // изменяем статус клетки
    public void changeCellStatus(int x, int y)
    {
        if(mGameGrid[x][y]==ALIVE)
            mGameGrid[x][y]=DEAD;
        else if(mGameGrid[x][y]==DEAD)
            mGameGrid[x][y]=ALIVE;

        gridChanged();
    }

    public void makeCellAlive(int x, int y)
    {
        mGameGrid[x][y]=ALIVE;
    }

    public void subscribe(LifeObserver observer)
    {
        subscribers.add(observer);
    }

    public void removeObserver(LifeObserver observer)
    {
        subscribers.remove(observer);
    }

    private void changeMode(Mode newMode)
    {
        mCurrentMode=newMode;
        for(LifeObserver o:subscribers)
            o.onStatusChanged(newMode);
    }
}
