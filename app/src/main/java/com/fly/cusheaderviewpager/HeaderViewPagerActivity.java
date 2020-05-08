package com.fly.cusheaderviewpager;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.fly.headerviewpager.HeaderViewPager;

public class HeaderViewPagerActivity extends AppCompatActivity {

    private HeaderViewPager headerViewPager;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_view_pager);

        headerViewPager = findViewById(R.id.headerViewPager);
        viewPager = findViewById(R.id.viewPager);


    }
}
