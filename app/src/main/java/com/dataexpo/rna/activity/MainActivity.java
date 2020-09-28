package com.dataexpo.rna.activity;

import androidx.annotation.NonNull;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dataexpo.rna.BascActivity;
import com.dataexpo.rna.ScanRecordActivity;
import com.dataexpo.rna.SoundManager;
import com.dataexpo.rna.common.DBUtils;
import com.dataexpo.rna.common.HttpCallback;
import com.dataexpo.rna.common.HttpService;
import com.dataexpo.rna.common.URLs;
import com.dataexpo.rna.common.Utils;
import com.dataexpo.rna.pojo.MsgBean;
import com.dataexpo.rna.pojo.SignData;
import com.dataexpo.rna.readidcard.DynamicPermission;
import com.google.gson.Gson;
import com.idata.fastscandemo.R;
import com.idata.ise.scanner.decoder.CamDecodeAPI;
import com.ivsign.android.IDCReader.IdentityCard;
import com.yishu.YSNfcCardReader.NfcCardReader;
import com.yishu.util.ByteUtil;
import com.zhy.http.okhttp.request.RequestCall;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;

public class MainActivity extends BascActivity implements View.OnClickListener {
    private final static String TAG = MainActivity.class.getName();
    public static final int STATUS_INIT = 0;
    public static final int STATUS_INPUT_CODEORID = 1;
    public static final int STATUS_INPUT_TEMPERATURE = 2;
    public static final int STATUS_INPUT_COMMIT = 3;

    public static final int INPUT_SUCCESS = 0;
    public static final int INPUT_HAVE_NET_ADDRESS = 1;
    public static final int INPUT_ONLY_NUM = 2;
    public static final int INPUT_CHECK_NET_ADDRESS = 3;
    public static final int INPUT_NULL = 4;
    public static final int INPUT_TOO_LONG = 5;

    SoundManager manager;

    private TextView scanStatus;
    //private TextView costTime;
    //private TextView barcodeType;
    private EditText tv_temp_value;
    private TextView tv_code_value;
    private TextView tv_idcard;
    private TextView tv_name_value;
    private TextView tv_sex_value;
    private TextView tv_n_value;
    private TextView tv_create_value;
    private TextView tv_addr_value;
    private TextView tv_plc_value;
    private TextView tv_data_value;
    private ImageView iv_head;
    private ImageView iv_menu;
    private Button btn_scan;
    private Button btn_last;

    private TextView tv_total_value;
    private TextView tv_a_code;

    private int scaning = 0;
    private MyHandler mHandler;
    private static NfcCardReader nfcCardReaderAPI;
    private DynamicPermission dynamicPermission;
    private boolean isActive = false;

    private static Intent thisIntent;

    private volatile int readStatus = notReading;
    private final static  int reading = 1;
    private final static  int notReading = 0;

    private volatile int mStatus = STATUS_INIT;

    private boolean bNFCInput = false;
    private Context mContext = null;
    private String expoId = "";
    private String a_code = "";
    private String onLineAddress = "";
    private HashMap<Integer, SignData> uploadMap = new HashMap<>();
    private int rid = 0;
    private int total = 0;

    private boolean bResult = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MainActivity.this;
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        tv_code_value = findViewById(R.id.tv_code_value);
        tv_temp_value = findViewById(R.id.tv_temp_value);
        tv_idcard = findViewById(R.id.tv_id_value);
        tv_name_value = findViewById(R.id.tv_name_value);
        tv_sex_value = findViewById(R.id.tv_sex_value);
        tv_n_value = findViewById(R.id.tv_n_value);
        tv_create_value = findViewById(R.id.tv_create_value);
        tv_addr_value = findViewById(R.id.tv_addr_value);
        tv_plc_value = findViewById(R.id.tv_plc_value);
        tv_data_value = findViewById(R.id.tv_data_value);
        iv_head = findViewById(R.id.iv_head);
        iv_menu = findViewById(R.id.iv_menu);
        iv_menu.setOnClickListener(this);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(this);
        btn_last = findViewById(R.id.btn_last);
        tv_a_code = findViewById(R.id.tv_active_code);
        btn_last.setOnClickListener(this);

        tv_total_value = findViewById(R.id.tv_total_value);

        manager = new SoundManager(this);
        manager.initSound();
        mHandler = new MyHandler(MainActivity.this);
        nfcCardReaderAPI = new NfcCardReader(mHandler,MainActivity.this);

