package z.hobin.weibofs;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;
import z.hobin.weibofs.net.SimpleCallBack;
import z.hobin.weibofs.net.Weibo;
import z.hobin.weibofs.net.WeiboCallBack;
import z.hobin.weibofs.net.WeiboResult;
import z.hobin.weibofs.util.Utils;
import z.hobin.weibofs.view.GridItem;
import z.hobin.weibofs.view.GridLinearLayout;

/**
 * 发布微博
 */
public class PublishCustomActivity extends AppCompatActivity implements View.OnClickListener, GridLinearLayout.ItemLongClickListener {

    private EditText input;
    private WebView web;
    private String visibile = "";
    private GridLinearLayout imageGrid;
    private int imageIndex;
    private int textIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_publish_random);

        TextView title = findViewById(R.id.action_title);
        title.setText("发布微博");

        input = findViewById(R.id.weibo_text);
        findViewById(R.id.actionbar_back).setOnClickListener(this);
        findViewById(R.id.publish).setOnClickListener(this);
        imageGrid = findViewById(R.id.images);
        imageGrid.setColumnCount(3);
        imageGrid.setOnItemLongClickListener(this);


        findViewById(R.id.random_img).setOnClickListener(this);
        findViewById(R.id.random_text).setOnClickListener(this);

        web = findViewById(R.id.web);
        WebSettings webSettings = web.getSettings();
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setGeolocationEnabled(false);
        web.setWebViewClient(new ViewClient());

        String cookie = Caches.get().getAsString("Cookie");
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie("https://m.weibo.cn", cookie);

        RadioGroup radioGroup = findViewById(R.id.radiogroup_status);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_open:
                        visibile = "";
                        break;
                    case R.id.radio_friend:
                        visibile = "6";
                        break;
                    case R.id.radio_private:
                        visibile = "1";
                        break;
                    default:
                        break;
                }
            }
        });

        RadioGroup radiogroupImg = findViewById(R.id.radiogroup_img);
        radiogroupImg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                imageIndex = group.indexOfChild(group.findViewById(group.getCheckedRadioButtonId()));
            }
        });

        RadioGroup radiogroupText = findViewById(R.id.radiogroup_text);
        radiogroupText.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                textIndex = group.indexOfChild(group.findViewById(group.getCheckedRadioButtonId()));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.publish:
                for (int i = 0; i < imageGrid.getChildCount(); i++) {
                    GridItem item = (GridItem) imageGrid.getChildAt(i);
                    final Bitmap bitmap = ((BitmapDrawable) item.getDrawable()).getBitmap();
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            String json = Utils.uploadPicture(bitmap);
                            System.out.println(json);
                        }
                    }.start();
                }

//                String weiboUrl = input.getText().toString();
//                if (TextUtils.isEmpty(weiboUrl)) {
//                    Toast.makeText(this, "输入需要拷贝的微博地址", Toast.LENGTH_SHORT).show();
//                } else {
//                    web.loadUrl(weiboUrl);
//                }
                break;
            case R.id.actionbar_back:
                finish();
                break;
            case R.id.random_img:
                fillImage();
                break;
            case R.id.random_text:
                fillText();
                break;
            default:
                break;
        }
    }

    private void fillImage() {
        GridItem img = new GridItem(getApplicationContext());
        imageGrid.addView(img);
        Picasso.get().load("https://unsplash.it/400/400?random&t=" + System.currentTimeMillis()).into(img);
    }


    private void fillText() {
        Utils.get("https://api.i-meto.com/hitokoto", new SimpleCallBack() {
            @Override
            public void onSuccess(String result) {
                input.setText(result);
            }

            @Override
            public void onFailed(String result) {

            }
        });
    }

    @Override
    public void onItemLongClick(int position, View child) {
        imageGrid.removeViewAt(position);
    }

    private class ViewClient extends WebViewClient {
        private boolean isFinish = false;

        @Override
        public void onPageFinished(final WebView view, String url) {
            super.onPageFinished(view, url);
            if (isFinish) {
                return;
            }
            view.evaluateJavascript("$render_data", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    try {
                        if (!TextUtils.isEmpty(value)) {
                            JSONObject json = new JSONObject(value);
                            JSONObject status = json.getJSONObject("status");
                            String text = status.getString("text");
                            JSONArray idsArray = status.getJSONArray("pic_ids");
                            String ids = "";
                            for (int i = 0; i < idsArray.length(); i++) {
                                ids += idsArray.getString(i);
                                if (i == idsArray.length() - 1) {
                                    break;
                                }
                                ids += ",";
                            }
                            text = Utils.trimHtml(text);
                            Weibo weibo = new Weibo();
                            weibo.publish(text, ids, visibile, new WeiboCallBack() {
                                @Override
                                public void onSuccess(WeiboResult result) {
                                    Toast.makeText(PublishCustomActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onFailed(WeiboResult result) {
                                    Toast.makeText(PublishCustomActivity.this, "发布失败," + result.msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    L.d("Config_Chat", value);
                }
            });
            isFinish = true;
        }
    }
}
