package z.hobin.weibofs.net;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;
import z.hobin.weibofs.util.Utils;

@SuppressLint("StaticFieldLeak")
public class Weibo {
    private OkHttpClient client = new OkHttpClient();
    private Caches caches = Caches.get();

    public Weibo() {

    }

    /**
     * 获取主页微博
     *
     * @param page 页数
     * @return 用户信息
     */
    public List<JSONObject> getMainWeibo(int page) {
        List<JSONObject> weibos = new ArrayList<>();

        String max_id = "";
        for (int i = 0; i < page; i++) {
            String url = "";
            if (i == 0) {
                url = "https://m.weibo.cn/feed/friends?";
            } else {
                if (TextUtils.isEmpty(max_id)) {
                    break;
                }
                url = String.format(Locale.CHINA, "https://m.weibo.cn/feed/friends?max_id=%s", max_id);
            }
            Request.Builder builder = getDefaultHeader();
            builder.url(url);
            builder.addHeader("Referer", "https://m.weibo.cn");
            Request request = builder.get().build();
            try {
                Response response = client.newCall(request).execute();
                JSONObject json = new JSONObject(response.body().string());
                if (json.getInt("ok") == 1) {
                    JSONObject data = json.getJSONObject("data");
                    max_id = String.valueOf(data.getLong("max_id"));
                    JSONArray statuses = data.getJSONArray("statuses");
                    for (int j = 0; j < statuses.length(); j++) {
                        weibos.add(statuses.getJSONObject(j));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                L.e("Follow", e);
            }
        }
        L.d("主页", "" + weibos.size());
        return weibos;
    }

    /**
     * 通过帖子提取微博用户名并关注
     *
     * @param url 贴吧地址
     */
    public WeiboResult followByTieba(String url) {
        WeiboResult result = new WeiboResult();
        List<String> userNames = new ArrayList<>();
        try {
            for (int i = 1; i < 20; i++) {
                String u = url + "?pn=" + i;
                Map<String, String> header = new HashMap<String, String>();
                header.put("Host", "tieba.baidu.com");
                header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
                header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                header.put("Accept-Language", "zh-HK,zh-CN;q=0.9,zh;q=0.8,ja-JP;q=0.7,ja;q=0.6,zh-TW;q=0.5,en-US;q=0.4,en;q=0.3");
                header.put("Connection", "keep-alive");
                header.put("Upgrade-Insecure-Requests", "1");
                header.put("Cache-Control", "max-age=0");
                Document document = Jsoup.connect(u).headers(header).get();
                Elements elements = document.select(".j_d_post_content");
                for (int j = 0; j < elements.size(); j++) {
                    String userName = elements.get(j).text();
                    if (userName.contains("dd")) {
                        continue;
                    }
                    if (userName.contains("粉")) {
                        continue;
                    }
                    if (userName.contains("关注")) {
                        continue;
                    }
                    if (userName.contains("id")) {
                        continue;
                    }
                    if (TextUtils.isEmpty(userName)) {
                        continue;
                    }
                    if (userName.contains(",")) {
                        continue;
                    }
                    if (userName.contains(" ")) {
                        continue;
                    }
                    if (userName.contains("ID")) {
                        continue;
                    }
                    userNames.add(userName);
                }
                Elements pagers = document.select(".pb_list_pager");
                if (pagers.size() == 0 || !pagers.get(0).text().contains("下一页")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        result.data = userNames;
        result.total = userNames.size();
        result.progress = follow(userNames);
        return result;
    }

    /**
     * 通过帖子提取微博用户名并关注
     *
     * @param url 贴吧地址
     */
    public void followByTieba(final String url, final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                return followByTieba(url);
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                weiboCallBack.onSuccess(result);
            }
        }.execute();
    }

    /**
     * 主页点赞
     *
     * @param page          页数
     * @param weiboCallBack 回调
     */
    public void likeMainPage(final int page, final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                List<JSONObject> weibos = getMainWeibo(page);
                result.total = weibos.size();
                for (JSONObject weibo : weibos) {
                    Utils.sleep(1000 * 5);
                    try {
                        String userId = String.valueOf(weibo.getJSONObject("user").getLong("id"));
                        String id = weibo.getString("id");
                        JSONObject likeJson = like(userId, id);
                        if (likeJson.getInt("ok") == 1) {
                            result.progress++;
                        } else {
                            result.msg = likeJson.getString("msg");
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result.exception = e;
                        result.msg = e.getMessage();
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    if (TextUtils.isEmpty(result.msg)) {
                        weiboCallBack.onSuccess(result);
                    } else {
                        weiboCallBack.onFailed(result);
                    }
                }
            }
        }.execute();
    }

    /**
     * 关注
     *
     * @param uid 用户id
     * @return 用户信息
     */
    public JSONObject follow(String uid) {
        if (!Utils.isInteger(uid)) {
            uid = getUserIdByName(uid);
            if (!Utils.isInteger(uid)) {
                return null;
            }
        }
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/create");
        String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFansContainerId());
        builder.addHeader("Referer", referer);
        String data = String.format(Locale.CHINA, "uid=%s&st=%s", uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.getInt("ok") == 0) {
                L.e("Follow", json.getString("msg"));
                if (json.has("error_type") &&
                        json.getString("error_type").equalsIgnoreCase("captcha")) {
                    L.d("Follow", "需要验证码");
                    //验证码
                    String captchaBase64 = getCaptcha();
                    LianZhong lianZhong = new LianZhong();
                    String captcha = lianZhong.validate(captchaBase64);
                    return follow(uid, captcha);
                }
            } else {
                L.d("Follow", "成功");
                return json;
            }
        } catch (Exception e) {
            L.e("Follow", e);
        }
        return null;
    }

    /**
     * 关注
     *
     * @param uid     用户id
     * @param captcha 验证码
     * @return 用户信息
     */
    public JSONObject follow(String uid, String captcha) {
        if (!Utils.isInteger(uid)) {
            uid = getUserIdByName(uid);
            if (!Utils.isInteger(uid)) {
                return null;
            }
        }
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/create");
        String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFansContainerId());
        builder.addHeader("Referer", referer);
        String data = String.format(Locale.CHINA, "code=%suid=%s&st=%s", captcha, uid, getSt());
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.getInt("ok") == 0) {
                if (json.getString("error_type").equalsIgnoreCase("captcha")) {
                    L.d("Follow", "验证码2");
                    //验证码
                    String captchaBase64 = getCaptcha();
                    LianZhong lianZhong = new LianZhong();
                    captcha = lianZhong.validate(captchaBase64);
                    return follow(uid, captcha);
                }
            } else {
                L.d("Follow", "成功");
            }
            return json;
        } catch (Exception e) {
            L.e("Follow", e);
        }
        return null;
    }

    /**
     * 批量关注
     *
     * @param userNames 用户名
     * @return 用户信息
     */
    public int follow(List<String> userNames) {
        if (userNames.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < userNames.size(); i++) {
            Utils.sleep(1000 * 5);
            JSONObject json = follow(userNames.get(i));
            if (json != null) {
                try {
                    if (json.getInt("ok") != 1) {
                        break;
                    } else {
                        count++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }


    /**
     * 批量关注
     *
     * @param userNames 用户名
     */
    public void follow(final List<String> userNames, final WeiboCallBack weiboCallBack) {
        if (userNames.isEmpty()) {
            return;
        }

        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                result.total = userNames.size();
                result.progress = follow(userNames);
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    weiboCallBack.onSuccess(result);
                }
            }
        }.execute();
    }

    /**
     * 关注
     *
     * @param uid 用户id
     */
    public void follow(final String uid, final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                result.json = follow(uid);
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    try {
                        if (result.json.getInt("ok") != 1) {
                            weiboCallBack.onFailed(result);
                        }
                    } catch (JSONException e) {
                        L.e(e);
                    }
                    weiboCallBack.onSuccess(result);
                }
            }
        }.execute();
    }

    /**
     * 删除单向好友
     *
     * @return int[单向总数, 删除成功数]
     */
    public Integer[] deleteSingle() {
        List<JSONObject> followeds = getFolloweds();
        int single = 0;
        int success = 0;
        for (JSONObject followed : followeds) {
            try {
                String name = followed.getJSONObject("user").getString("screen_name");
                String uid = followed.getJSONObject("user").getString("id");
                int ship = followed.getJSONArray("buttons").getJSONObject(0).getInt("relationship");
                if (ship == 3) {
                    L.d("互关", name);
                } else {
                    single++;
                    JSONObject unFollow = unFollow(uid);
                    if (unFollow != null) {
                        if (unFollow.getInt("ok") != 1) {
                            L.d("UnFollow", unFollow.getString("msg"));
                            break;
                        } else {
                            success++;
                        }
                    }
                    Utils.sleep(1000 * 3);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new Integer[]{single, success};
    }


    /**
     * 删除单向用户
     */
    public void deleteSingle(final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                Integer[] res = deleteSingle();
                result.total = res[0];
                result.progress = res[1];
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    weiboCallBack.onSuccess(result);
                }
            }
        }.execute();
    }

    /**
     * 删除单向好友,不删除认证
     *
     * @return int[单向总数, 删除成功数]
     */
    public Integer[] deleteSingleWithoutVerf() {
        List<JSONObject> followeds = getFolloweds();
        int single = 0;
        int success = 0;
        int verified = 0;
        for (JSONObject followed : followeds) {
            try {
                JSONObject user = followed.getJSONObject("user");
                String name = user.getString("screen_name");
                String uid = user.getString("id");
                int ship = followed.getJSONArray("buttons").getJSONObject(0).getInt("relationship");
                if (ship == 3) {
                    L.d("互关", name);
                } else {
                    single++;
                    if (user.getString("verified").equalsIgnoreCase("true")) {
                        verified++;
                        continue;
                    }
                    JSONObject unFollow = unFollow(uid);
                    if (unFollow != null) {
                        if (unFollow.getInt("ok") != 1) {
                            L.d("UnFollow", unFollow.getString("msg"));
                            continue;
                        } else {
                            success++;
                        }
                    }
                    Utils.sleep(1000 * 3);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new Integer[]{single, success, verified};
    }


    /**
     * 删除单向好友,不删除认证
     */
    public void deleteSingleWithoutVerf(final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                Integer[] res = deleteSingleWithoutVerf();
                result.total = res[0];
                result.progress = res[1];
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    weiboCallBack.onSuccess(result);
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
                    String captchaBase64 = getCaptcha();
                    LianZhong lianZhong = new LianZhong();
                    String captcha = lianZhong.validate(captchaBase64);
                    return unFollow(uid, captcha);
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
     * 取消关注
     *
     * @param uid  用户id
     * @param code 验证码
     * @return 用户信息
     */
    public JSONObject unFollow(String uid, String code) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/friendships/destory");
        String referer = String.format(Locale.CHINA, "https://m.weibo.cn/u/%s?uid=%s&luicode=10000011&lfid=%s&featurecode=1", uid, uid, getSelfFollowedContainerId());
        builder.addHeader("Referer", referer);
        String data = String.format(Locale.CHINA, "uid=%s&st=%s&_code=%s", uid, getSt(), code);
        Request request = builder.post(getStringRequestBody(data)).build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            if (json.getInt("ok") == 0) {
                if (json.getString("error_type").equalsIgnoreCase("captcha")) {
                    //验证码
                    String captchaBase64 = getCaptcha();
                    LianZhong lianZhong = new LianZhong();
                    String captcha = lianZhong.validate(captchaBase64);
                    return unFollow(uid, captcha);
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
     * 关注粉丝
     *
     * @return int[粉丝数, 单向粉丝, 关注成功]
     */
    public Integer[] followFans() {
        List<JSONObject> fans = getFans();
        int unFollowed = 0;
        int success = 0;
        for (JSONObject followed : fans) {
            try {
                String name = followed.getJSONObject("user").getString("screen_name");
                String uid = followed.getJSONObject("user").getString("id");
                int ship = followed.getJSONArray("buttons").getJSONObject(0).getInt("relationship");
                if (ship == 3) {
                    L.d("互关", name);
                } else if (ship == 1) {
                    unFollowed++;
                    JSONObject follow = follow(uid);
                    if (follow.getInt("ok") != 1) {
                        L.d("Follow", follow.getString("msg"));
                    } else {
                        success++;
                    }
                    Utils.sleep(1000 * 3);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        L.d("粉丝", fans.size() + "");
        return new Integer[]{fans.size(), unFollowed, success};
    }

    /**
     * 关注粉丝
     */
    public void followFans(final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                WeiboResult result = new WeiboResult();
                Integer[] res = followFans();
                result.total = res[1];
                result.progress = res[2];
                return result;
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                if (weiboCallBack != null) {
                    weiboCallBack.onSuccess(result);
                }
            }
        }.execute();
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


    /**
     * 获取用户微博
     *
     * @param uid 用户id
     * @return 用户微博
     */
    public JSONArray getUserWeibo(String uid) {
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/profile/info?uid=" + uid);
        builder.addHeader("Referer", "https://m.weibo.cn/profile/" + uid);
        builder.addHeader("MWeibo-Pwa", "1");
        Request request = builder.get().build();
        try {
            Response response = client.newCall(request).execute();
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONObject("data").getJSONArray("statuses");
        } catch (Exception e) {
            L.e(e);
        }
        return null;
    }

    /**
     * 发微博
     *
     * @param content  内容
     * @param picId    图片id
     * @param visibile 1 自己可见
     */
    public WeiboResult publish(String content, String picId, String visibile) {
        WeiboResult result = new WeiboResult();
        Request.Builder builder = getDefaultHeader();
        builder.url("https://m.weibo.cn/api/statuses/update");
        builder.addHeader("Referer", "https://m.weibo.cn");
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append("content=");
        dataBuilder.append(content);
        dataBuilder.append("&st=");
        dataBuilder.append(getSt());
        if (!TextUtils.isEmpty(visibile)) {
            dataBuilder.append("&visible=");
            dataBuilder.append(visibile);
        }
        if (!TextUtils.isEmpty(picId)) {
            dataBuilder.append("&picId=");
            dataBuilder.append(picId);
        }
        Request request = builder.post(getStringRequestBody(dataBuilder.toString())).build();
        try {
            Response response = client.newCall(request).execute();
            result.json = new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发微博
     *
     * @param content  内容
     * @param picId    图片id
     * @param visibile 1 自己可见 6好友可见 ,空全部可见
     */
    public void publish(final String content, final String picId, final String visibile, final WeiboCallBack weiboCallBack) {
        new AsyncTask<Void, Void, WeiboResult>() {

            @Override
            protected WeiboResult doInBackground(Void... voids) {
                return publish(content, picId, visibile);
            }

            @Override
            protected void onPostExecute(WeiboResult result) {
                super.onPostExecute(result);
                try {
                    if (result.json.getInt("ok") == 1) {
                        weiboCallBack.onSuccess(result);
                    } else {
                        weiboCallBack.onFailed(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
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

    public String getSt() {
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
