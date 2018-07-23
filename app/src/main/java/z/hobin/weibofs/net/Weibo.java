package z.hobin.weibofs.net;

import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;

public class Weibo {
    private OkHttpClient client = new OkHttpClient();
    private Caches caches = Caches.get();
    private WeiboCallBack weiboCallBack;

    public Weibo() {

    }

    public Weibo(WeiboCallBack weiboCallBack) {
        this.weiboCallBack = weiboCallBack;
    }

    /**
     * 关注
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public JSONObject follow(String uid) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/create");
        String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFansContainerId());
        builder.addHeader("Referer", referer);
        String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("Follow", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("Follow", e);
        }
        return null;
    }

    /**
     * 关注
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public void followAsync(final String uid) {
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... voids) {
                Request.Builder builder = getDefaultHeader();
                builder.url("https://m.weibo.cn/api/friendships/create");
                String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFansContainerId());
                builder.addHeader("Referer", referer);
                String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
                Request request = builder.post(getStringRequestBody(data)).build();
                try {
                    Response response = client.newCall(request).execute();
                    String json = response.body().string();
                    L.d("Follow", json);
                    return new JSONObject(json);
                } catch (Exception e) {
                    L.e("Follow", e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                super.onPostExecute(jsonObject);
                if (weiboCallBack != null) {
                    if (jsonObject != null) {
                        weiboCallBack.onSuccess(jsonObject);
                    } else {
                        weiboCallBack.onFailed(null);
                    }
                }
            }
        }.execute();
    }

    /**
     * 取消关注
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public JSONObject unFollow(String uid) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/destory");
        String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFollowedContainerId());
        builder.addHeader("Referer", referer);
        String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.getInt("ok") == 0) {
                if (json.getString("error_type").equalsIgnoreCase("captcha")) {
                    //验证码
                }
            }
            L.d("UnFollow", json.toString());
            return new JSONObject(json.toString());
        } catch (Exception e) {
            L.e("UnFollow", e);
        }
        return null;
    }

    /**
     * 验证码图片 base64 转码
     *
     * @return 验证码
     */
    public String getCaptcha() {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/captcha/show?t=" + System.currentTimeMillis());
        builder.addHeader("Referer", "https://m.weibo.cn/sw.js");
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            byte[] data = response.body().bytes();
            return new String(Base64.encode(data, Base64.DEFAULT));
        } catch (Exception e) {
            L.e("UnFollow", e);
        }
        return null;
    }

    /**
     * 点赞
     *
     * @param id 帖子id
     * @return 用户信息
     */
    public JSONObject like(String uid, String id) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/attitudes/create");
        builder.addHeader("Referer", "https://m.weibo.cn/profile/" + uid);
        String data = String.format(Locale.CHINA, "id=%s&attitude=heart&st=%s", id, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("Like", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("Like", e);
        }
        return null;
    }


    /**
     * 取消点赞
     *
     * @param id 帖子id
     * @return 用户信息
     */
    public JSONObject unlike(String id) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/attitudes/destroy");
        String data = String.format(Locale.CHINA, "id=%s&attitude=heart&st=%s", id, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("UnLike", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("UnLike", e);
        }
        return null;
    }

    /**
     * 评论
     *
     * @param id  帖子id
     * @param msg 内容
     * @return 用户信息
     */
    public JSONObject comment(String id, String msg) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/comments/create");
        builder.addHeader("Referer", "https://m.weibo.cn/status/" + id);
        String data = String.format(Locale.CHINA, "content=%s&mid=%s&st=%s", msg, id, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("Comment", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("Comment", e);
        }
        return null;
    }

    /**
     * 发送消息
     *
     * @param uid 用户id
     * @param msg 消息内容
     * @return
     */
    public JSONObject sendMsg(String uid, String msg) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/chat/send");
        builder.addHeader("Referer", "https://m.weibo.cn/message/chat?uid=" + uid);
        builder.addHeader("MWeibo-Pwa", "1");
        String data = String.format(Locale.CHINA, "uid=%s&content=%s&st=%s", uid, msg, getChatSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("Msg", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("Msg", e);
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public JSONObject getUserInfo(String uid) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/container/getIndex?type=uid&value=" + uid);
        builder.addHeader("Referer", "https://m.weibo.cn/profile/" + uid);
        builder.addHeader("MWeibo-Pwa", "1");
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d("UserInfo", json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e("UserInfo", e);
        }
        return null;
    }

    /**
     * 获取关注用户containerid
     *
     * @return 231093_-_selffollowed
     */
    public String getSelfFollowedContainerId() {
        JSONObject json = getUserInfo(getLocalUid());
        try {
            String scheme = json.getJSONObject("data").getString("follow_scheme");
            URLParser urlParser = URLParser.fromURL(scheme);
            urlParser.compile();
            String containerId = urlParser.getParameter("containerid");
            return containerId.split("_")[0] + "_-_selffollowed";
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取粉丝containerid
     *
     * @return 231093_-_selffollowed
     */
    public String getSelfFansContainerId() {
        JSONObject json = getUserInfo(getLocalUid());
        try {
            String scheme = json.getJSONObject("data").getString("fans_scheme");
            URLParser urlParser = URLParser.fromURL(scheme);
            urlParser.compile();
            String containerId = urlParser.getParameter("containerid");
            return containerId + "_-_selffans";
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取所有关注用户
     * "relationship": 3, 互关
     */
    public List<JSONObject> getFolloweds() {
        String containerId = getSelfFollowedContainerId();
        List<JSONObject> userInfo = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            Request.Builder builder = getDefaultHeader();
            String url = String.format(Locale.CHINA, "https://m.weibo.cn/api/container/getIndex?containerid=%s&page=%d", containerId, i);
            builder.url(url);
            builder.addHeader("Referer", "https://m.weibo.cn/p/index?containerid=" + containerId);
            builder.addHeader("MWeibo-Pwa", "1");
            Request request = builder.get().build();
            try {
                Response response = client.newCall(request).execute();
                JSONObject json = new JSONObject(response.body().string());
                JSONArray cards = json.getJSONObject("data").getJSONArray("cards");
                JSONArray cardGroup = null;
                if (cards.length() == 1) {
                    cardGroup = cards.getJSONObject(0).getJSONArray("card_group");
                } else if (cards.length() == 2) {
                    cardGroup = cards.getJSONObject(1).getJSONArray("card_group");
                }
                if (cardGroup != null) {
                    for (int j = 0; j < cardGroup.length(); j++) {
                        userInfo.add(cardGroup.getJSONObject(j));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                L.e("UserInfo", e);
                break;
            }
        }
        L.d("Followed", userInfo.size() + "");
        return userInfo;
    }

    /**
     * 获取所有粉丝
     * "relationship": 3, 互关
     * 3 互关
     * 2 我关注了对方,没有关注我
     * 1 关注了我,没有关注对方
     */
    public List<JSONObject> getFans() {
        String containerId = getSelfFansContainerId();
        List<JSONObject> userInfo = new ArrayList<>();
        for (int i = 1; i < 20; i++) {
            Request.Builder builder = getDefaultHeader();
            String url = String.format(Locale.CHINA, "https://m.weibo.cn/api/container/getIndex?containerid=%s&page=%d", containerId, i);
            builder.url(url);
            builder.addHeader("Referer", "https://m.weibo.cn/p/index?containerid=" + containerId);
            builder.addHeader("MWeibo-Pwa", "1");
            Request request = builder.get().build();
            try {
                Response response = client.newCall(request).execute();
                JSONObject json = new JSONObject(response.body().string());
                JSONArray cards = json.getJSONObject("data").getJSONArray("cards");
                JSONArray cardGroup = null;
                if (cards.length() == 1) {
                    cardGroup = cards.getJSONObject(0).getJSONArray("card_group");
                } else if (cards.length() == 3) {
                    cardGroup = cards.getJSONObject(2).getJSONArray("card_group");
                }
                if (cardGroup != null) {
                    for (int j = 0; j < cardGroup.length(); j++) {
                        userInfo.add(cardGroup.getJSONObject(j));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                L.e("UserInfo", e);
                break;
            }
        }
        L.d("Followed", userInfo.size() + "");
        return userInfo;
    }

    /**
     * 评论并点赞
     *
     * @param uid  用户id
     * @param msgs 消息内容
     * @return
     */
    public JSONObject commentLike(String uid, List<String> msgs) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/profile/info?uid=" + uid);
        builder.addHeader("Referer", "https://m.weibo.cn/profile/" + uid);
        builder.addHeader("MWeibo-Pwa", "1");
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            JSONArray statuses = json.getJSONObject("data").getJSONArray("statuses");
            if (statuses != null && statuses.length() != 0) {
                for (int i = 0; i < statuses.length() && i < 3; i++) {
                    JSONObject card = statuses.getJSONObject(i);
                    String id = card.getString("id");
                    like(uid, id);
                    if (msgs != null) {
                        comment(id, msgs.get(i));
                    }
                    sleep(1000 * 3);
                }
            }
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据用户名字获取id
     *
     * @param userName 用户昵称
     * @return 用户ID
     */
    public String getUserIdByName(String userName) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/n/" + userName);
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            String userId = response.request().url().pathSegments().get(1);
            L.d("GetUserIdByName", userId);
            return userId;
        } catch (Exception e) {
            L.e("GetUserIdByName", e);
        }
        return null;
    }

    private RequestBody getStringRequestBody(String data) {
        String[] datas = data.split("&");
        FormBody.Builder builder = new FormBody.Builder();
        for (String line : datas) {
            String[] lines = line.split("=");
            builder.add(lines[0], lines[1]);
        }
        return builder.build();
        //return RequestBody.create(MediaType.parse("text/plain"), data);
    }

    private Request.Builder getDefaultHeader() {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Host", "m.weibo.cn");
        builder.addHeader("Connection", "keep-alive");
        builder.addHeader("Accept", "application/json, text/plain, */*");
        builder.addHeader("Origin", "https://m.weibo.cn");
        builder.addHeader("X-Requested-With", "XMLHttpRequest");
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        builder.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        builder.addHeader("Cookie", caches.getAsString("Cookie"));
        return builder;
    }

    private String getSt() {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/config");
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            String st = json.getJSONObject("data").getString("st");
            L.d("ST ", st);
            return st;
        } catch (Exception e) {
            L.e(e);
        }
        Caches caches = Caches.get();
        if (caches != null) {
            return caches.getAsString("st");
        }
        return null;
    }

    private String getChatSt() {
        Caches caches = Caches.get();
        if (caches != null) {
            return caches.getAsString("st_chat");
        }
        return null;
    }

    private String getLocalUid() {
        Caches caches = Caches.get();
        if (caches != null) {
            return caches.getAsString("uid");
        }
        return null;
    }
}
