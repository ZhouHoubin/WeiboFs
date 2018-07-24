package z.hobin.weibofs;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;

public class LoginActivity extends Activity {

    private WebView web;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        web = findViewById(R.id.web);
        WebSettings webSettings = web.getSettings();
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(false);

        web.setWebViewClient(new ViewClient());
        web.loadUrl("https://passport.weibo.cn/signin/login?entry=mweibo&res=wel&wm=3349&r=https%3A%2F%2Fm.weibo.cn%2F");
    }

    private class ViewClient extends WebViewClient {
        @Override
        public void onPageFinished(final WebView view, String url) {
            CookieManager cookieManager = CookieManager.getInstance();
            String cookie = cookieManager.getCookie(url);
            final Caches caches = Caches.get();
            if (!TextUtils.isEmpty(cookie)) {
                if (caches != null) {
                    caches.put("Cookie", cookie, Caches.TIME_DAY);
                }
            }
            L.d("Cookie@" + url, cookie);
            if (url.equalsIgnoreCase("https://m.weibo.cn/")) {
                view.evaluateJavascript("config", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        try {
                            if (caches != null && !TextUtils.isEmpty(value)) {
                                JSONObject json = new JSONObject(value);
                                caches.put("st", json.getString("st"), Caches.TIME_DAY);
                                caches.put("uid", json.getString("uid"), Caches.TIME_DAY);
                                Toast.makeText(LoginActivity.this, "请稍候", Toast.LENGTH_SHORT).show();
                                web.pauseTimers();
                                web.loadUrl("https://m.weibo.cn/message/");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        L.d("Config", value);
                    }
                });
            }
            if (url.equalsIgnoreCase("https://m.weibo.cn/message/")) {
                view.evaluateJavascript("config", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        try {
                            if (caches != null && !TextUtils.isEmpty(value)) {
                                JSONObject json = new JSONObject(value);
                                caches.put("st_chat", json.getString("st"), Caches.TIME_DAY);
                                caches.put("uid", json.getString("uid"), Caches.TIME_DAY);
                                Toast.makeText(LoginActivity.this, "注册成功,ID " + json.getString("uid"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        L.d("Config_Chat", value);
                        finish();
                    }
                });
            }
            super.onPageFinished(view, url);
        }
    }
}
