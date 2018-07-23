package z.hobin.weibofs.net;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LianZhong {
    private OkHttpClient client = new OkHttpClient();
    private final String softwareId = "10489";
    private final String softwareSecret = "775YnZRnPpd1Tg69Fo7mSTW6Q2BecUhCx5LWh0xi";
    private final String userName = "";
    private final String password = "";
    private final String captchaType = "";

    public LianZhong() {

    }

    public String validate(String data) {
        Request.Builder builder = new Request.Builder();
        builder.addHeader("Host", "v2-api.jsdama.com");
        builder.addHeader("Connection", "keep-alive");
        builder.addHeader("Accept", "application/json, text/plain, */*");
        builder.addHeader("X-Requested-With", "XMLHttpRequest");
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        builder.addHeader("Content-Type", "text/json");
        builder.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6");
        builder.url("https://v2-api.jsdama.com/upload");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("softwareId", softwareId);
            jsonObject.put("softwareSecret", softwareSecret);
            jsonObject.put("username", userName);
            jsonObject.put("password", password);
            jsonObject.put("captchaData", data);
            jsonObject.put("captchaType", captchaType);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MediaType.parse("text/json"), jsonObject.toString());
        try {
            Response response = client.newCall(builder.post(body).build()).execute();
            String responseJson = response.body().string();
            JSONObject respJsonObject = new JSONObject(responseJson);
            return respJsonObject.getString("recognition");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
