package com.glebit.gameoflife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements LifeObserver {

    private final static int DEFAULT_WIDTH=40;
    private final static int DEFAULT_HEIGHT=40;
    private final static int MAXIMUM_TAP_TIME=400;

    protected ImageView mImgStart;
    protected ImageView mImgReset;
    protected ImageView mImgNext;
    protected ImageView mImgRandom;
    protected ImageView mImgSave;
    protected ImageView mImgOpen;
    protected LifeGrid mLifeGrid;
    protected SeekBar mSpeedBar;
    protected TextView mSpeedText;

    protected Game mGame;

    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    private ScaleGestureDetector mScaleDetector;
    private float mPreviousScaleFactor=1.f;
    private float mScaleFactor = 1.f;
    private float mMinScaleFactor = 1.f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImgStart =(ImageView)findViewById(R.id.imgStart);
        mImgReset =(ImageView)findViewById(R.id.imgReset);
        mImgNext =(ImageView)findViewById(R.id.imgNext);
        mImgRandom =(ImageView)findViewById(R.id.imgRandom);
        mImgSave =(ImageView)findViewById(R.id.imgSave);
        mImgOpen =(ImageView)findViewById(R.id.imgOpen);
        mLifeGrid=(LifeGrid)findViewById(R.id.lifeGrid);
        mSpeedBar=(SeekBar)findViewById(R.id.speedBar);
        mSpeedText=(TextView)findViewById(R.id.speedTextView);

        mScaleDetector = new ScaleGestureDetector(this, new ScaleListener());
        mLifeGrid.requestFocus();
        mLifeGrid.setOnTouchListener(lifeGridOnTouchListener);

        mImgStart.setOnClickListener(imgStartOnClickListener);
        mImgReset.setOnClickListener(imgResetOnClickListener);
        mImgNext.setOnClickListener(imgNextOnClickListener);
        mImgRandom.setOnClickListener(imgRandomOnClickListener);
        mImgSave.setOnClickListener(imgSaveOnClickListener);
        mImgOpen.setOnClickListener(imgOpenOnClickListener);
        mSpeedBar.setOnSeekBarChangeListener(speedBarChangeListener);
        mGame=new Game(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        mGame.addObserver(mLifeGrid);
        mGame.subscribe(this);
        mSpeedText.setText(getString(R.string.text_view_speed_text)+mGame.getDelay()/1000);
    }

    View.OnTouchListener lifeGridOnTouchListener=new View.OnTouchListener() {
        private  long startTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(event.getAction()==MotionEvent.ACTION_UP) {
                long dt=event.getEventTime()-event.getDownTime();
                // не очень хорошо
                if(dt<=MAXIMUM_TAP_TIME) {
                    int x = (int) (event.getX() / mLifeGrid.getActualCellWidth());
                    int y = (int) (event.getY() / mLifeGrid.getActualCellHeight());
                    // если увеличено, приводим координаты.
                    if (mScaleFactor > 1) {
                        x = (int) ((event.getX() / mScaleFactor + mLifeGrid.getGridRect().left) / mLifeGrid.getActualCellWidth());
                        y = (int) ((event.getY() / mScaleFactor + mLifeGrid.getGridRect().top) / mLifeGrid.getActualCellHeight());
                    }
                    mGame.changeCellStatus(x, y);
                }
            }

            mScaleDetector.onTouchEvent(event);

            final int action = event.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    mActivePointerId = event.getPointerId(0);
                    break;
                }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    if(mScaleFactor<1)
                        mScaleFactor=1;

                    mLifeGrid.onChanged(mPosX, mPosY, mScaleFactor);
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                            >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    final int pointerId = event.getPointerId(pointerIndex);
                    if (pointerId == mActivePointerId) {
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchX = event.getX(newPointerIndex);
                        mLastTouchY = event.getY(newPointerIndex);
                        mActivePointerId = event.getPointerId(newPointerIndex);
                    }
                    break;
                }
            }
            return true;
        }
    };

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 5.0f));

            // если увеличили больше 1, то просто увеличиваем
            // если увеличили относительно текущего состояния(меньше 1), увеличиваем на дельту
            // если уменьшили, меняем разер поля. После изменения размера - грид перерусуется
            if(mScaleFactor>=1)
                mLifeGrid.onChanged(mPosX, mPosY, mScaleFactor);
            else{
                if(mScaleFactor>mMinScaleFactor){
                    float scale=1.f+ Math.abs(mPreviousScaleFactor-mScaleFactor);
                    mLifeGrid.onChanged(0, 0, scale);
                }
                else{
                    int newSize = 40 + (40 - Math.round(40 * mScaleFactor));
                    mGame.changeSize(newSize, newSize);
                    mMinScaleFactor=mScaleFactor;
                }
            }
            mPreviousScaleFactor=mScaleFactor;
            return true;
        }
    }

    SeekBar.OnSeekBarChangeListener speedBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            // in seconds
            float value=(float)(progress/10.0);
            mGame.setDelay((int)(value*1000));
            mSpeedText.setText(getString(R.string.text_view_speed_text)+value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    View.OnClickListener imgOpenOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOpenDialog();
        }
    };

    View.OnClickListener imgSaveOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openSaveDialog();
        }
    };

    View.OnClickListener imgRandomOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGame.generateRandomField();
        }
    };

    View.OnClickListener imgNextOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGame.oneStepForward();
        }
    };

    View.OnClickListener imgResetOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mGame.reset();
        }
    };

    View.OnClickListener imgStartOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mGame.getCurrentMode()==Mode.RUNNING)
                mGame.pause();
            else if(mGame.getCurrentMode()==Mode.PAUSE)
                mGame.start();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void showOpenDialog()
    {
        final ArrayList<String> items=StorageAdapter.getConfigs(this);
        CharSequence[] cItems=items.toArray(new CharSequence[items.size()]);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.open_dialog_title))
                .setItems(cItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selected = items.get(which);
                        byte[][] field = StorageAdapter.loadPreset(selected, MainActivity.this);
                        mGame.setGameGrid(field);
                    }
                })
                .setNegativeButton(getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void openSaveDialog()
    {
        LayoutInflater li=LayoutInflater.from(this);
        View saveDialogView=li.inflate(R.layout.save_dialog, null);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setView(saveDialogView);

        final EditText userInput = (EditText) saveDialogView
                .findViewById(R.id.edtSaveName);

        builder.setCancelable(false)
                .setTitle(getString(R.string.save_dialog_title))
                .setPositiveButton(getString(R.string.ok_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = userInput.getText().toString();
                        StorageAdapter.savePreset(mGame.getGameGrid(), name, MainActivity.this);
                    }
                })
                .setNegativeButton(getString(R.string.cancel_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog=builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent intent=new Intent(this, AboutActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStatusChanged(Mode status)
    {
        if(status==Mode.RUNNING)
            mImgStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        else if(status==Mode.PAUSE)
            mImgStart.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
    }
}
