package com.example.aghwavegenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private boolean isPlay = false;
    int count = 0;
    double freqHz = 300;
    double sampleDurationMs = 500;
    AudioTrack audio;
    int minFrequency = 10;
    int maxFrequency = 24000;
    boolean notUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playBtn = findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlay) {
                    startAudio();
                }
            }
        });

        Button stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
            }
        });

        Button decrementFrequencyBtn = findViewById(R.id.decrementFrequency);
        decrementFrequencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFrequency(freqHz - 1, true);
            }
        });

        Button decrementFrequency2Btn = findViewById(R.id.decrementFrequency2);
        decrementFrequency2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFrequency(freqHz - 10, true);
            }
        });

        Button incrementFrequencyBtn = findViewById(R.id.incrementFrequency);
        incrementFrequencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFrequency(freqHz + 1, true);
            }
        });

        Button incrementFrequency2Btn = findViewById(R.id.incrementFrequency2);
        incrementFrequency2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFrequency(freqHz + 10, true);
            }
        });

        Button applyFrequencyBtn = findViewById(R.id.applyFrequency);
        applyFrequencyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText frequencyInput = findViewById(R.id.frequencyInput);
                updateFrequency(Integer.parseInt(frequencyInput.getText().toString()));
            }
        });

        SeekBar frequencySlide = findViewById(R.id.frequencySlide);
        updateFrequencyLabel();

        frequencySlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double progressChanged = minFrequency;

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(notUpdate) {
                    notUpdate = false;
                    return;
                }
                progressChanged = minFrequency + progress;
                updateFrequency(progressChanged);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    Runnable waveGenerator = new Runnable()
    {
        public void run()
        {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            short[] samples = new short[count];
            double factor;
            int length = samples.length;
            while(isPlay) {
                factor = 2 * Math.PI / (44100 / freqHz);
                for(int i = 0; i < count; i++) {
                    samples[i] = (short)(Math.sin(factor * i) * 0x7FFF);
                }


                audio.write(samples, 0, length);
            }
        }
    };

    void startAudio()
    {
        isPlay = true;

        count = (int)(44100 * 2.0 * (sampleDurationMs / 1000.0)) & ~1;
        audio = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                count * 2,
                AudioTrack.MODE_STREAM
        );

        audio.play();

        Thread audioThread = new Thread(waveGenerator);
        audioThread.start();
    }

    void stopAudio()
    {
        isPlay = false;
        try {
            audio.pause();
//            audio.stop();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    void updateFrequency(double frequency){
        updateFrequency(frequency, false);
    }

    void updateFrequency(double frequency, boolean updateSlide){
        if(frequency < minFrequency) frequency = minFrequency;
        if(frequency > maxFrequency) frequency = maxFrequency;
        freqHz = frequency;

        updateFrequencyLabel();
        if(updateSlide) {
            notUpdate = true;
            SeekBar frequencySlide = findViewById(R.id.frequencySlide);
            frequencySlide.setProgress((int)freqHz);
        }
    }

    void updateFrequencyLabel() {
        TextView frequencyTxt = findViewById(R.id.frequencyTxt);
        frequencyTxt.setText(String.format(Locale.ENGLISH,"%d Hz", (int)freqHz));
    }

}
