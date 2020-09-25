package com.dataexpo.rna.common;

import android.content.Context;

import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.cookie.CookieJarImpl;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.HashMap;

import okhttp3.CookieJar;
import okhttp3.MediaType;

/**
 * 网络请求类
 */
public class HttpService {
    public static String token;

    /**
     * get方式
     *
     * @param url
     * @param httpCallBack
     */
    public static void getWithNullParams(Context context, String url, HttpCallback httpCallBack) {
        OkHttpUtils
                .get()
                .url(url)
                .params(getParams(context, null))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);


    }

    /**
     * get方式提交键值对数据
     *
     * @param url
     * @param hashMap
     * @param httpCallBack
     */
    public static void getWithParams(Context context, String url, HashMap<String, String> hashMap, HttpCallback httpCallBack) {
        OkHttpUtils
                .get()
                .url(url)
                .headers(getHeads(context))
                .params(getParams(context, hashMap))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);
    }


    /**
     * get方式提交键值对数据
     *
     * @param url
     * @param hashMap
     * @param id
     * @param httpCallBack
     */
    public static void getWithParams(Context context, String url, HashMap<String, String> hashMap, int id, HttpCallback httpCallBack) {
        OkHttpUtils
                .get()
                .url(url)
                .id(id)
                .params(getParams(context, hashMap))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);
    }

    /**
     * get方式获取图片
     *
     * @param url
     * @param tag
     * @param bitmapCallback
     */
    public static void getImage(Context context, String url, String tag, BitmapCallback bitmapCallback) {
        OkHttpUtils
                .get()
                .url(url)
                .tag(tag)
                .params(getParams(context, null))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(bitmapCallback);
    }


    /**
     * get方式下载文件
     *
     * @param url
     * @param fileCallBack
     */
    public static void downloadFile(Context context, String url, FileCallBack fileCallBack) {
        OkHttpUtils
                .get()
                .url(url)
                .params(getParams(context, null))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(fileCallBack);
    }


    /**
     * post方式提交键值对数据
     *
     * @param url
     * @param httpCallBack
     */
    public static void postOtherWithNullParams(Context context, String url, HttpCallback httpCallBack) {
        RequestCall requestCall = OkHttpUtils
                .post()
                .url(url)
                .params(getParams(context, null))
                .headers(getHeads(context))
                .build()
                .connTimeOut(5000)
                .readTimeOut(5000)
                .writeTimeOut(5000);
        requestCall.execute(httpCallBack);
    }


    /**
     * post方式提交键值对数据
     *
     * @param url
     * @param httpCallBack
     */
    public static void postWithNullParams(Context context, String url, HttpCallback httpCallBack) {
        RequestCall requestCall = OkHttpUtils
                .post()
                .params(getParams(context, null))
                .url(url)
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000);
        requestCall.execute(httpCallBack);
    }

    /**
     * post方式提交键值对数据
     *
     * @param url
     * @param hashMap
     * @param httpCallBack
     */
    public static RequestCall postWithParams(Context context, String url, HashMap<String, String> hashMap, int id, HttpCallback httpCallBack) {
        RequestCall requestCall = OkHttpUtils
                .post()
                .url(url)
                .id(id)
                .params(getParams(context, hashMap))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000);
        requestCall.execute(httpCallBack);
        return requestCall;
    }

    /**
     * post方式提交文件
     *
     * @param url
     * @param file
     * @param fileCallBack
     */
    public static void postFile(Context context, String url, File file, FileCallBack fileCallBack) {
        OkHttpUtils
                .postFile()
                .url(url)
                .file(file)
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(fileCallBack);
    }

    /**
     * post方式提交文件
     *
     * @param url
     * @param file
     * @param httpCallBack
     */
    public static void postFile(Context context, String url, File file, HttpCallback httpCallBack) {
        OkHttpUtils
                .postFile()
                .file(file)
                .url(url)
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);
    }

    /**
     * post方式提交键值对数据，同时提交文件
     *
     * @param url
     * @param hashMap
     * @param file
     * @param fileType     例如：image
     * @param fileName
     * @param httpCallBack
     */
    public static void postParamsAndFile(Context context, String url, HashMap<String, String> hashMap, File file, String fileType, String fileName, HttpCallback httpCallBack) {
        //可以提交多个文件
        OkHttpUtils
                .post()
                .addFile(fileType, fileName, file)
                .url(url)
                .params(hashMap)
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);

    }


    /**
     * post方式提交Json数据
     *
     * @param url
     * @param obj
     * @param httpCallBack
     */
    public static void postJson(Context context, String url, Object obj, HttpCallback httpCallBack) {
        OkHttpUtils
                .postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(new Gson().toJson(obj))
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);
    }

    /**
     * post方式提交Json数据
     *
     * @param url
     * @param jsonValue
     * @param httpCallBack
     */
    public static void postJson(Context context, String url, String jsonValue, HttpCallback httpCallBack) {
        OkHttpUtils
                .postString()
                .url(url)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .content(jsonValue)
                .headers(getHeads(context))
                .build()
                .connTimeOut(10000)
                .readTimeOut(10000)
                .writeTimeOut(10000)
                .execute(httpCallBack);
    }

    /**
     * 清除Session
     */
    public static void clearSession() {
        CookieJar cookieJar = OkHttpUtils.getInstance().getOkHttpClient().cookieJar();
        if (cookieJar instanceof CookieJarImpl) {
            ((CookieJarImpl) cookieJar).getCookieStore().removeAll();
        }
    }


    /**
     * 将Unicode编码解析成字符串形式（如汉字）
     */
    public static String decodeUnicodeToString(String uString) {
        StringBuilder sb = new StringBuilder();
        int i = -1, pos = 0;
        while ((i = uString.indexOf("\\u", pos)) != -1) {
            sb.append(uString.substring(pos, i));
            if (i + 5 < uString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(uString.substring(i + 2, i + 6), 16));
            }
        }
        sb.append(uString.substring(pos));
        return sb.toString();
    }

    /**
     * 获取头部
     */
    public static HashMap<String, String> getHeads(Context context) {
        HashMap<String, String> heads = new HashMap<>();
        return heads;
    }

    /**
     * 获取传递参数
     */
    public static HashMap<String, String> getParams(Context context, HashMap<String, String> hashMap) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        return hashMap;
    }

}