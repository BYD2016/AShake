package codepath.com.cn.ashake.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import codepath.com.cn.ashake.R;

/**
 *  管理摇一摇结果会话框
 *    1.显示摇中的奖品
 * Created by admin on 2017/2/16.
 */

public final class ShakeResultDialogManager {

    private static Dialog sShakeResultDialog;

    public static void showShakeResultDialog(Context context) {

        if (sShakeResultDialog == null) {
            ShakeResultDialogManager.prepare(context);
        }

        try {
            sShakeResultDialog.show();
        } catch (Exception e) {
            ShakeResultDialogManager.prepare(context);
            sShakeResultDialog.show();
        }

    }

    static void  prepare(Context context) {
        sShakeResultDialog = new Dialog(context, R.style.style_shake_result_dialog);
        View rootView = LayoutInflater.from(context).inflate(R.layout.dialog_shake_result, null);
        sShakeResultDialog.setContentView(rootView);
        sShakeResultDialog.setCanceledOnTouchOutside(true);
    }


    public static boolean isShowing() {
        if (sShakeResultDialog != null) {
            return sShakeResultDialog.isShowing();
        } else {
            return false;
        }
    }


    public static void dismissDialog() {
        if (sShakeResultDialog != null) {
            sShakeResultDialog.dismiss();
        }
    }
}
