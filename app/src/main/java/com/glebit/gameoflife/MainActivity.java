package com.glebit.gameoflife;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements LifeObserver {

    protected ImageView mImgStart;
    protected ImageView mImgReset;
    protected ImageView mImgNext;
    protected ImageView mImgRandom;
    protected ImageView mImgSave;
    protected ImageView mImgOpen;
    protected LifeGrid mLifeGrid;
    protected SeekBar mSpeedBar;
    protected TextView mSpeedText;

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

        mLifeGrid.requestFocus();
        mLifeGrid.subscribe(this);

        mImgStart.setOnClickListener(imgStartOnClickListener);
        mImgReset.setOnClickListener(imgResetOnClickListener);
        mImgNext.setOnClickListener(imgNextOnClickListener);
        mImgRandom.setOnClickListener(imgRandomOnClickListener);
        mImgSave.setOnClickListener(imgSaveOnClickListener);
        mImgOpen.setOnClickListener(imgOpenOnClickListener);
        mSpeedBar.setOnSeekBarChangeListener(speedBarChangeListener);

        mSpeedText.setText(getString(R.string.text_view_speed_text)+mLifeGrid.getDelay()/1000);
    }

    SeekBar.OnSeekBarChangeListener speedBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            // in seconds
            float value=(float)(progress/10.0);
            mLifeGrid.setDelay((int)(value*1000));
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
            mLifeGrid.randomField();
        }
    };

    View.OnClickListener imgNextOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.nextGeneration();
        }
    };

    View.OnClickListener imgResetOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.reset();
        }
    };

    View.OnClickListener imgStartOnClickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //mLifeGrid.start();
            if(mLifeGrid.getCurrentMode()==Mode.RUNNING)
                mLifeGrid.pause();
            else if(mLifeGrid.getCurrentMode()==Mode.PAUSE)
                mLifeGrid.start();
        }
    };

    View.OnClickListener btnPauseOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.pause();
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
                        mLifeGrid.setField(field);
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
                        StorageAdapter.savePreset(mLifeGrid.getField(), name, MainActivity.this);
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
