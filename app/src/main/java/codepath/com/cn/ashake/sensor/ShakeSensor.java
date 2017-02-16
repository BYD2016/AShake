package codepath.com.cn.ashake.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


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
