package com.dataexpo.rna.readidcard;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;


public class DynamicPermission {
    private Context mContext;
    private String[] permissions;
    private List<String> noPermissions;
    private PassPermission passPermission;
    private int requestCode;
    //获得不再询问的禁止权限名
    private String permissionForbidName = "";

    public DynamicPermission(Context context, PassPermission passPermission){
        this.mContext = context;
        this.passPermission = passPermission;
        init();
    }

    /**
     * 权限检测入口
     */
    public void getPermissionStart(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            judgePermission();
        }else{
            Toast.makeText(mContext,"此设备无sd卡", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 检查用户是否通过了所有的手动授予的权限，没有则继续弹窗让用户手动设置
     * @return
     */
    public boolean checkPermissionPassState(){
        if(permissionDialog != null){
            getNoPermissions();
            if(noPermissions.size()>0){
                return false;
            }
        }
        return true;
    }

    /**
     * 对用户的选择进行处理
     * @param requestCode
     * @param permission
     * @param grantResults
     */
    public void permissionRequestOperation(int requestCode, String[] permission, int[] grantResults){
        boolean permissionAllPassed = true;
        boolean permissionAskNever = false;
        switch (requestCode){
            case 1:
                for(int i = 0;i<grantResults.length;i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        permissionAllPassed = false;

                        if(!isRequestAllow(permission[i])){
                            permissionAskNever = true;
                            permissionForbidName = permission[i];
                        }
                    }
                }
                break;
            default:break;
        }

        if(permissionAllPassed){
            passPermission.operation();
        }else{
            if(permissionAskNever){
                showPermissionDialog(permissionForbidName);
            }else{
                judgePermission();
            }
        }
    }

    /**
     * 判断用户有没有勾选‘不在提示’复选框
     * @param permission
     * @return
     */
    private boolean isRequestAllow(String permission){
        if(ActivityCompat.shouldShowRequestPermissionRationale((Activity)mContext,permission)){
            return true;
        }
        return false;
    }

    private void init(){
        //project permissions needed
        permissions = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        //no permission list
        noPermissions = new ArrayList<>();
        requestCode = 1;
    }

    private void judgePermission(){
        getNoPermissions();

        /**
         * 判断未授权集合是否为空
         */
        if(noPermissions.isEmpty()){
            //权限全部获取，进行相应的操作
            passPermission.operation();
        }else{
            //请求未授权的权限
            String[] getPermissions = noPermissions.toArray(new String[noPermissions.size()]);
            ActivityCompat.requestPermissions((Activity) mContext,getPermissions,requestCode);
        }
    }

    /**
     * 不再提示权限时弹出的对话框，此时未授予的权限为拒绝访问状态
     */
    AlertDialog permissionDialog = null;

    /**
     * 用户手动禁止后的弹窗提示
     */
    public void showPermissionDialog(){
        showPermissionDialog(permissionForbidName);
    }

    private void showPermissionDialog(String name){
        if(permissionDialog == null){
            createPermissionDialog();
        }
        permissionDialog.setMessage("已禁用"+getPermissionName(name)+"相关权限，请手动授予");
        permissionDialog.show();
    }

    private void createPermissionDialog(){
        permissionDialog = new AlertDialog.Builder(mContext)
                .setMessage("已禁用相关权限，请手动授予")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionDialog.dismiss();
                        Uri packageURI = Uri.parse("package:" + mContext.getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        mContext.startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionDialog.dismiss();
                        ((Activity)mContext).finish();
                    }
                })
                .setCancelable(false)
                .create();
    }

    private String getPermissionName(String name){
        if(name.contains("STORAGE")){
            return "存储";
        }else if(name.contains("PHONE")){
            return "电话";
        }
        return "";
    }

    private void getNoPermissions(){
        /**
         * 判断哪些权限未授予
         */
        noPermissions.clear();
        for(int i = 0;i<permissions.length;i++){
            if(ActivityCompat.checkSelfPermission(mContext,permissions[i]) != PackageManager.PERMISSION_GRANTED){
                noPermissions.add(permissions[i]);
            }
        }
    }

    /**
     * 回调接口，通知程序权限全部授予之后做的操作
     */
    public interface PassPermission{
        void operation();
    }
}
