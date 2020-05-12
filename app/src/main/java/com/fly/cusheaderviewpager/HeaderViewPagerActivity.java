package com.fly.cusheaderviewpager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import com.fly.headerviewpager.CusViewPager;
import com.fly.headerviewpager.HeaderViewPager;

public class HeaderViewPagerActivity extends AppCompatActivity {

    private HeaderViewPager headerViewPager;
    private CusViewPager viewPager;
    private ItemFragment itemFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_header_view_pager);

        TextView textView = findViewById(R.id.textView);
        headerViewPager = findViewById(R.id.headerViewPager);
        viewPager = findViewById(R.id.viewPager);
        itemFragment = new ItemFragment();

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

            @Override
            public int getCount() {
                return 1;
            }

            @NonNull
            @Override
            public Fragment getItem(int position) {
                return itemFragment;
            }
        });

        viewPager.setCurrentItem(0);
        headerViewPager.setViewPager(viewPager);
        headerViewPager.setScrollableInterface(itemFragment);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "hhh", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
