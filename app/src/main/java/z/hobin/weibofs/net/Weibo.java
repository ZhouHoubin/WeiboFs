package z.hobin.weibofs.net;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public Weibo() {

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
        String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 取消关注
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public JSONObject unfollow(String uid) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/destory");
        String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            String json = response.body().string();
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
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
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
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
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
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
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
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
            L.d(json);
            return new JSONObject(json);
        } catch (Exception e) {
            L.e(e);
        }
        return null;
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
                    comment(id, msgs.get(i));
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
            L.d(userId);
            return userId;
        } catch (Exception e) {
            L.e(e);
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
