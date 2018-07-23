package z.hobin.weibofs.net;

import org.json.JSONObject;

public interface WeiboCallBack {
    void onSuccess(Object json);

    void onFailed(Object json);
}
