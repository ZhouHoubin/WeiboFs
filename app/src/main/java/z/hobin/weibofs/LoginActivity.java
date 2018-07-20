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

//                                Request.Builder builder = new Request.Builder();
//                                builder.addHeader("Host", "m.weibo.cn");
//                                builder.addHeader("Connection", "keep-alive");
//                                builder.addHeader("Accept", "application/json, text/plain, */*");
//                                builder.addHeader("Origin", "https://m.weibo.cn");
//                                builder.addHeader("X-Requested-With", "XMLHttpRequest");
//                                builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
//                                builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
//                                builder.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
//                                builder.addHeader("Cookie", caches.getAsString("Cookie"));
//                                builder.url("https://m.weibo.cn/message/chat?uid=" + caches.getAsString("uid"));
//                                Request request = builder.get().build();
//                                try {
//                                    OkHttpClient client = new OkHttpClient();
//                                    client.newCall(request).enqueue(new Callback() {
//                                        @Override
//                                        public void onFailure(Call call, IOException e) {
//
//                                        }
//
//                                        @Override
//                                        public void onResponse(Call call, Response response) throws IOException {
//                                            String html = response.body().string();
//                                            Document document = Jsoup.parse(html);
//                                            Elements scripts = document.select("script");
//                                            System.out.println();
//                                        }
//                                    });
//                                } catch (Exception e) {
//                                    L.e(e);
//                                }
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
