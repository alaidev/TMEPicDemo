package com.example.tmepicdemo;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class TitleBar  {

    private TextView textView;
    private ImageView leftImageView;
    private Button button;

    public TitleBar(View root) {
        textView = root.findViewById(R.id.title_text);
        leftImageView = root.findViewById(R.id.leftImageView);
        button = root.findViewById(R.id.title_button);
    }

    public void setTitleText(String text) {
        textView.setText(text);
    }

    public void showRightButton(boolean isShow) {
        button.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setRightButtonText(String text) {
        button.setText(text);
    }

    public void setRightButtonOnClick(View.OnClickListener onClick) {
        button.setOnClickListener(onClick);
    }

    public void setLeftImageViewOnClick(View.OnClickListener onClick) {
        leftImageView.setOnClickListener(onClick);
    }

    public void showLeftImageView(boolean isShow) {
        leftImageView.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
