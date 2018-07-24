package z.hobin.weibofs.net;

import org.json.JSONObject;

public interface WeiboCallBack {
    void onSuccess(WeiboResult result);

    void onFailed(WeiboResult result);
}
