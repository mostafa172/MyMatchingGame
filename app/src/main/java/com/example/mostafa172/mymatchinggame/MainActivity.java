package com.example.mostafa172.mymatchinggame;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class MainActivity extends Activity {

    static float scale;
    static int pixels;

    boolean firstPress = true;
    ImageView currentView = null;
    ImageView oldView = null;

    boolean gameStarted = false;
    CountUpTimer timer;

    static int[] drawable = new int[] {
            R.drawable.sample_0,
            R.drawable.sample_1,
            R.drawable.sample_2,
            R.drawable.sample_3
    };
    int[] pos = {0,1,2,3,0,1,2,3};
    Boolean[] truePositions = {false, false, false, false, false, false, false, false};
    int currentPos = -1;

    Button restartButton;
    TextView timerTextView, scoreTextView;
    int scoreOldValue, secs;

    public static void shuffleArray(int[] tempArr){
        Random rand = new Random();

        for (int i = 0; i < tempArr.length; i++) {
            int randomIndexToSwap = rand.nextInt(tempArr.length);
            int temp = tempArr[randomIndexToSwap];
            tempArr[randomIndexToSwap] = tempArr[i];
            tempArr[i] = temp;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // lock portrait mode

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); // remove status bar

        restartButton = (Button) findViewById(R.id.restartButton);

        timerTextView = (TextView) findViewById(R.id.timerTextView);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);

        scale = this.getResources().getDisplayMetrics().density;
        pixels = (int) (150 * MainActivity.scale + 0.5f);

        shuffleArray(pos); //shuffling drawable elements positions

        //loading grids
        myPhotoAdapter photoAdapter = new myPhotoAdapter(this);
        final GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(photoAdapter);

        //logic
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(firstPress && !truePositions[position]){ //First Image Press
                    System.out.println("FIRST PRESS");
                    //Start Timer
                    firstPress = false;
                    currentView = (ImageView) view;
                    currentView.setImageResource(drawable[pos[position]]);
                    currentPos = position;
                    if(!gameStarted){
                        gameStarted = true;
                        timer = new CountUpTimer(100000) {
                            public void onTick(int second) {
                                timerTextView.setText(String.valueOf(second));
                            }
                        };
                        timer.start();
                    }
                }
                else if(!firstPress){
                    oldView = currentView;
                    currentView = (ImageView) view;
                    oldView.setId(R.id.oldView);
                    currentView.setId(R.id.currentView);
                    if((pos[currentPos] == pos[position]) && ((oldView.getId() != currentView.getId())) ){ //Image Match
                        System.out.println("MATCH");
                        currentView.setImageResource(drawable[pos[position]]);
                        truePositions[currentPos] = true;
                        truePositions[position] = true;
                        currentView.setEnabled(false);
                        oldView.setEnabled(false);
                        firstPress = true;
                        scoreOldValue = Integer.parseInt(scoreTextView.getText().toString());
                        secs = Integer.parseInt(timerTextView.getText().toString());
                        scoreTextView.setText(scoreOldValue + (100/(secs+100)*100));
                        if (!(Arrays.asList(truePositions).contains(false))) { //Win situation
                            Toast.makeText(MainActivity.this, "You Win!", Toast.LENGTH_LONG).show();
                            timer.cancel();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    gridView.setEnabled(false);
                                    gridView.setVisibility(View.INVISIBLE);
                                }
                            }, 1000);
                        }
                    }
                    else if(pos[currentPos] != pos[position]){ //Not Matching
                        System.out.println("MISMATCH");
                        truePositions[currentPos] = false;
                        currentView.setImageResource(drawable[pos[position]]);
                        Toast.makeText(MainActivity.this, "Not Match!", Toast.LENGTH_LONG).show();
                        gridView.setEnabled(false);
                        Handler handler = new Handler();
                        scoreOldValue = Integer.parseInt(scoreTextView.getText().toString());
                        scoreTextView.setText(scoreOldValue - 50);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                oldView.setImageResource(R.drawable.unknown);
                                currentView.setImageResource(R.drawable.unknown);
                                firstPress = true;
                                gridView.setEnabled(true);
                            }
                        }, 1000);
                    }
                    else{
                        System.out.println("ANA HENA");//Do nothing
                    }
                }

            }
        });

        //Restart on Click
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

    }
}

// class handling the photos
class myPhotoAdapter extends BaseAdapter {
    private final Context context;

    public myPhotoAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 8;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(this.context);
            imageView.setLayoutParams(new GridView.LayoutParams(MainActivity.pixels, MainActivity.pixels));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else imageView = (ImageView)convertView;
        imageView.setImageResource(R.drawable.unknown);
        return imageView;
    }
}

// Count Up Timer
abstract class CountUpTimer extends CountDownTimer {
    private static final long INTERVAL_MS = 1000;
    private final long duration;

    protected CountUpTimer(long durationMs) {
        super(durationMs, INTERVAL_MS);
        this.duration = durationMs;
    }

    public abstract void onTick(int second);

    @Override
    public void onTick(long msUntilFinished) {
        int second = (int) ((duration - msUntilFinished) / 1000);
        onTick(second);
    }

    @Override
    public void onFinish() {
        onTick(duration / 1000);
    }
}