        //权限判断
        dynamicPermission = new DynamicPermission(MainActivity.this, new DynamicPermission.PassPermission() {
            @Override
            public void operation() {
            }
        });
        dynamicPermission.getPermissionStart();
        mHandler.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCSTART);

        initView();

        tv_temp_value.setHintTextColor(Color.WHITE);

        total = DBUtils.getInstance().countToDay();
        tv_total_value.setText(String.valueOf(total));

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            expoId = bundle.getString("Expo_id");
            a_code = bundle.getString("a_code");
        }
        Log.i(TAG, "expo_id:" + expoId + " a_code: " + a_code);
        tv_a_code.setText(a_code);
    }

    private void initView() {
        tv_name_value.setText("");
        //tv_code_value.setText("");
        tv_idcard.setText("");
        tv_addr_value.setText("");
        tv_a_code.setText("");

        iv_head.setImageDrawable(null);
        mStatus = STATUS_INPUT_CODEORID;
        bNFCInput = false;
        tv_idcard.requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("onPause","enter onPause");
        isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mHandler.getNfcInit()) {
            nfcCardReaderAPI.enabledNFCMessage();
        }

        isActive = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("Info","enter onNewIntent");
        super.onNewIntent(intent);
        thisIntent = intent;
        mHandler.sendEmptyMessage(ByteUtil.MESSAGE_VALID_NFCBUTTON);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown  status: " + mStatus + " --- " + keyCode);
        if (600==keyCode||601==keyCode||602==keyCode) {
            commit();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CamDecodeAPI.getInstance(this).Dispose();
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick  status: " + mStatus);
        switch (v.getId()) {
            case R.id.iv_menu:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final String[] module = {"查看数据", "导出数据到zmtRecord","后台连接设置", "重新设置在线配置", "数据上传", "设备编码：" + Utils.getSerialNumber(), "当前版本:V1.0"};
                //builder.setTitle("选择读取模式");
                //builder.setIcon(R.mipmap.ic_launcher);
                builder.setSingleChoiceItems(module, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case 0:
                                //查看数据
                                startActivity(new Intent(MainActivity.this, ScanRecordActivity.class));
                                break;
                            case 1:
                                //导出数据
                                List<SignData> codes = DBUtils.getInstance().listAllOffLine();
                                if (codes.size() > 0) {
                                    final ZLoadingDialog zdialog = new ZLoadingDialog(mContext);
                                    zdialog.setLoadingBuilder(Z_TYPE.CIRCLE)//设置类型
                                            .setLoadingColor(Color.BLACK)//颜色
                                            .setHintText("正在导出到/sdcard/zmtRecord/...")
                                            .setCanceledOnTouchOutside(false)
                                            .show();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
                                    long dateTime = new Date().getTime();
                                    String date = simpleDateFormat.format(dateTime);
                                    String fileName = date + ".txt";
                                    File file = new File("/sdcard/zmtRecord/");
                                    if (!file.exists()) {
                                        file.mkdirs();
                                    }
                                    file = new File(file, fileName);
                                    try {
                                        FileWriter fw = new FileWriter(file, true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        PrintWriter printWriter = new PrintWriter(bw);
                                        for (SignData code : codes) {
                                            String strContent = (code.getName() == null || "null".equals(code.getName()) ? "" : code.getName()) + "&" +
                                                    (code.getIdcard() == null || "null".equals(code.getIdcard()) ? "" : code.getIdcard()) + "&" +
                                                    (code.getEucode() == null || "null".equals(code.getEucode()) ? "" : code.getEucode()) + "&" +
                                                    (code.getTemp() == null || "null".equals(code.getTemp()) ? "" : code.getTemp()) + "&" +
                                                    (code.getTime() == null || "null".equals(code.getTime()) ? "" : code.getTime());
                                            printWriter.println(strContent);
                                        }
                                        printWriter.close();
                                        bw.close();
                                        fw.close();
                                        zdialog.cancel();
                                        Toast.makeText(mContext, "导出成功, 目录是/sdcard/zmtRecord/", Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        zdialog.cancel();
                                        Toast.makeText(mContext, "导出失败", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, "没有数据可导出", Toast.LENGTH_LONG).show();
                                }
                                break;
                            case 2:

                                break;
                            case 3:

                                break;

                            case 4:
                                //数据上传
                                startActivity(new Intent(mContext, CheckExpoId.class));
                                break;
                            default: break;
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;

            case R.id.btn_scan:
                commit();
                break;
            case R.id.btn_last:
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult request: " +requestCode + " result:" + resultCode);

        if (resultCode == 1 && data != null) {
            expoId = data.getExtras().getString("expoId");
            onLineAddress = data.getExtras().getString("address");
        }
    }

    private void commit() {
        if (!bResult) {
            Log.i(TAG, "bResult 1 " + bResult);
            return;
        }

        Log.i(TAG, "bResult 2 " + bResult);
        String name = tv_name_value.getText().toString();
        String idcard = tv_idcard.getText().toString();

        if ("".equals(name) || "".equals(idcard)) {
            Toast.makeText(this, "请输入身份证数据", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("expoId", expoId);
        hashMap.put("a_code", a_code);
        hashMap.put("name", name + "");
        hashMap.put("idcard", idcard);
        hashMap.put("address", tv_addr_value.getText().toString() + "");
        hashMap.put("cardFace", "");

        final String url = URLs.pdaActiveSignUp;
        bResult = false;

        RequestCall call = HttpService.postWithParams(mContext, url, hashMap, ++rid, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                bResult = true;
                Log.i(TAG, "bResult 3 " + bResult);
                Toast.makeText(mContext, "上传失败", Toast.LENGTH_SHORT).show();
                Log.i(TAG, e.toString());
            }

            @Override
            public void onResponse(String response, final int id) {
                bResult = true;
                Log.i(TAG, "bResult 4 " + bResult);
                Log.i(TAG, "onResponse id : " + id);
                final MsgBean result = new Gson().fromJson(response, MsgBean.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.code == 200) {
                            if (result.data != null) {
                                double r = (double) result.data;
                                if (r == 1) {
                                    Toast toast = new Toast(mContext);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    ImageView imageView = new ImageView(mContext);
                                    imageView.setImageResource(R.drawable.active_success);
                                    LinearLayout linearLayout = new LinearLayout(mContext);
                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.addView(imageView);
                                    toast.setView(linearLayout);
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.show();

                                    //Toast.makeText(mContext, "激活成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast toast = new Toast(mContext);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    ImageView imageView = new ImageView(mContext);
                                    imageView.setImageResource(R.drawable.idcard_is_active);
                                    LinearLayout linearLayout = new LinearLayout(mContext);
                                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                                    linearLayout.addView(imageView);
                                    toast.setView(linearLayout);
                                    toast.setDuration(Toast.LENGTH_SHORT);
                                    toast.show();
                                    //Toast.makeText(mContext, "证件已激活", Toast.LENGTH_SHORT).show();
                                }

                                Intent intent = new Intent();
                                intent.putExtra("Expo_id", expoId);
                                intent.setClass(mContext, InputActiveActivity.class);
                                initView();
                                startActivity(intent);

                                finish();
                            }
                        } else if (result.code == 101) {
                            Toast toast = new Toast(mContext);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            ImageView imageView = new ImageView(mContext);
                            imageView.setImageResource(R.drawable.code_is_using);
                            LinearLayout linearLayout = new LinearLayout(mContext);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            linearLayout.addView(imageView);
                            toast.setView(linearLayout);
                            toast.setDuration(Toast.LENGTH_SHORT);
                            toast.show();
                            Toast.makeText(mContext, "已超过最大激活次数", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, "激活失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //initView();
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

    private static class MyHandler extends Handler{
        private WeakReference<MainActivity> activityWeakReference;
        //蓝牙nfc初始化之后不再初始化
        private boolean nfcInit  = false;
        private boolean btInit = false;

        public boolean getNfcInit() {
            return nfcInit;
        }

        public MyHandler(MainActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final MainActivity activity = activityWeakReference.get();
            if(activity == null){
                return;
            }

            switch (msg.what){
                case ByteUtil.MESSAGE_VALID_NFCSTART:
                    Log.i("Info","enter MESSAGE_VALID_NFCSTART");
                    if(nfcInit){
                        break;
                    }

                    Boolean enabledNFC = false;
                    if(activity.isActive){
                        enabledNFC = nfcCardReaderAPI.enabledNFCMessage();
                    }
                    if(enabledNFC){
                        nfcInit = true;
//                        Toast.makeText(activity,"NFC初始化成功",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(activity,"NFC初始化失败",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ByteUtil.MESSAGE_VALID_BTSTART:
                    Log.i("Info","enter MESSAGE_VALID_BTSTART");

                    break;
                case ByteUtil.BLUETOOTH_CONNECTION_SUCCESS:
                    Log.i("Info","enter BLUETOOTH_CONNECTION_SUCCESS");
                    break;
                case ByteUtil.BLUETOOTH_CONNECTION_FAILED:
                    Log.i("Info","enter BLUETOOTH_CONNECTION_FAILED");
                    Toast.makeText(activity,"设备连接失败，请重新连接",Toast.LENGTH_SHORT).show();
                    break;
                case ByteUtil.MESSAGE_VALID_NFCBUTTON:
                    Log.i("Info","enter MESSAGE_VALID_NFCBUTTON");
                    boolean isNFC = true;
                    if(isNFC){
                        nfcCardReaderAPI.CreateCard(thisIntent);
                    }else{
                        Toast.makeText(activity,"获取nfc失败",Toast.LENGTH_SHORT).show();
                    }
                    thisIntent = null;
                    break;
                case ByteUtil.MESSAGE_VALID_BTBUTTON:
                    Log.i("Info","enter MESSAGE_VALID_BTBUTTON");
                    break;
                case ByteUtil.READ_CARD_START:
                    Log.i("Info","enter READ_CARD_START");
                    //main_tvContent_show.setText("开始读卡，请稍后...");
                    Toast.makeText(activity,"开始读卡",Toast.LENGTH_SHORT).show();
                    break;
                case ByteUtil.READ_CARD_FAILED:
                    Log.e("Info","enter READ_CARD_FAILED");
                    Toast.makeText(activity,"读卡失败，请关闭其他身份证读取软件",Toast.LENGTH_SHORT).show();
                    if(78 !=nfcCardReaderAPI.getErrorFlag()){
                        String message = nfcCardReaderAPI.getMessage();

                        //when mode is bluetooth,message is ""
                        if(!("".equals(message))) {
                            //main_tvContent_show.setText("");
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }

                    break;
                case ByteUtil.READ_CARD_SUCCESS:
                    Log.i("Info","enter READ_CARD_SUCCESS");

                    //if (activity.mStatus == activity.STATUS_INIT || activity.mStatus == activity.STATUS_INPUT_CODEORID) {
                        Toast.makeText(activity, "读卡成功", Toast.LENGTH_SHORT).show();
                        IdentityCard card = (IdentityCard) msg.obj;
                        if (card != null) {
                            final String name = card.getNameText();
                            final String sex = card.getSexText();
                            final String birthday = card.getBirthdayText();
                            final String nation = card.getMingZuText();
                            final String address = card.getAddressText();
                            final String number = card.getNumberText();
                            final String qianfa = card.getQianfaText();
                            final String effdate = card.getEffectiveDate();
                            Bitmap head = card.getImage();

                            if (head == null) {
                                Toast.makeText(activity, "头像读取失败", Toast.LENGTH_SHORT).show();
                            }

                            final Bitmap personImg = card.getImage();

                            Log.i("Info:", name + "\n" + sex + "\n" + birthday + "\n" + nation + "\n" + address + "\n" + number + "\n" + qianfa + "\n" + effdate + "\n");
                            //activity.setData(card);
                            activity.tv_name_value.post(new Runnable() {
                                @Override
                                public void run() {
                                    activity.tv_idcard.setText(number);
                                    activity.tv_name_value.setText(name);
//                                    activity.tv_sex_value.setText(sex);
//                                    activity.tv_n_value.setText(nation);
//                                    activity.tv_create_value.setText(birthday);
                                    activity.tv_addr_value.setText(address);
//                                    activity.tv_plc_value.setText(qianfa);
//                                    activity.tv_data_value.setText(effdate);
                                    activity.iv_head.setImageBitmap(personImg);
                                    //activity.tv_code_value.setText("");
                                    //设置进入测温
                                    activity.mStatus = activity.STATUS_INPUT_TEMPERATURE;
                                    activity.bNFCInput = true;
                                    activity.tv_temp_value.requestFocus();
                                    //activity.tv_idcard.setSelection(activity.tv_addr_value.getText().toString().length());
                                    //activity.tv_addr_value.setSelection(activity.tv_addr_value.getText().toString().length());
                                }
                            });
                        }
                    //}
                    break;
                case ByteUtil.MESSAGE_VALID_OTGSTART:
                    Log.i("Info","enter MESSAGE_VALID_OTGSTART");
                    break;
                case ByteUtil.BTREAD_BUTTON_ENABLED:
                    Log.i("Info","enter BTREAD_BUTTON_ENABLED");
                    break;
                default:break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        dynamicPermission.permissionRequestOperation(requestCode, permissions, grantResults);
    }
}
