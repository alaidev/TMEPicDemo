package com.example.tmepicdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TitleBar titleBar;
    private List<Image> imageDatas;
    private List<String> images;
    private boolean selected = false;
    private List<String> selectedPic;
    private mHandler handler;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private Activity mainActivityContext;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //透明状态栏
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mainActivityContext = this;
        handler = new mHandler();
        ToastUtil.init(this);
        titleBar = new TitleBar(findViewById(R.id.title_bar));
        titleBar.setTitleText("相册");
        titleBar.setRightButtonText("确定");
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setIndeterminate(false);//循环滚动
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("loading...");
        progressDialog.setCancelable(false);//false不能取消显示，true可以取消显示
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        Adapter adapter = new Adapter(this);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(MainActivity.this, PicShowActivity.class);
                intent.putExtra("images", (Serializable) images);
                intent.putExtra("position", images.indexOf(imageDatas.get(position).getLocation()));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(int position) {
                selectedPic.clear();
                selected = true;
                titleBar.showLeftImageView(true);
                titleBar.showRightButton(true);
                recyclerView.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onItemCheckBoxClick(View view, int position) {
                CheckBox checkBox = (CheckBox) view;
                if (checkBox.isChecked()) {
                    selectedPic.add(imageDatas.get(position).getLocation());
                } else {
                    selectedPic.remove(imageDatas.get(position).getLocation());
                }
            }

        });
        titleBar.setLeftImageViewOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected = false;
                titleBar.showLeftImageView(false);
                titleBar.showRightButton(false);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
        titleBar.setRightButtonOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPic.size() != 5) {
                    ToastUtil.showShort("请选择5张图片");
                }
                int height = 0, width = 0;
                List<Bitmap> list = new ArrayList<>();
                for (String s : selectedPic) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(s, options);
                    height += options.outHeight;
                    width = Math.max(width, options.outWidth);
                    options.inJustDecodeBounds = false;
                    list.add(BitmapFactory.decodeFile(s, options));
                }
                Bitmap longBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(longBmp);
                height = 0;
                for (Bitmap b : list) {
                    canvas.drawBitmap(b, 0.0f, height, null);
                    height += b.getHeight();
                }
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.saveImage(mainActivityContext, longBmp);
                        Message message = Message.obtain();
                        message.what = 1;
                        message.obj = "success";
                        handler.sendMessage(message);
                    }
                }).start();

            }
        });
        recyclerView.setAdapter(adapter);
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        Utils.requestPermissions(mainActivityContext, permissions);
        initImages();
    }

    private class mHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                progressDialog.dismiss();
                initImages();
                selected = false;
                titleBar.showLeftImageView(false);
                titleBar.showRightButton(false);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void initImages() {
        selectedPic = new ArrayList<>();
        imageDatas = new ArrayList<>();
        images = new ArrayList<>();
        Utils.getPic(mainActivityContext, imageDatas, images);
        Collections.sort(imageDatas, (o1, o2) -> (int) (o2.getTime() - o1.getTime()));
        List<Image> t = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        long preTimeDate = -1;
        for (Image image : imageDatas) {
            calendar.setTime(new Date(image.getTime()));
            int tDate = calendar.get(Calendar.DATE);
            if (tDate != preTimeDate) {
                preTimeDate = tDate;
                t.add(new Image(null, null, null, null, image.getTime(), 1));
            }
            t.add(image);
            images.add(image.getLocation());
        }
        imageDatas.clear();
        imageDatas.addAll(t);
    }

    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Context mContext;
        OnItemClickListener onItemClickListener;

        public Adapter(Context context) {
            mContext = context;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
                return new ViewHolder(view);
            }
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_time, parent, false);
            return new TimeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
            if (imageDatas.get(position).getType() == 0) {
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.checkBox.setVisibility(selected ? View.VISIBLE : View.GONE);
                viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("ImgActivity ", "getImgFromDesc: " + position);
                        if (onItemClickListener != null) onItemClickListener.onItemClick(position);
                    }
                });
                viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (onItemClickListener != null) onItemClickListener.onItemLongClick(position);
                        return true;
                    }
                });
                viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onItemClickListener != null) onItemClickListener.onItemCheckBoxClick(v, position);
                    }
                });
                if (imageDatas.get(position).getBitmap() == null) {
                    viewHolder.imageView.setTag(imageDatas.get(position).getLocation());
                    viewHolder.imageView.post(new Runnable() {
                        @Override
                        public void run() {
                            if(imageDatas.get(position).getLocation().equals(viewHolder.imageView.getTag())) {
                                imageDatas.get(position).setBitmap(Utils.getImgFromDesc(mainActivityContext, imageDatas.get(position).getLocation()));
                                viewHolder.imageView.setImageBitmap(imageDatas.get(position).getBitmap());
                            }
                        }
                    });
                } else {
                    viewHolder.imageView.setImageBitmap(imageDatas.get(position).getBitmap());
                }
            } else {
                TimeViewHolder timeViewHolder = (TimeViewHolder) holder;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date(System.currentTimeMillis()));
                int today = calendar.get(Calendar.DATE);
                calendar.setTime(new Date(imageDatas.get(position).getTime()));
                if (calendar.get(Calendar.DATE) == today) {
                    timeViewHolder.textView.setText("今天");
                } else if (calendar.get(Calendar.DATE) == today - 1) {
                    timeViewHolder.textView.setText("昨天");
                } else {
                    timeViewHolder.textView.setText(Utils.stampToDate(imageDatas.get(position).getTime()));
                }
            }
        }

        @Override
        public int getItemCount() {
            return imageDatas.size();
        }

        @Override
        public int getItemViewType(int position) {
            return imageDatas.get(position).getType();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                final GridLayoutManager gridManager = ((GridLayoutManager) manager);
                gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (getItemViewType(position) == 0) {
                            return 1;
                        } else {
                            return 3;
                        }

                    }
                });
            }
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image_view);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    private static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.time_text_view);
        }
    }
}