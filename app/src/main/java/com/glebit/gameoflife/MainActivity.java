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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    protected Button mButtonStart;
    protected Button mButtonPause;
    protected Button mButtonReset;
    protected Button mButtonNext;
    protected Button mButtonRandom;
    protected Button mButtonSave;
    protected Button mButtonOpen;
    protected LifeGrid mLifeGrid;
    protected SeekBar mSpeedBar;
    protected TextView mSpeedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonStart=(Button)findViewById(R.id.btnStart);
        mButtonPause=(Button)findViewById(R.id.btnPause);
        mButtonReset=(Button)findViewById(R.id.btnReset);
        mButtonNext=(Button)findViewById(R.id.btnNext);
        mButtonRandom=(Button)findViewById(R.id.btnRandom);
        mButtonSave=(Button)findViewById(R.id.btnSave);
        mButtonOpen=(Button)findViewById(R.id.btnOpen);
        mLifeGrid=(LifeGrid)findViewById(R.id.lifeGrid);
        mSpeedBar=(SeekBar)findViewById(R.id.speedBar);
        mSpeedText=(TextView)findViewById(R.id.speedTextView);

        mLifeGrid.requestFocus();

        mButtonStart.setOnClickListener(btnStartOnClickListener);
        mButtonPause.setOnClickListener(btnPauseOnClickListener);
        mButtonReset.setOnClickListener(btnResetOnClickListener);
        mButtonNext.setOnClickListener(btnNextOnClickListener);
        mButtonRandom.setOnClickListener(btnRandomOnClickListener);
        mButtonSave.setOnClickListener(btnSaveOnClickListener);
        mButtonOpen.setOnClickListener(btnOpenOnClickListener);
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

    View.OnClickListener btnOpenOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showOpenDialog();
        }
    };

    View.OnClickListener btnSaveOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            openSaveDialog();
        }
    };

    View.OnClickListener btnRandomOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.randomField();
        }
    };

    View.OnClickListener btnNextOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.nextGeneration();
        }
    };

    View.OnClickListener btnResetOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mLifeGrid.reset();
        }
    };

    View.OnClickListener btnStartOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
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
        CharSequence[] cItmes=items.toArray(new CharSequence[items.size()]);

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.open_dialog_title))
                .setItems(cItmes, new DialogInterface.OnClickListener() {
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
}
