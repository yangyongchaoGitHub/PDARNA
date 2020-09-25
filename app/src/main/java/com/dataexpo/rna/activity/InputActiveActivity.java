package com.dataexpo.rna.activity;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dataexpo.rna.BascActivity;
import com.dataexpo.rna.common.HttpCallback;
import com.dataexpo.rna.common.HttpService;
import com.dataexpo.rna.common.URLs;
import com.dataexpo.rna.pojo.MsgBean;
import com.google.gson.Gson;
import com.idata.fastscandemo.R;
import com.idata.ise.scanner.decoder.CamDecodeAPI;
import com.idata.ise.scanner.decoder.DecodeResult;
import com.idata.ise.scanner.decoder.DecodeResultListener;

import java.util.HashMap;

import okhttp3.Call;

import static com.dataexpo.rna.activity.MainActivity.INPUT_TOO_LONG;
import static com.dataexpo.rna.common.Utils.INPUT_HAVE_NET_ADDRESS;
import static com.dataexpo.rna.common.Utils.INPUT_NULL;
import static com.dataexpo.rna.common.Utils.INPUT_SUCCESS;


public class InputActiveActivity extends BascActivity implements View.OnClickListener, DecodeResultListener {
    private final String TAG = InputActiveActivity.class.getSimpleName();
    private Context mContext;
    private EditText et_a_code;
    private String expo_id;
    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_input_active);
        initView();
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);
        soundMap.put(1, soundPool.load(mContext, R.raw.rescan, 1)); //播放的声音文件
        initData();
        CamDecodeAPI.getInstance(mContext)
                .SetOnDecodeListener(this);
    }

    private void initData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            expo_id = bundle.getString("Expo_id");
        }
        Log.i(TAG, "expo_id:" + expo_id);
    }

    private void initView() {
        et_a_code = findViewById(R.id.et_a_code);
        findViewById(R.id.btn_input_login_back).setOnClickListener(this);
        findViewById(R.id.tv_login_query).setOnClickListener(this);
    }

    private void playSound() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    soundPool.play(soundMap.get(1), 1, // 左声道音量
                            1, // 右声道音量
                            1, // 优先级，0为最低
                            0, // 循环次数，0无不循环，-1无永远循环
                            1 // 回放速度 ，该值在0.5-2.0之间，1为正常速度
                    );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
       /* if (!et_phone_or_qrcode.hasFocus()) {
            et_phone_or_qrcode.requestFocus();
        }*/
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_input_login_back:
                this.finish();
                break;

            case R.id.tv_login_query:
                if ("".equals(et_a_code.getText().toString())) {
                    CamDecodeAPI.getInstance(mContext).ScanBarcode(
                            mContext);
                } else {

                    queryInfo();
                }
                break;
            default:
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            if ("".equals(et_a_code.getText().toString())) {
                CamDecodeAPI.getInstance(mContext).ScanBarcode(
                        mContext);
            } else {
                queryInfo();
            }

            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    private void queryInfo() {
        String code = et_a_code.getText().toString();

        if (checkInput(code) == INPUT_SUCCESS) {
            String url = URLs.checkActive;
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("expoId", expo_id);
            hashMap.put("a_code", code);

            HttpService.getWithParams(mContext, url, hashMap, new HttpCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                        }
                    });

                    Log.i(TAG, e.getMessage());
                }

                @Override
                public void onResponse(String response, int id) {
                    final MsgBean userInfo = new Gson().fromJson(response, MsgBean.class);
                    if (userInfo.code == 100) {
                        //激活码不存在
                        Toast.makeText(mContext, "激活码不存在", Toast.LENGTH_SHORT).show();

                    } else if (userInfo.code == 101) {
                        //超过最大使用次数
                        Toast.makeText(mContext, "超过最大使用次数", Toast.LENGTH_SHORT).show();

                    } else if (userInfo.code == 200) {
                        Log.i(TAG, "用户数据查找成功" + response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (userInfo.data == null) {
                                    et_a_code.setText("");

                                    Toast.makeText(mContext,
                                            "用户不存在，请检查输入！！",
                                            Toast.LENGTH_SHORT).show();

                                } else {

                                    Intent intent = new Intent();
                                    intent.putExtra("Expo_id", expo_id);
                                    intent.putExtra("a_code", et_a_code.getText().toString());
                                    intent.setClass(mContext, MainActivity.class);
                                    et_a_code.setText("");
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public static int checkInput(String input) {
        if (TextUtils.isEmpty(input)) {
            return INPUT_NULL;
        }

        if (input.contains("http")) {
            return INPUT_HAVE_NET_ADDRESS;
        }

        if (input.contains("www.")) {
            return INPUT_HAVE_NET_ADDRESS;
        }

        if (input.length() > 50) {
            return INPUT_TOO_LONG;
        }

        return INPUT_SUCCESS;
    }

    @Override
    public void onDecodeResult(DecodeResult decodeResult) {
        if (null != decodeResult){
            //扫码完成返回
            //manager.playSoundAndVibrate(true, false);
            String code = new String(decodeResult.getBarcodeData());

            if (INPUT_SUCCESS == checkInput(code)) {
                et_a_code.setText(code);
            } else {
                Toast.makeText(this, "扫描内容异常", Toast.LENGTH_SHORT).show();
            }
        }else {
        }
    }
}
