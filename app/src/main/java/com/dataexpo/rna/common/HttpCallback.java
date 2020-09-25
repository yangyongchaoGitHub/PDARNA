package com.dataexpo.rna.common;

import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * 网络请求回调
 */

public abstract class HttpCallback extends StringCallback {

    public abstract void onError(Call call, Exception e, int id);

    public abstract void onResponse(String response, int id);
}