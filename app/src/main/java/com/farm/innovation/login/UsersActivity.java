package com.farm.innovation.login;

import android.os.Bundle;
import android.widget.TextView;

import com.farm.innovation.base.BaseActivity;
import com.innovation.pig.insurance.R;


public class UsersActivity extends BaseActivity {

    private TextView textViewName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.farm_activity_users);


    }

    @Override
    protected int getLayoutId() {
        return R.layout.farm_activity_users;
    }

    @Override
    protected void initData() {
        textViewName = (TextView) findViewById(R.id.text1);
        String nameFromIntent = getIntent().getStringExtra("EMAIL");
        textViewName.setText("Welcome " + nameFromIntent);
    }
}
