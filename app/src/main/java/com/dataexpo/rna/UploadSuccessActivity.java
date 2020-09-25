package com.dataexpo.rna;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.idata.fastscandemo.R;

public class UploadSuccessActivity extends BascActivity implements View.OnClickListener {
    private int success;
    private TextView tv_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_success);
        initView();
        initData();
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            success = bundle.getInt("success_count");
            tv_text.setText(String.format(getResources().getString(R.string.upload_success_value),success));
        }
    }

    private void initView() {
        findViewById(R.id.tv_upload_success_bottom_exit).setOnClickListener(this);
        tv_text = findViewById(R.id.tv_upload_success_text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_upload_success_bottom_exit:
                finish();
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return false;
        }
        return super.dispatchKeyEvent(event);
    }
}
