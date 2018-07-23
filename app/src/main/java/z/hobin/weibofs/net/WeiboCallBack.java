package z.hobin.weibofs.net;

import org.json.JSONObject;

public interface WeiboCallBack {
    void onSuccess(JSONObject json);

    void onFailed(JSONObject json);
}
