/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.workspace;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

import cn.wildfire.chat.kit.Config;
import cn.wildfire.chat.kit.WfcWebViewActivity;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GeneralCallback2;
import wendu.dsbridge.CompletionHandler;
import wendu.dsbridge.DWebView;

public class JsApi {
    private static final String TAG = "JsApi";
    private Context context;
    private DWebView webView;
    private String url;

    public JsApi(Context context, DWebView webView, String url) {
        this.context = context;
        this.webView = webView;
        this.url = url;
    }

    @JavascriptInterface
    public void openUrl(Object url) {
        if (!Config.WORKSPACE_URL.equals(this.url)) {
            Log.e(TAG, "only workspace can call openurl " + this.url);
            return;
        }
        WfcWebViewActivity.loadUrl(context, "", url.toString());
    }

    @JavascriptInterface
    public void getAuthCode(Object obj, CompletionHandler handler) {
        JSONObject jsonObject = (JSONObject) obj;
        String appId = jsonObject.optString("appId");
        int type = jsonObject.optInt("type");
        String host = null;
        try {
            host = new URL(url).getHost().toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ChatManager.Instance().getAuthCode(appId, type, host, new GeneralCallback2() {
            @Override
            public void onSuccess(String result) {
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("code", 0);
                    resultObj.put("data", result);
                    handler.complete(resultObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(int errorCode) {
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("code", errorCode);
                    handler.complete(resultObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @JavascriptInterface
    public void config(Object obj) {
        JSONObject jsonObject = (JSONObject) obj;
        String appId = jsonObject.optString("appId");
        int type = jsonObject.optInt("type");
        long timestamp = jsonObject.optLong("timestamp");
        String nonce = jsonObject.optString("nonce");
        String signature = jsonObject.optString("signature");
        ChatManager.Instance().configApplication(appId, type, timestamp, nonce, signature, new GeneralCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, obj.toString(), Toast.LENGTH_SHORT).show();
                webView.callHandler("ready", (Object[]) null);
            }

            @Override
            public void onFail(int errorCode) {
                webView.callHandler("error", new String[]{"" + errorCode});
            }
        });
    }

    @JavascriptInterface
    public void toast(Object text) {
        Toast.makeText(context, text.toString(), Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void chooseContact(Object obj, CompletionHandler handler) {

    }
}