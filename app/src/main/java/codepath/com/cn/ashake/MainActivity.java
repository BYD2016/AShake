package codepath.com.cn.ashake;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import codepath.com.cn.ashake.sensor.ShakeSensor;
import codepath.com.cn.ashake.view.ShakeResultDialogManager;

public class MainActivity extends AppCompatActivity implements ShakeSensor.OnShakeListener {
    private static final String TAG = "MainActivity";

    private static final int MSG_COUNT_END = 0X01; // 次数使用结束
    private static final int MSG_COUNT_CONTINUE = 0X02; // 次数没有结束

    private MediaPlayer mPlayer; // 音乐效果
    private Vibrator mVibrator; // 震动效果

    private ShakeSensor mShakeSensor; // 传感器

    private ImageView mImgHandle; // 视图
    private TextView mTxtCount; // 视图

    private int sShakeCount = 3; // 纪录次数
    private long mlastStartVibratorMillis;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_COUNT_END:
                    startVibrator();
                    break;

                case MSG_COUNT_CONTINUE:
                    // 震动 音乐效果
                    startAudioWithVibrator();

                    // 显示摇动结果
                    ShakeResultDialogManager.showShakeResultDialog(MainActivity.this);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mImgHandle = (ImageView) this.findViewById(R.id.iv_main_handle);
        mTxtCount = (TextView) this.findViewById(R.id.tv_main_net_shake_count);

        mShakeSensor = new ShakeSensor(this);
        mShakeSensor.setOnShakeListener(this);

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 启动动画
        Animation animation = new RotateAnimation(0f, 30f, 50.0f, 100.0f);
        animation.setDuration(500);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        mImgHandle.startAnimation(animation);
    }

    @Override
    protected void onPause() {
        mImgHandle.clearAnimation();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mVibrator.cancel();

        if (mPlayer != null) {
            mPlayer.release();
        }

        mShakeSensor.unregisterSensorListener();
        super.onDestroy();
    }

    @Override
    public void onShake() {

        if (ShakeResultDialogManager.isShowing()) return;
        if (System.currentTimeMillis() - mlastStartVibratorMillis < 1100) return;

        if (sShakeCount == 0) {
            // 次数已经使用完成
            mHandler.sendEmptyMessage(MSG_COUNT_END);
        } else {
            // 还有次数可以摇一摇
            mHandler.sendEmptyMessage(MSG_COUNT_CONTINUE);

            Toast.makeText(this, "摇一摇成功", Toast.LENGTH_SHORT).show();

        }

        // 添加跳转时showActivity进入动画
        //overridePendingTransition(R.anim.main_set_in, 0);

    }

    private void startVibrator() {
        if (mVibrator.hasVibrator()) {
            mVibrator.cancel();
            mlastStartVibratorMillis = System.currentTimeMillis();
            long pattern[] = {0, 300, 500, 300}; // 间隔多长时间震动
            mVibrator.vibrate(pattern, -1);
        }
    }

    private void startAudioWithVibrator() {

        mPlayer = MediaPlayer.create(this, R.raw.entervoice);

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 播放
                mPlayer.start();

            }
        });

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                    mVibrator.cancel();
                    mTxtCount.setText(String.format("今天还剩下%d次", --sShakeCount));
                }
            }
        });

        startVibrator();

    }
}
