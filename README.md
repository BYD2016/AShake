# 演示摇啊摇

## 设置访问权限
在AndroidManifest.xml文件中，添加:
``` xml
<uses-permission android:name="android.permission.VIBRATE" />
```
## 借助加速度传感器感知用户摇动

```java
/**
 *借助加速度传感器感知用户摇动
 * Created by admin on 2017/2/16.
 */

public final class ShakeSensor implements SensorEventListener {

    private static final String TAG = ShakeSensor.class.getSimpleName();
    private static final double VOLACITY_SHRESHOLD = 880.0;

    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private long mLastShaleTtimeMillis;
    private float mLastX, mLastY, mLastZ;

    private OnShakeListener mOnShakeListener;

    public ShakeSensor(Context context){
        mContext = context;
        init();
    }

    public void setOnShakeListener(OnShakeListener onShakeListener) {
        mOnShakeListener = onShakeListener;
    }

    private void init() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 注册传感器
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unregisterSensorListener(){
        if (mSensorManager != null && mSensor != null) {
            mSensorManager.unregisterListener(this, mSensor);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        long curTime = System.currentTimeMillis();

        // 两次摇动的时间间隔
        long shakeTimeInterval = curTime - mLastShaleTtimeMillis;

        if (shakeTimeInterval > 100) {
            // 当前的值
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // 速度的阀值
            double volacity;

            // 当x/y/z达到一定值后进行操作
            float absDeltaValue = Math.abs(x + y + z - mLastX - mLastY - mLastZ);

            volacity = absDeltaValue / shakeTimeInterval * 10000;

            if(volacity > VOLACITY_SHRESHOLD){
                Log.d(TAG, "volacity > VOLACITY_SHRESHOLD");

                if(null != mOnShakeListener){
                    Log.d(TAG, "null != mOnShakeListener");
                    mOnShakeListener.onShake();
                }
            }

            mLastX = x;
            mLastY = y;
            mLastZ = z;

            mLastShaleTtimeMillis = curTime;

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // empty
    }

    public interface OnShakeListener{
        void onShake();
    }

}

```

## 主Activity

### 感知到摇动回调

``` java
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
```

### 获取震动服务

```java
mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
```

### 震动
```java
private void startVibrator() {
        if (mVibrator.hasVibrator()) {
            mVibrator.cancel();
            mlastStartVibratorMillis = System.currentTimeMillis();
            long pattern[] = {0, 300, 500, 300}; // 间隔多长时间震动
            mVibrator.vibrate(pattern, -1);
        }
    }
```

### 播放提示音与震动
```java
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
```

# 演示界面
![演示界面](./doc/mainUI.png)

#  修订 2017-02-16

  >MediaPlayer主要用于播放比较长的音视频，短音频采用SoundPool
  >更好，这样节省资源且延时少。

```java
 private void startAudioWithVibrator2() {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    mSoundPool.play(mSoundId, 0.5f, 0.5f, 0, 0, 1.0f);
                }
            });

            mSoundId = mSoundPool.load(this, R.raw.entervoice, 1);
        } else {
            mSoundPool.play(mSoundId, 0.5f, 0.5f, 0, 0, 1.0f);
        }

        mTxtCount.setText(String.format("今天还剩下%d次", --sShakeCount));
        startVibrator();

 }
```

