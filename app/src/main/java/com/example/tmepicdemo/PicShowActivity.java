package com.example.tmepicdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PicShowActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private static int WIDTH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //透明状态栏
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_pic_show);
        initView();
    }

    private void initView() {
        viewPager = findViewById(R.id.view_pager);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        PicAdapter picAdapter = new PicAdapter(intent.getStringArrayListExtra("images"), this);
        viewPager.setAdapter(picAdapter);
        viewPager.setCurrentItem(position);
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        WIDTH = point.x;
    }

    public static class PicAdapter extends PagerAdapter {

        List<String> listView;
        Context mContext;
        Map<Integer, View> map = new HashMap<>();

        public PicAdapter(List<String> list, Context context) {
            this.listView = list;
            mContext = context;
        }

        @Override
        public int getCount() {
            return listView.size();
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(map.get(position));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(mContext);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(listView.get(position), options);
            options.inSampleSize = Utils.calSampleSize(options, WIDTH, WIDTH);
            options.inJustDecodeBounds = false;
            imageView.setImageBitmap(BitmapFactory.decodeFile(listView.get(position), options));
            map.put(position, imageView);
            container.addView(imageView, 0);
            return imageView;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}