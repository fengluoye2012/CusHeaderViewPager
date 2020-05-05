package com.fly.cusheaderviewpager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView tvCustomLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCustomLinearLayout = findViewById(R.id.tv_custom_linearLayout);
        tvCustomLinearLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
