package com.example.AudioServoTester;

import AudioServo.AudioPPM_8ch;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import AudioServo.AudioPPM;

public class MainActivity extends AppCompatActivity {
    public static int SERVO_MIDPOINT = 72;
    public static int SERVO_LOWPOINT = 48;
    public static int SERVO_HIGHPOINT = 96;

    public static int ppmNum = 18;
    AudioPPM ap;
    int[] ppmSamples;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ppmSamples = new int[ppmNum];
        ap = new AudioPPM();
        SetButtonListener();
        GenerateSliders(ppmNum);
    }

    public void SetButtonListener() {
        Switch sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ap.EnableOutput();
                } else {
                    ap.DisableOutput();
                }
            }
        });
    }

    public void GenerateSliders(int controlCount){
        final TableLayout tl = findViewById(R.id.tl);
        //Use base 1 for widget indexing because Android UI likes it apparently
        for (int i = 0; i < controlCount+0; i++){

            TableRow tableRow = new TableRow(this);
            tableRow.setMinimumHeight(95);

            TextView tvPos = new TextView(this);
            tvPos.setText(String.valueOf(SERVO_MIDPOINT));
            tvPos.setId(i*4+1);

            SeekBar seekBar = new SeekBar(this);
            seekBar.setMin(SERVO_LOWPOINT);
            seekBar.setMax(SERVO_HIGHPOINT);
            seekBar.setProgress(SERVO_MIDPOINT);
            seekBar.setId(i*4+2);
            seekBar.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    UpdateSliders(seekBar, progress, fromUser);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //Not Used
                }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //Not used
                }
            });

            String indexString = String.valueOf(i);
            TextView tvIndex = new TextView(getApplicationContext());
            tvIndex.setText(indexString);

            tableRow.addView(tvIndex);
            tableRow.addView(seekBar, 900, 80);
            tableRow.addView(tvPos);
            tl.addView(tableRow);

            //Initialize corresponding array to default value of SERVO_MIDPOINT;
            ppmSamples[i] = SERVO_MIDPOINT;
        }
    }

    public void UpdateSliders(SeekBar seekBar, int progress, boolean fromUser){
        int idNum = seekBar.getId();
        int index = (int)Math.floor(idNum / 4);
        ppmSamples[index] = progress;
        TextView tv = findViewById(idNum-1);
        tv.setText(String.valueOf(progress));
        ap.WritePPM(ppmSamples);
    }
}