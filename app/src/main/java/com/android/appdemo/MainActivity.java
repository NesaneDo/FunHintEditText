package com.android.appdemo;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FunHintEditText funHintEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        funHintEditText=findViewById(R.id.fun_hint_edit_text);

        funHintEditText.setOnHintValueClick(new FunHintEditText.IOnHintValueClickListener() {
            @Override
            public void onHintClick(View v) {
                funHintEditText.setBgClickableRegion(Color.BLACK);
                funHintEditText.setFgClickableRegion(Color.CYAN);
                funHintEditText.update();
                Toast.makeText(MainActivity.this,funHintEditText.getHintValue(),Toast.LENGTH_LONG).show();
            }
        });
    }
}
