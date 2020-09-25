package com.dataexpo.rna.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Utils {

    public static final int INPUT_SUCCESS = 0;
    public static final int INPUT_HAVE_NET_ADDRESS = 1;
    public static final int INPUT_ONLY_NUM = 2;
    public static final int INPUT_CHECK_NET_ADDRESS = 3;
    public static final int INPUT_NULL = 4;
    public static final int INPUT_NO_CHECK = 99;

    static byte AESIV[] = ByteBuffer.allocate(16).array();
    static String algorithm = "AES";
    static String transformation = "AES/CBC/PKCS5PADDING";
    static byte AESKEY[];

    public static String USAGE_MODE = "user_mode";

    static {
        try {
            AESKEY = "345f266b93ee2072".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 时间格式转化
     * @param timeStamp
     * @param pattern
     * @return
     */
    public static String formatTime(long timeStamp, String pattern) {
        Date date = new Date(timeStamp);
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static int checkInput(String input) {
        return checkInput(input, INPUT_CHECK_NET_ADDRESS);
    }

    public static int checkInput(String input, int target) {
        if (TextUtils.isEmpty(input)) {
            return INPUT_NULL;
        }

        if (input.contains("http")) {
            return INPUT_HAVE_NET_ADDRESS;
        }

        if (input.contains("www")) {
            return INPUT_HAVE_NET_ADDRESS;
        }

        if (INPUT_ONLY_NUM == target) {

        }
        //TODO: check int or order code
        return INPUT_SUCCESS;
    }

    /**
     * aes-128-cbc  加密
     * @param key
     * @param initVector
     * @param value
     * @return
     */
    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: " + Base64.encodeToString(encrypted, Base64.DEFAULT));

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    /**
     * aes-128-cbc  解密
     * @param encrypted
     * @return
     */
    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(AESIV);
            SecretKeySpec skeySpec = new SecretKeySpec(AESKEY, algorithm);

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted.getBytes(), Base64.DEFAULT));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public synchronized static String getUsageMode(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(USAGE_MODE,
                Context.MODE_PRIVATE);
        return preferences.getString(USAGE_MODE, "");
    }

    public synchronized static void saveUsageMode(Context context, String val) {
        SharedPreferences preferences = context.getSharedPreferences(USAGE_MODE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(USAGE_MODE, val);
        editor.apply();
    }

    public static String getSerialNumber() {
        String serial = "";
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {//8.0+
                serial = Build.SERIAL;

            } else {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class);
                serial = (String) get.invoke(c, "ro.serialno");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serial;

    }
}
