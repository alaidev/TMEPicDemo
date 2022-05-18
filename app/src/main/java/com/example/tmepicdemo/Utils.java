package com.example.tmepicdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Utils {
    public static void saveImage(Context context, Bitmap bitmap) {
        String fileName = String.valueOf(System.currentTimeMillis()) + ".png";
        //存储的路径
        //设置路径 Pictures/
        String folder = Environment.DIRECTORY_PICTURES;
        //设置保存参数到ContentValues中
        ContentValues values = new ContentValues();
        //设置图片名称
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        //设置图片格式
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        //设置图片路径
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, folder);
        }
        //执行insert操作，向系统文件夹中添加文件
        //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        OutputStream os = null;
        String path;
        try {
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                os = context.getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                path = uri.getPath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void requestPermissions(Activity activity, String[] permissions) {
        final int REQUEST_CODE = 10001;
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                //  GRANTED---授权  DINIED---拒绝
                if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
                }
            }
        }
    }

    @SuppressLint({"Range", "Recycle"})
    public static void getPic(Activity activity, List<Image> imageDatas, List<String> images) {
        imageDatas.clear();
        images.clear();
        long timeStart = System.currentTimeMillis() - 60 * 60 * 24 * 1000 * 14;
        Cursor cursor = activity.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null,  null, null);
        while (cursor.moveToNext()) {
            //获取图片的路径
            String location = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.d("ImgActivity: ", "initImages: " + "imageLocation: " + location);
            File file = new File(location);
            long time = file.lastModified();
            if (time < timeStart) continue;

            //获取图片的名称
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));
            Log.d("ImgActivity: ", "initImages: " + "imageName: " + name);

            //获取图片的详细信息
            String desc = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION));
            imageDatas.add(new Image(name, null, desc, location, time, 0));
        }
    }

    public static int calSampleSize(BitmapFactory.Options options, int dstWidth, int dstHeight) {
        int rawWidth = options.outWidth;
        int rawHeight = options.outHeight;
        int inSampleSize = 1;
        if (rawWidth > dstWidth || rawHeight > dstHeight) {
            float ratioHeight = (float) rawHeight / dstHeight;
            float ratioWidth = (float) rawWidth / dstHeight;
            inSampleSize = (int) Math.min(ratioWidth, ratioHeight);
        }
        return inSampleSize;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String stampToDate(long lt) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(lt);
        return simpleDateFormat.format(date);
    }

    public static Bitmap getImgFromDesc(Activity activity, String path) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int WIDTH = point.x;
        Bitmap bm = null;
        File file = new File(path);
        if(file.exists()) {
            try {
                int width = (WIDTH / 3) - dip2px(activity, 3);
                InputStream is = new FileInputStream(path);
                BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder
                        .newInstance(is, false);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(path, options);
                int w = options.outWidth;
                int h = options.outHeight;
                options.inSampleSize = calSampleSize(options, width, width);
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(path, options);
                w = bm.getWidth();
                h = bm.getHeight();
                bm =  Bitmap.createBitmap(bm, Math.max((w - width) / 2, 0), Math.max((h - width) / 2, 0), Math.min(width, w), Math.min(width, h), null, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            ToastUtil.showShort("该图片不存在！");
            Log.d("ImgActivity ", "getImgFromDesc: 该图片不存在！");
        }
        return bm;
    }
}
