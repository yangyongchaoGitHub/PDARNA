package com.dataexpo.rna.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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


public class InputActiveActivity extends BascActivity implements View.OnClickListener, DecodeResultListener, TextWatcher {
    private final String TAG = InputActiveActivity.class.getSimpleName();
    private Context mContext;
    private EditText et_a_code;
    private TextView tv_version;

    private String expo_id;
    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;

    private boolean bResult = true;

    private int exitCount = 0;
    private long exitTime = System.currentTimeMillis();

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
        et_a_code.addTextChangedListener(this);

        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(),0);
            tv_version.setText("V:" + packInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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
        tv_version = findViewById(R.id.tv_version);

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
        if (!et_a_code.hasFocus()) {
            et_a_code.requestFocus();
        }
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
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_DOWN) {

            if (System.currentTimeMillis() - exitTime < 2000) {
                if (++exitCount == 3) {
                    exitTime = System.currentTimeMillis();
                    InputActiveActivity.this.finish();
                }
            } else {
                exitTime = System.currentTimeMillis();
                exitCount = 0;
            }
            return false;
        }

        if ((event.getKeyCode() == 600 || event.getKeyCode() == 601 || event.getKeyCode() == 602) &&
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
        if (!bResult) {
            return;
        }
        bResult = false;

        String code = et_a_code.getText().toString();

        if (checkInput(code) == INPUT_SUCCESS) {
            String url = URLs.checkActive;
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("expoId", expo_id);
            hashMap.put("a_code", code);

            HttpService.getWithParams(mContext, url, hashMap, new HttpCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    bResult = true;
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
                    bResult = true;
                    final MsgBean userInfo = new Gson().fromJson(response, MsgBean.class);
                    if (userInfo.code == 100) {
                        //激活码不存在
                        //Toast.makeText(mContext, "激活码不存在", Toast.LENGTH_SHORT).show();
                        Toast toast = new Toast(InputActiveActivity.this);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        ImageView imageView = new ImageView(InputActiveActivity.this);
                        imageView.setImageResource(R.drawable.code_is_not_fount);
                        LinearLayout linearLayout = new LinearLayout(InputActiveActivity.this);
                        linearLayout.addView(imageView);
//                        TextView tv = new TextView(InputActiveActivity.this);
//                        tv.setTextSize(30);
//                        tv.setText("提示文字");
//                        linearLayout.addView(tv);
                        toast.setView(linearLayout);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();


                    } else if (userInfo.code == 101) {
                        //超过最大使用次数
                        //Toast.makeText(mContext, "超过最大使用次数", Toast.LENGTH_SHORT).show();
                        Toast toast = new Toast(InputActiveActivity.this);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        ImageView imageView = new ImageView(InputActiveActivity.this);
                        imageView.setImageResource(R.drawable.code_is_using);
                        LinearLayout linearLayout = new LinearLayout(InputActiveActivity.this);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.addView(imageView);
                        toast.setView(linearLayout);
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.show();

                    } else if (userInfo.code == 200) {
                        Log.i(TAG, "用户数据查找成功" + response);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //没有取返回数据
                                Intent intent = new Intent();
                                intent.putExtra("Expo_id", expo_id);
                                intent.putExtra("a_code", et_a_code.getText().toString());
                                intent.setClass(mContext, MainActivity.class);
                                et_a_code.setText("");
                                startActivity(intent);
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
                Log.i(TAG, "set Text : " + et_a_code.getSelectionEnd());
                et_a_code.setSelection(et_a_code.getText().toString().length());
            } else {
                Toast.makeText(this, "扫描内容异常", Toast.LENGTH_SHORT).show();
            }
        }else {
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        if (count > 1) {
//            et_a_code.setSelection(et_a_code.getSelectionEnd());
//        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
