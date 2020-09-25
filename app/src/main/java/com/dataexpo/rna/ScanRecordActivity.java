package com.dataexpo.rna;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dataexpo.rna.common.DBUtils;
import com.dataexpo.rna.common.Utils;
import com.dataexpo.rna.listener.OnItemClickListener;
import com.dataexpo.rna.pojo.SignData;
import com.idata.fastscandemo.R;
import com.zyao89.view.zloading.ZLoadingDialog;
import com.zyao89.view.zloading.Z_TYPE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScanRecordActivity extends BascActivity implements View.OnClickListener, OnItemClickListener {
    private final String TAG = ScanRecordActivity.class.getSimpleName();
    private Context mContext;
    private TextView tv_total;
    private TextView tv_total_today;
    private List<SignData> codes;
    private RecordAdapter dateAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_scan_record);
        initView();
        initData();
    }

    private void initData() {
        String usage_mode = Utils.getUsageMode(mContext);
        if ("offline".equals(usage_mode)) {
            codes = DBUtils.getInstance().listAllOffLine();
        } else {
            codes = DBUtils.getInstance().listAllOnLine();
        }
        tv_total.setText(String.valueOf(codes.size()));

        tv_total_today.setText(String.valueOf(DBUtils.getInstance().countToDay()));
        dateAdapter = new RecordAdapter();
        recyclerView.setAdapter(dateAdapter);

        dateAdapter.setItemClickListener(this);
        dateAdapter.setData(codes);
    }

    private void initView() {
        findViewById(R.id.btn_scan_record_back).setOnClickListener(this);
        //findViewById(R.id.tv_scan_record_back_scan).setOnClickListener(this);
        findViewById(R.id.btn_scan_record_clear).setOnClickListener(this);
        tv_total = findViewById(R.id.tv_scan_record_total_value);
        tv_total_today = findViewById(R.id.tv_scan_record_today_value);
        recyclerView = findViewById(R.id.recycler_scan_record);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        findViewById(R.id.btn_scan_record_save).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan_record_clear:
                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(mContext);
                normalDialog.setMessage("是否全部删除数据?不可恢复,请备份!");
                normalDialog.setPositiveButton("删除",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String usage_mode = Utils.getUsageMode(mContext);

                                if ("offline".equals(usage_mode)) {
                                    DBUtils.getInstance().delDataAllOffline();
                                } else {
                                    DBUtils.getInstance().delDataAllOnline();
                                }
                                finish();
                            }
                        });
                normalDialog.setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO: cancel button
                            }
                        });
                // 显示
                normalDialog.show();

                break;

            case R.id.btn_scan_record_back:
            //case R.id.tv_scan_record_back_scan:
                this.finish();
                break;

            case R.id.btn_scan_record_save:
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
                            //String strContent = code.getName() + "&&" + code.getIdcard() + "&&" + code.getEucode() + "&&" + code.getTemp() + "&&" + code.getTime();
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
                        Toast.makeText(mContext, "导出成功！目录是/sdcard/zmtRecord/", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        zdialog.cancel();
                        Toast.makeText(mContext, "导出失败", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(mContext, "没有数据可导出", Toast.LENGTH_LONG).show();
                }
                break;
            default:
        }
    }

    @Override
    public void onItemClick(View view, final int position) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setMessage("是否删除该数据?不可恢复!");
        normalDialog.setPositiveButton("删除",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBUtils.getInstance().delData(codes.get(position).getId());
                        codes.remove(position);
                        tv_total.setText(String.valueOf(codes.size()));
                        tv_total_today.setText(String.valueOf(codes.size()));
                        dateAdapter.notifyDataSetChanged();
                        Log.i(TAG, "delete codesize :" + codes.size());
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TODO: cancel button
                    }
                });
        // 显示
        normalDialog.show();
    }

    private static class DataHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView tv_name;
        private TextView tv_code;
        private TextView tv_time;
        private TextView tv_temp;
        private TextView tv_delete;

        public DataHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            tv_name = itemView.findViewById(R.id.tv_item_record_name);
            tv_code = itemView.findViewById(R.id.tv_item_record_code);
            tv_time = itemView.findViewById(R.id.tv_item_record_time);
            tv_temp = itemView.findViewById(R.id.tv_item_record_temp);
            tv_delete = itemView.findViewById(R.id.btn_item_record_delete);
        }
    }

    public class RecordAdapter extends RecyclerView.Adapter<DataHolder> implements View.OnClickListener {
        private List<SignData> mList;
        private OnItemClickListener mItemClickListener;

        public void setData(List<SignData> list) {
            mList = list;
        }

        @Override
        public void onClick(View v) {
//            if (mItemClickListener != null) {
//                mItemClickListener.onItemClick(v, (Integer) v.getTag());
//            }
        }

        private void setItemClickListener(OnItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @NonNull
        @Override
        public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_record, parent, false);
            DataHolder viewHolder = new DataHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(@NonNull DataHolder holder, int position, @NonNull List<Object> payloads) {
            super.onBindViewHolder(holder, position, payloads);
        }

        @Override
        public void onBindViewHolder(@NonNull DataHolder holder, final int position) {
            holder.itemView.setTag(position);
            // 添加数据
            holder.tv_code.setText(mList.get(position).getEucode());
            holder.tv_name.setText(mList.get(position).getName());
            holder.tv_temp.setText(mList.get(position).getTemp());
            holder.tv_time.setText(mList.get(position).getTime());

            holder.tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener!= null) {
                        mItemClickListener.onItemClick(v, position);
                    }
                }
            });

            Log.i(TAG, "size " + mList.size());
            //渲染奇数行
            if ((position & 0x01) == 0) {
                holder.itemView.setBackgroundColor(Color.parseColor("#FF7CAFF7"));
            } else {
                holder.itemView.setBackgroundColor(Color.WHITE);
            }
        }

        @Override
        public int getItemCount() {
            return mList != null ? mList.size() : 0;
        }
    }
}
