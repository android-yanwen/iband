package com.manridy.iband.view.toast;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.manridy.iband.R;
public class HrCorrectingResultToast {
    public static HrCorrectingResultToast mToastEmail;
    private Toast toast;

    private HrCorrectingResultToast() {
    }

    public static HrCorrectingResultToast getToastEmail() {
        if (mToastEmail == null) {
            mToastEmail = new HrCorrectingResultToast();
        }
        return mToastEmail;
    }

    /**
     * 显示
     */
    public void ToastShow(Context context, ViewGroup root, String str) {
        View view = LayoutInflater.from(context).inflate(R.layout.hr_correcting_result_layout, root);
        TextView text = (TextView) view.findViewById(R.id.textToast);
        text.setText(str); // 设置显示文字
        toast = new Toast(context);
        toast.setGravity(Gravity.CENTER, 0, 0); // Toast显示的位置
        toast.setDuration(Toast.LENGTH_SHORT); // Toast显示的时间
        toast.setView(view);
        toast.show();
    }

    public void ToastCancel() {
        if (toast != null) {
            toast.cancel();
        }

    }
}