package com.adara.yashsd.waterlevelindicator;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.github.lzyzsd.circleprogress.DonutProgress;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button b1, b2, b3;
    FloatingActionButton fab;

    //EditText et1, et2, et3;

    TextView tvFlowRate,tvRunTime,tvTime1,tvTime2,tvTime3;

    SeekBar sb1;

    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout infoLayout,linearStartLayout,linearStoptLayout;

    ViewFlipper viewFlipper;

    DonutProgress donutProgress;

    boolean oldFlow = false;
    String oldLevel = "0";

    ObjectAnimator objAni1;
    ObjectAnimator objAni2;
    ObjectAnimator objAni3;

    int height;
    int width;

    final static public String REFRESH = "REFRESH";
    final static public String POWER = "POWER";
    final static public String TIMEFUNCTION = "TIMEFUNCTION";
    final static public String TIMEFUNCTIONFOR = "TIMEFUNCTIONFOR";

    final static public String REFRESHRESULT = "REFRESHRESULT";
    final static public String NORMALUPDATERESULT = "NORMALUPDATERESULT";

    private IntentFilter mIntentfilter;

    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(REFRESHRESULT)) {
                String flowRate = intent.getExtras().getString("flowRate");
                String tankStatus = intent.getExtras().getString("tankStatus");
                String timeStatus = intent.getExtras().getString("timeStatus");
                refreshResult(flowRate, tankStatus, timeStatus);
                int flowRateInt = 0;
                flowRateInt = Integer.parseInt(flowRate);
                if (flowRateInt > 0) {
                    tankAnimate(true, tankStatus);
                } else {
                    tankAnimate(false, tankStatus);
                }
            } else if (intent.getAction().equals(NORMALUPDATERESULT)) {
                String flowRate = intent.getExtras().getString("flowRate");
                String tankStatus = intent.getExtras().getString("tankStatus");
                String timeStatus = intent.getExtras().getString("timeStatus");
                tvFlowRate.setText(flowRate + "L/s");
                tvRunTime.setText(timeStatus + "min");
                donutProgressUpdate(tankStatus);
                int flowRateInt = 0;
                flowRateInt = Integer.parseInt(flowRate);
                if (flowRateInt > 0) {
                    tankAnimate(true, tankStatus);
                } else {
                    tankAnimate(false, tankStatus);
                }
            }/* else if(intent.getAction().equals(Alert_OFF)) {
                ChangeToGreen();
            }else if(intent.getAction().equals(ALL_OFF)) {
                Shutdown();
            }*/
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        donutProgress = (DonutProgress) findViewById(R.id.donut_progress);

        tvFlowRate = (TextView) findViewById(R.id.tvFlowRate);
        tvRunTime = (TextView)findViewById(R.id.tvRunTime);
        tvTime1 = (TextView)findViewById(R.id.tvTime1);
        tvTime2 = (TextView)findViewById(R.id.tvTime2);
        tvTime3 = (TextView)findViewById(R.id.tvTime3);

        final SeekBar sb1 = (SeekBar)findViewById(R.id.sb1);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        linearStartLayout = (LinearLayout)findViewById(R.id.linearStartLayout);
        linearStoptLayout = (LinearLayout)findViewById(R.id.linearStopLayout);

        objAni1 = ObjectAnimator.ofFloat(infoLayout, View.TRANSLATION_X, 0.0f, 100.0f);
        objAni2 = ObjectAnimator.ofFloat(infoLayout, View.TRANSLATION_X, 100.0f, -100.0f);
        objAni3 = ObjectAnimator.ofFloat(infoLayout, View.TRANSLATION_X, -100.0f, 0.0f);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

        //et1 = (EditText) findViewById(R.id.et1);
        //et2 = (EditText) findViewById(R.id.et2);
        //et3 = (EditText) findViewById(R.id.et3);

        b1 = (Button) findViewById(R.id.button1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(i);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(POWER);
                sendBroadcast(broadcastIntent);
            }
        });

        b2 = (Button) findViewById(R.id.button2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*int startTime = Integer.parseInt(et1.getText().toString());
                int stopTime = Integer.parseInt(et2.getText().toString());

                if(et1.getText().length()<4 || et2.getText().length()<4 || et1.getText().length()>4 || et2.getText().length()>4){
                    Toast.makeText(MainActivity.this, "The times should of length 4 only!", Toast.LENGTH_SHORT).show();
                } else if (startTime == stopTime) {
                    Toast.makeText(MainActivity.this, "Start time is equal to stop time!!", Toast.LENGTH_SHORT).show();
                } else if (stopTime < startTime) {
                    Toast.makeText(MainActivity.this, "Start time can not be less than stop time!!", Toast.LENGTH_SHORT).show();
                } else if (startTime > 2400 || stopTime > 2400) {
                    Toast.makeText(MainActivity.this, "Incorrect values of start and stop time", Toast.LENGTH_SHORT).show();
                } else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(TIMEFUNCTION);
                    broadcastIntent.putExtra("starttime", et1.getText().toString());
                    broadcastIntent.putExtra("stoptime", et2.getText().toString());
                    sendBroadcast(broadcastIntent);
                }*/

                int startTime = Integer.parseInt(tvTime1.getText().toString());
                int stopTime = Integer.parseInt(tvTime2.getText().toString());
                if (startTime == stopTime) {
                    Toast.makeText(MainActivity.this, "Start time is equal to stop time!!", Toast.LENGTH_SHORT).show();
                } else if (stopTime < startTime) {
                    Toast.makeText(MainActivity.this, "Start time can not be less than stop time!!", Toast.LENGTH_SHORT).show();
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
                    String str = sdf.format(new Date());

                    Toast.makeText(MainActivity.this,str.toString(), Toast.LENGTH_SHORT).show();

                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(TIMEFUNCTION);
                    broadcastIntent.putExtra("starttime", tvTime1.getText().toString());
                    broadcastIntent.putExtra("stoptime", tvTime2.getText().toString());
                    broadcastIntent.putExtra("currenttime",str.toString());
                    sendBroadcast(broadcastIntent);
                }
            }
        });

        b3 = (Button)findViewById(R.id.button3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*int startForTime = Integer.parseInt(et3.getText().toString());

                if(et3.getText().length()<4 || et3.getText().length()>4){
                    Toast.makeText(MainActivity.this, "The time should of length 4 only!", Toast.LENGTH_SHORT).show();
                } else if(startForTime == 0){
                    Toast.makeText(MainActivity.this, "Cannot Start for 0 mins", Toast.LENGTH_SHORT).show();
                } else if(startForTime <-1){
                    Toast.makeText(MainActivity.this, "Duration cannot be negative", Toast.LENGTH_SHORT).show();
                } else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(TIMEFUNCTIONFOR);
                    broadcastIntent.putExtra("startForTime", et3.getText().toString());
                    sendBroadcast(broadcastIntent);
                }*/
                int startForTime = sb1.getProgress();

                if(startForTime == 0){
                    Toast.makeText(MainActivity.this, "Cannot Start for 0 mins", Toast.LENGTH_SHORT).show();
                } else {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(TIMEFUNCTIONFOR);
                    broadcastIntent.putExtra("startForTime", tvTime3.getText().toString());
                    sendBroadcast(broadcastIntent);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(REFRESH);
                sendBroadcast(broadcastIntent);
            }
        });

        linearStartLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tvTime1.setText(String.format("%02d",hourOfDay) + String.format("%02d",minute));
                    }
                },0,0,false);
                timePickerDialog.show();
            }
        });

        linearStoptLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tvTime2.setText(String.format("%02d",hourOfDay) + String.format("%02d",minute));
                    }
                },0,0,false);
                timePickerDialog.show();
            }
        });

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        ImageView imageView = new ImageView(this);
        imageView = new ImageView(getApplicationContext());
        imageView.setImageResource(R.drawable.flow_zero_tank_0);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxHeight((int) (0.4 * height));
        imageView.setMaxWidth((int) (0.5 * width));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        viewFlipper.addView(imageView);

        mIntentfilter = new IntentFilter();
        mIntentfilter.addAction(REFRESHRESULT);
        mIntentfilter.addAction(NORMALUPDATERESULT);

        registerReceiver(mReciever, mIntentfilter);

        sb1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvTime3.setText(String.format("%04d", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (checkPermission(Manifest.permission.BLUETOOTH) && checkPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    007);
        }
    }

    private boolean checkPermission(String permission) {
        int checkPermission = ContextCompat.checkSelfPermission(this, permission);
        return (checkPermission == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 007: {
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void donutProgressUpdate(String tankStatus) {
        if (tankStatus.equals("error")) {
            Toast.makeText(MainActivity.this, "Error! Check system Hardware!", Toast.LENGTH_LONG).show();
        } else if (tankStatus.equals("0")) {
            donutProgress.setProgress(0f);
        } else if (tankStatus.equals("25")) {
            donutProgress.setProgress(25f);
        } else if (tankStatus.equals("50")) {
            donutProgress.setProgress(50f);
        } else if (tankStatus.equals("75")) {
            donutProgress.setProgress(75f);
        } else if (tankStatus.equals("100")) {
            donutProgress.setProgress(100f);
        } else {
            Toast.makeText(MainActivity.this, "Error! Check system Hardware!", Toast.LENGTH_LONG).show();
            donutProgress.setProgress(0f);
        }
    }

    public void refreshResult(String flowRate, String tankStatus, String timeStatus) {

        int colorFrom = getResources().getColor(R.color.colorWhite);
        int colorTo = getResources().getColor(R.color.colorWaterBlue);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo, colorFrom);
        colorAnimation.setDuration(500); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                infoLayout.setBackgroundColor((int) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();

        swipeRefreshLayout.setRefreshing(false);

        tvFlowRate.setText(flowRate + "L/s");
        tvRunTime.setText(timeStatus + "min");

        donutProgressUpdate(tankStatus);
    }

    void tankAnimate(boolean flow, String level) {
        if (flow != oldFlow || !level.equals(oldLevel)) {
            if (flow) {
                if (level.equals("error")) {
                    oldLevel = "error";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                } else if (level.equals("0")) {
                    oldLevel = "0";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                } else if (level.equals("25")) {
                    oldLevel = "25";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_25_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_one_tank_25_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);

                } else if (level.equals("50")) {
                    oldLevel = "50";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_50_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_one_tank_50_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else if (level.equals("75")) {
                    oldLevel = "75";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_75_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_one_tank_75_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else if (level.equals("100")) {
                    oldLevel = "100";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_one_tank_100_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_one_tank_100_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else {
                    oldLevel = "error";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                }
            } else {
                if (level.equals("error")) {
                    oldLevel = "error";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                } else if (level.equals("0")) {
                    oldLevel = "0";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                } else if (level.equals("25")) {
                    oldLevel = "25";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_25_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_zero_tank_25_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);

                } else if (level.equals("50")) {
                    oldLevel = "50";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_50_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_zero_tank_50_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else if (level.equals("75")) {
                    oldLevel = "75";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_75_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_zero_tank_75_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else if (level.equals("100")) {
                    oldLevel = "100";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_100_1);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);

                    ImageView imageView1 = new ImageView(this);
                    imageView1 = new ImageView(getApplicationContext());
                    imageView1.setImageResource(R.drawable.flow_zero_tank_100_2);
                    imageView1.setAdjustViewBounds(true);
                    imageView1.setMaxHeight((int) (0.4 * height));
                    imageView1.setMaxWidth((int) (0.5 * width));
                    imageView1.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView1);
                } else {
                    oldLevel = "error";
                    viewFlipper.removeAllViews();

                    ImageView imageView = new ImageView(this);
                    imageView = new ImageView(getApplicationContext());
                    imageView.setImageResource(R.drawable.flow_zero_tank_0);
                    imageView.setAdjustViewBounds(true);
                    imageView.setMaxHeight((int) (0.4 * height));
                    imageView.setMaxWidth((int) (0.5 * width));
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    viewFlipper.addView(imageView);
                }
            }
            oldFlow = flow;
        } else {}
    }
}