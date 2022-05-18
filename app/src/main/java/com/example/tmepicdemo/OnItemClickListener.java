package com.example.tmepicdemo;

import android.view.View;

public interface OnItemClickListener {
    void onItemClick(int position);
    void onItemLongClick(int position);
    void onItemCheckBoxClick(View view, int position);
}
