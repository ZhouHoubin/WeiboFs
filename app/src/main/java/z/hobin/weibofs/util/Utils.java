package z.hobin.weibofs.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.net.SimpleCallBack;
import z.hobin.weibofs.net.Weibo;

public class Utils {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String random(int length) {
        //定义一个字符串（A-Z，a-z，0-9）即62位；
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //由Random生成随机数
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        //长度为几就循环几次
        for (int i = 0; i < length; ++i) {
            //产生0-61的数字
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static String trimHtml(String html) {
        html = html.replaceAll("\n", "");
        html = html.replaceAll("<br />", "\r\n");
        StringBuilder htmlBuilder = new StringBuilder();
        Document document = Jsoup.parse(html);
        System.out.println();
        Element body = document.getElementsByTag("body").get(0);
        List<Node> nodes = body.childNodes();
        for (Node node : nodes) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                htmlBuilder.append(textNode.text());
            } else if (node instanceof Element) {
                Element element = (Element) node;
                htmlBuilder.append(element.text());
                Elements imgElements = ((Element) node).getElementsByTag("img");
                for (int i = 0; i < imgElements.size(); i++) {
                    String alt = imgElements.get(i).attr("alt");
                    htmlBuilder.append(alt);
                }
            }
        }
        return htmlBuilder.toString();
    }

    public static void get(String url, final SimpleCallBack callBack) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(url).get();
        client.newCall(builder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onFailed(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callBack.onSuccess(response.body().string());
            }
        });
    }

    public static String uploadPicture(Bitmap bitmap) {
        byte[] data = bitmap2Bytes(bitmap);
        String actionUrl = "https://m.weibo.cn/api/statuses/uploadPic";

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "----WebKitFormBoundary" + random(16);

        DataOutputStream ds = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;

        try {
            // 统一资源
            URL url = new URL(actionUrl);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;

            // 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setDoInput(true);
            // 设置是否向httpUrlConnection输出
            httpURLConnection.setDoOutput(true);
            // Post 请求不能使用缓存
            httpURLConnection.setUseCaches(false);
            // 设定请求的方法，默认是GET
            httpURLConnection.setRequestMethod("POST");
            // 设置字符编码连接参数
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 设置请求内容类型
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            httpURLConnection.setRequestProperty("Host", "m.weibo.cn");
            httpURLConnection.setRequestProperty("Origin", "https://m.weibo.cn");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
            httpURLConnection.setRequestProperty("Accept", "application/json, text/plain, */*");
            httpURLConnection.setRequestProperty("MWeibo-Pwa", "1");
            httpURLConnection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            httpURLConnection.setRequestProperty("DNT", "1");
            httpURLConnection.setRequestProperty("Referer", "https://m.weibo.cn/compose/");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Cookie", Caches.get().getAsString("Cookie"));
            // 设置DataOutputStream
            StringBuilder builder = new StringBuilder();
            ds = new DataOutputStream(httpURLConnection.getOutputStream());
            ds.writeBytes(twoHyphens + boundary + end);
            builder.append(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; name=\"type\"" + end);
            builder.append("Content-Disposition: form-data; name=\"type\"" + end);
            ds.writeBytes(end);
            builder.append(end);
            ds.writeBytes("json");
            builder.append("json");
            ds.writeBytes(end);
            builder.append(end);


            ds.writeBytes(twoHyphens + boundary + end);
            builder.append(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; " + "name=\"pic\";filename=\"a.jpg\"" + end);
            builder.append("Content-Disposition: form-data; " + "name=\"pic\";filename=\"a.jpg\"" + end);
            ds.writeBytes("Content-Type: image/jpeg");
            builder.append("Content-Type: image/jpeg");
            ds.writeBytes(end);
            builder.append(end);
            ds.write(data, 0, data.length);
            builder.append("xxxxxx");
            ds.writeBytes(end);
            builder.append(end);
            //ds.writeBytes(twoHyphens + boundary + twoHyphens + end);

            ds.writeBytes(twoHyphens + boundary + end);
            builder.append(twoHyphens + boundary + end);
            ds.writeBytes("Content-Disposition: form-data; name=\"st\"" + end);
            builder.append("Content-Disposition: form-data; name=\"st\"" + end);
            ds.writeBytes(end);
            builder.append(end);
            ds.writeBytes(new Weibo().getSt());
            builder.append(new Weibo().getSt());
            ds.writeBytes(end);
            builder.append(end);
            ds.writeBytes(twoHyphens + boundary + twoHyphens + end);
            builder.append(twoHyphens + boundary + twoHyphens + end);

            /* close streams */
            ds.flush();
            System.out.println(builder.toString());
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception(
                        "HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                tempLine = null;
                resultBuffer = new StringBuffer();
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                    resultBuffer.append("\n");
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (ds != null) {
                try {
                    ds.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return resultBuffer.toString();
        }
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    public static byte[] bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }

}
