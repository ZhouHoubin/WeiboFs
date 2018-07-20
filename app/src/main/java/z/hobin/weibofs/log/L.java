package z.hobin.weibofs.log;

import android.util.Log;

public class L {

    public static void i(String msg) {
        Log.i("Weibo", msg);
    }

    public static void d(String msg) {
        Log.d("Weibo", msg);
    }

    public static void d(String tag, String msg) {
        Log.d("Weibo", tag + ":" + msg);
    }

    public static void e(String msg) {
        Log.e("Weibo", msg);
    }

    public static void e(Exception e) {
        Log.e("Weibo", e.getMessage());
    }

    public static void e(String tag, Exception e) {
        Log.e(tag, e.getMessage());
    }

    public static void e(String tag, String msg) {
        Log.e("Weibo", tag + ":" + msg);
    }
}
