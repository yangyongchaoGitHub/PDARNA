package com.dataexpo.rna;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.dataexpo.rna.common.DBUtils;
import com.dataexpo.rna.common.HttpCallback;
import com.dataexpo.rna.common.HttpService;
import com.dataexpo.rna.common.URLs;
import com.dataexpo.rna.common.Utils;
import com.dataexpo.rna.pojo.MsgBean;
import com.dataexpo.rna.pojo.SignData;
import com.google.gson.Gson;
import com.idata.fastscandemo.R;
import com.zhy.http.okhttp.request.RequestCall;

import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class UploadActivity extends BascActivity implements View.OnClickListener {
    private static final String TAG = UploadActivity.class.getSimpleName();

    private static final int UPLOAD_WAIT = 0;
    private static final int UPLOAD_ING = 1;
    private static final int UPLOAD_STOP = 2;
    private Context mContext;
    private TextView tv_total;
    private TextView tv_success;
    private TextView tv_wait;
    private TextView tv_upload_warning;
    private List<SignData> datas;
    private String expo_id;
    private String address;
    private HttpCallback callback;
    private int success;
    private int wait;
    private int total;
    private volatile int uploadStatus = UPLOAD_STOP;

    private long lastChange = System.currentTimeMillis();

    private HashMap<Integer, SignData> uploadMap = new HashMap<>();

    private int rid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_upload);
        initView();
        initData();
        //tryUpload();
    }

    private void initData() {
        String usage_mode = Utils.getUsageMode(mContext);
        if ("offline".equals(usage_mode)) {
            datas = DBUtils.getInstance().listAllOffLine();
        } else {
            datas = DBUtils.getInstance().listAllOnLine();
        }
        success = 0;
        total = wait = datas.size();
        tv_total.setText(String.valueOf(total));
        tv_success.setText("0");
        tv_wait.setText(String.valueOf(wait));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            expo_id = bundle.getString("Expo_id");
            address = bundle.getString("Add");
        }
    }

    private void initView() {
        tv_total = findViewById(R.id.tv_upload_scan_total_value);
        tv_success = findViewById(R.id.tv_upload_success_total_value);
        tv_wait = findViewById(R.id.tv_upload_wait_total_value);
        tv_upload_warning = findViewById(R.id.tv_upload_warning_text);
        tv_upload_warning.setOnClickListener(this);
        findViewById(R.id.btn_stop_upload).setOnClickListener(this);
        findViewById(R.id.btn_upload_check_expo_back).setOnClickListener(this);
    }

    private void tryUpload() {
        if (datas.size() == 0) {
            return;
        }
        if (uploadStatus == UPLOAD_ING) {
            return;
        }
        uploadStatus = UPLOAD_ING;
        tv_upload_warning.setText(R.string.upload_no_close);

        new Thread(new Runnable() {
            @Override
            public void run() {
                upLoadData();
                Log.i(TAG, "upload exit!!!!!!");
                //uploadStop();
            }
        }).start();
    }

    private void uploadStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_upload_warning.setText(R.string.upload_success);
                UploadActivity.this.finish();
                uploadEnd();
            }
        });
    }

    private void upLoadData() {
        final String url = URLs.offLineUploadCT;

        String serial = Utils.getSerialNumber();

        for (SignData data: datas) {
            if (uploadStatus == UPLOAD_STOP) {
                break;
            }
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("expoId", expo_id);
            hashMap.put("address", address);

            //Log.i(TAG, "eucode: " + data.getEucode() + " time: " + data.getTime() + " name: " +  data.getName()  + "idcard:" +  data.getIdcard() );

            if (null != data.getEucode() && !"".equals(data.getEucode()) && !"null".equals(data.getEucode())) {
                //Log.i(TAG, "----------------------- eucode " + data.getEucode());
                hashMap.put("eucode", data.getEucode());
            }
            if (null != data.getTime() && !"".equals(data.getTime()) && !"null".equals(data.getTime())) {
                hashMap.put("time", data.getTime() + "");
            }
            if (null != data.getName() && !"".equals(data.getName()) && !"null".equals(data.getName())) {
                hashMap.put("name", data.getName() + "");
            }
            if (null != data.getIdcard() && !"".equals(data.getIdcard()) && !"null".equals(data.getIdcard())) {
                hashMap.put("idcard", data.getIdcard() + "");
            }
            if (null != data.getAddress() && !"".equals(data.getAddress()) && !"null".equals(data.getAddress())) {
                hashMap.put("useraddress", data.getAddress() + "");
            }
            if (null != data.getTemp() && !"".equals(data.getTemp()) && !"null".equals(data.getTemp())) {
                hashMap.put("temperature", data.getTemp() + "");
            }
            if (null != data.getModeType()) {
                hashMap.put("modeType", data.getModeType() + "");
            }
            hashMap.put("deviceKey", serial + "");

            //Log.i(TAG, "euCode: " + data.getEucode() + " euFileCode: " + data.getIdcard() + " " + hashMap.get("eucode"));
            //synchronized (this) {
                //HttpService.postWithParams(mContext, url, hashMap, callback);
                RequestCall call = HttpService.postWithParams(mContext, url, hashMap, ++rid, new HttpCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                    }
                    @Override
                    public void onResponse(String response, final int id) {
                        //Log.i(TAG, "onResponse id : " + id);
                        final MsgBean result = new Gson().fromJson(response, MsgBean.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                --wait;

                                if (result.code == 200) {

                                    success++;
                                    SignData saveData = uploadMap.get(id);
                                    //Log.i(TAG, "result 200 " + id + " saveData : " + saveData);

                                    if (saveData != null) {
                                        //Log.i(TAG, "saveData id " + saveData.getId());
                                        DBUtils.getInstance().delData(saveData.getId());
                                    }
                                } else {
                                    //tv_success.setText(String.valueOf(++success));
                                    //tv_wait.setText(String.valueOf(++wait));
                                    //Toast.makeText(mContext, "上传失败id: " + data.eufilecode, Toast.LENGTH_SHORT).show();
                                }

                                if (System.currentTimeMillis() - lastChange > 500 || wait < 100) {
                                    tv_wait.setText(String.valueOf(wait));
                                    tv_success.setText(String.valueOf(success));
                                    lastChange = System.currentTimeMillis();
                                }

                                //Log.i(TAG, "total:" + total + " success:" + success + " wait: " + wait);
                                if(wait == 0){
                                    Log.i(TAG, "------------------- total:" + total + " success:" + success + " wait: " + wait);
                                    uploadEnd();
                                }
                            }});
                    }
                });
                uploadMap.put(rid, data);

            //}
        }
    }

    private void uploadEnd() {
        tv_wait.setText(String.valueOf(wait));
        tv_success.setText(String.valueOf(success));
        tv_upload_warning.setText(R.string.upload_success);
        UploadActivity.this.finish();

        uploadStatus = UPLOAD_STOP;
        Intent intent = new Intent();

        intent.putExtra("success_count", success);
        intent.setClass(mContext, UploadSuccessActivity.class);

        startActivity(intent);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (datas != null && datas.size() > 0) {
                tryUpload();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_upload_warning_text:
                tryUpload();
                break;

            case R.id.btn_stop_upload:
                uploadStatus = UPLOAD_STOP;
                break;
            case R.id.btn_upload_check_expo_back:
                finish();
                break;
            default:
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        uploadStatus = UPLOAD_STOP;
        super.onResume();
    }

    private static class UploadAnsync extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
}