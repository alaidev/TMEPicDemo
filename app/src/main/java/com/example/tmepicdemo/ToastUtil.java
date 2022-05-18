package com.example.tmepicdemo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Context context;

    public static void init(Context mContext) {
        context = mContext;
    }

    public static void showLong(String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void showShort(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
