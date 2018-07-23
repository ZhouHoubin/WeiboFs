package z.hobin.weibofs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import z.hobin.weibofs.net.Weibo;
import z.hobin.weibofs.net.WeiboCallBack;

/**
 * 关注
 */
public class FollowActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userNames;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_follow);

        findViewById(R.id.actionbar_back).setOnClickListener(this);

        TextView title = findViewById(R.id.action_title);
        title.setText("关注");

        userNames = findViewById(R.id.users);

        findViewById(R.id.follow).setOnClickListener(this);

        findViewById(R.id.followMessage).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar_back:
                finish();
                break;
            case R.id.follow:
                List<String> userNames = getUserNames();
                if (userNames == null || userNames.size() == 0) {
                    return;
                }
                Weibo weibo = new Weibo();
                Toast.makeText(this, userNames.size() + " ", Toast.LENGTH_SHORT).show();
                weibo.follow(userNames, new WeiboCallBack() {
                    @Override
                    public void onSuccess(Object json) {
                        if (json instanceof Integer) {
                            Toast.makeText(FollowActivity.this, "关注成功 " + Integer.parseInt(json.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(Object json) {
                        if (json instanceof Integer) {
                            Toast.makeText(FollowActivity.this, "关注失败 " + Integer.parseInt(json.toString()), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.followMessage:
                Toast.makeText(this, "请更新", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private List<String> getUserNames() {
        List<String> ids = new ArrayList<>();
        String names = userNames.getText().toString();
        if (!TextUtils.isEmpty(names)) {
            String[] lines = names.split("\r\n");
            for (String line : lines) {
                line = line.replaceAll(" ", "");
                line = line.trim();
                ids.add(line);
            }
        } else {
            return null;
        }
        return ids;
    }
}
