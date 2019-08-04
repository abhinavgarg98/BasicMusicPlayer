package com.example.abhinav.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Uri audioFileUri;
    MediaPlayer mediaPlayer;
    ImageButton playBtn, likeBtn, forbtn, backBtn, repeatBtn, shuffleBtn;
    SeekBar seekBar;
    int repeatCount = 0;
    boolean likedCount, isRepeat, shuffleCount;
    TextView passTime, remainTime,songName;
    int currentTime;
    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //find all
        songName=findViewById(R.id.name);
        shuffleBtn = findViewById(R.id.shuffle);
        repeatBtn = findViewById(R.id.repeat);
        passTime = findViewById(R.id.passed_time);
        remainTime = findViewById(R.id.remain_time);
        forbtn = findViewById(R.id.forward);
        backBtn = findViewById(R.id.backward);
        likeBtn = findViewById(R.id.like);
        FloatingActionButton addSong = findViewById(R.id.addsong);
        seekBar = findViewById(R.id.seekBar);
        playBtn = findViewById(R.id.play);
        handler = new Handler();


        //add a song listener
        addSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBtn.setImageResource(R.drawable.play);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/mpeg");
                startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
                seekBar.setProgress(0);
                currentTime = 0;

            }
        });

        //play btn listener
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer == null) {
                    Toast.makeText(MainActivity.this, "Add a song", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.seekTo(currentTime);
                        mediaPlayer.start();
                        changeSeekbar();
                        playBtn.setImageResource(R.drawable.pause);
                    } else if (mediaPlayer.isPlaying()) {
                        currentTime = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        mediaPlayer.pause();
                        playBtn.setImageResource(R.drawable.play);
                    }
                }
            }
        });

        //Forward Button
        forbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSeekbar();
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
            }
        });

        //backward button
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSeekbar();
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
            }
        });

        //like button
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!likedCount) {
                    likeBtn.setImageResource(R.drawable.liked);
                    likedCount = true;
                } else {
                    likeBtn.setImageResource(R.drawable.like);
                    likedCount = false;
                }
            }
        });

        //seekbar listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mediaPlayer.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //on complete song
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if (!isRepeat) {
                        mediaPlayer.seekTo(0);
                        playBtn.setImageResource(R.drawable.play);
                    } else {
                        mediaPlayer.seekTo(0);
                        mediaPlayer.start();
                    }
                }
            });
        }

        //repeat button
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRepeat && repeatCount == 0) {
                    repeatBtn.setImageResource(R.drawable.repeatonce);
                    repeatCount = 1;
                    isRepeat = true;
                } else if (isRepeat && repeatCount == 1) {
                    repeatBtn.setImageResource(R.drawable.repeaton);
                    repeatCount = 0;
                } else if (isRepeat && repeatCount == 0) {
                    repeatBtn.setImageResource(R.drawable.repeat);
                    isRepeat = false;
                    repeatCount = 0;
                }
            }
        });

        //shuffle button
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!shuffleCount) {
                    shuffleBtn.setImageResource(R.drawable.shuffleon);
                    shuffleCount = true;
                } else {
                    shuffleBtn.setImageResource(R.drawable.shuffle);
                    shuffleCount = false;
                }
            }
        });
    }

    //time create
    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    //intent result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                audioFileUri = data.getData();
                Cursor returnCursor =
                        getContentResolver().query(audioFileUri, null, null, null, null);
                /*
                 * Get the column indexes of the data in the Cursor,
                 * move to the first row in the Cursor, get the data,
                 * and display it.
                 */
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                songName.setText(returnCursor.getString(nameIndex));

                mediaPlayer = MediaPlayer.create(MainActivity.this, audioFileUri);
                seekBar.setMax(mediaPlayer.getDuration());
                changeSeekbar();
                remainTime.setText(createTimeLabel(mediaPlayer.getDuration()));
            }
        }
    }

    //update seekbar and time
    private void changeSeekbar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                    passTime.setText(createTimeLabel(mediaPlayer.getCurrentPosition()));
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }
}

