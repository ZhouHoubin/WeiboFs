package z.hobin.weibofs;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;
import z.hobin.weibofs.net.Weibo;
import z.hobin.weibofs.util.Utils;

public class MainActivity extends AppCompatActivity {
    private String cookie;
    private int widthPixels;
    private Drawable d;
    private List<String> userNames = new ArrayList<>();
    private List<String> comments = new ArrayList<>();
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthPixels = displayMetrics.widthPixels;

        TextView title = findViewById(R.id.action_title);
        title.setText("小微助手");
        findViewById(R.id.actionbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GridView grid = findViewById(R.id.grid);
        List<String> list = new ArrayList<>();
        list.add("关注");//单人关注,多人关注,删除单项好友(排除特别关注,认证用户),贴吧抓取
        list.add("粉丝");//移除粉丝//关注粉丝
        list.add("消息");//批量消息,分组消息
        list.add("点赞");//首页点赞//自动点赞//
        list.add("重新登录");
        list.add("帮助");
        grid.setAdapter(new MainGridAdapter(list));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                switch (position) {
                    case 0://关注
                        AlertDialog.Builder followBuilder = new AlertDialog.Builder(MainActivity.this);
                        followBuilder.setTitle("关注");
                        followBuilder.setSingleChoiceItems(new CharSequence[]{"关注一个人", "关注多个人", "删除没有关注我的人", "删除没有关注我的人(不包括认证用户)", "贴吧帖子提取关注"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        AlertDialog.Builder followBuilder = new AlertDialog.Builder(MainActivity.this);
                                        followBuilder.setTitle("输入用户名");
                                        final EditText editText = new EditText(getApplicationContext());
                                        followBuilder.setView(editText);
                                        followBuilder.setPositiveButton("关注", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String userName = editText.getText().toString();
                                                if (TextUtils.isEmpty(userName)) {
                                                    Toast.makeText(MainActivity.this, "输入对方微博名", Toast.LENGTH_SHORT).show();
                                                } else {

                                                }
                                            }
                                        });
                                        followBuilder.setNegativeButton("取消", null);
                                        followBuilder.show();
                                        break;
                                    case 1:
                                        Intent gotoMultiFollow = new Intent(getApplicationContext(), FollowActivity.class);
                                        startActivity(gotoMultiFollow);
                                        break;
                                    case 2:
                                        AlertDialog.Builder deleteAllBuilder = new AlertDialog.Builder(MainActivity.this);
                                        deleteAllBuilder.setTitle("删除没有关注我的人");
                                        deleteAllBuilder.setMessage("操作会删除所有没有关注我的人,保留关注我的人,可能会删除一些明星#官微#认证用户,是否继续?");
                                        deleteAllBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });

                                        deleteAllBuilder.setNegativeButton("取消", null);
                                        deleteAllBuilder.show();
                                        break;
                                    case 3:
                                        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(MainActivity.this);
                                        deleteBuilder.setTitle("删除没有关注我的人(不包括认证用户)");
                                        deleteBuilder.setMessage("操作会删除所有没有关注我的人,保留关注我的人,不删除明星#官微#认证用户,是否继续?");
                                        deleteBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        deleteBuilder.setNegativeButton("取消", null);
                                        deleteBuilder.show();
                                        break;
                                    case 4:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        followBuilder.show();
                        break;
                    case 1://粉丝
                        final AlertDialog.Builder fansBuilder = new AlertDialog.Builder(MainActivity.this);
                        fansBuilder.setTitle("粉丝");
                        fansBuilder.setSingleChoiceItems(new CharSequence[]{"关注粉丝", "移除粉丝"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which) {
                                    case 0:
                                        AlertDialog.Builder followBuilder = new AlertDialog.Builder(MainActivity.this);
                                        followBuilder.setTitle("关注粉丝");
                                        followBuilder.setMessage("此操作会关注所有的粉丝,是否继续?");
                                        followBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        followBuilder.setNegativeButton("取消", null);
                                        followBuilder.show();
                                        break;
                                    case 1:
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        fansBuilder.show();
                        break;
                    case 2://消息
                        final AlertDialog.Builder messageBuilder = new AlertDialog.Builder(MainActivity.this);
                        messageBuilder.setTitle("消息");
                        messageBuilder.setSingleChoiceItems(new CharSequence[]{"批量发送消息", "分组发消息"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        messageBuilder.show();
                        break;
                    case 3://点赞
                        final AlertDialog.Builder likeBuilder = new AlertDialog.Builder(MainActivity.this);
                        likeBuilder.setTitle("点赞");
                        likeBuilder.setSingleChoiceItems(new CharSequence[]{"首页点赞", "自动点赞", "粉丝点赞"}, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        likeBuilder.show();
                        break;
                    case 4://重新登录
                        break;
                    case 5://帮助
                        break;
                    default:
                        break;
                }

                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Weibo weibo = new Weibo();
                        switch (position) {
                            case 0://删除单项
                                List<JSONObject> followeds = weibo.getFolloweds();
                                int c = 0;
                                for (JSONObject followed : followeds) {
                                    try {
                                        String name = followed.getJSONObject("user").getString("screen_name");
                                        String uid = followed.getJSONObject("user").getString("id");
                                        int ship = followed.getJSONArray("buttons").getJSONObject(0).getInt("relationship");
                                        if (ship == 3) {
                                            c++;
                                            L.d("互关", name);
                                        } else {
                                            JSONObject unFollow = weibo.unFollow(uid);
                                            if (unFollow.getInt("ok") != 1) {
                                                Message message = new Message();
                                                message.what = 0;
                                                message.obj = "取消关注异常," + unFollow.getString("msg");
                                                handler.sendMessage(message);
                                                break;
                                            }
                                            Utils.sleep(1000 * 3);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                L.d("关注", followeds.size() + "");
                                break;
                            case 1://关注粉丝
                                List<JSONObject> fans = weibo.getFans();
                                for (JSONObject followed : fans) {
                                    try {
                                        String name = followed.getJSONObject("user").getString("screen_name");
                                        String uid = followed.getJSONObject("user").getString("id");
                                        int ship = followed.getJSONArray("buttons").getJSONObject(0).getInt("relationship");
                                        if (ship == 3) {
                                            L.d("互关", name);
                                        } else if (ship == 1) {
                                            JSONObject follow = weibo.follow(uid);
                                            if (follow.getInt("ok") != 1) {
                                                Message message = new Message();
                                                message.what = 0;
                                                message.obj = "关注异常," + follow.getString("msg");
                                                handler.sendMessage(message);
                                                break;
                                            }
                                            Utils.sleep(1000 * 3);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                L.d("粉丝", fans.size() + "");
                                break;
                            case 2://消息私信
                                weibo.sendMsg("2099181812", System.currentTimeMillis() + "");
                                break;
                            case 3://批量关注私信广告
                                for (String userName : userNames) {
                                    String userId = weibo.getUserIdByName(userName);
                                    if (!TextUtils.isEmpty(userId)) {
                                        try {
                                            JSONObject follow = weibo.follow(userId);
                                            Utils.sleep(1000 * 2);
                                            weibo.sendMsg(userId, userName + " 已经关注你了哦    ~~~~~来自贴吧");
                                            Utils.sleep(1000 * 2);
                                            weibo.sendMsg(userId, userName + " 互粉互关Q group  786122281");
                                            Utils.sleep(1000 * 2);
                                            L.d("关注+私信", userName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    try {
                                        Thread.sleep(1000 * 60);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            case 4://批量关注评论点赞第一条
                                for (String userName : userNames) {
                                    String userId = weibo.getUserIdByName(userName);
                                    if (!TextUtils.isEmpty(userId)) {
                                        try {
                                            JSONObject follow = weibo.follow(userId);
                                            List<String> msgs = new ArrayList<>();

//                                            Random random = new Random();
//                                            String comment = comments.get(random.nextInt(comments.size() - 1));
//                                            StringBuilder builder = new StringBuilder(comment);
//                                            builder.insert(random.nextInt(comment.length() - 1), " 互粉qun 786122281 ");
//                                            msgs.add(builder.toString());

                                            msgs.add(" 已粉 " + userName);
                                            msgs.add(" 已赞 " + userName);
                                            //weibo.commentLike(userId, null);
                                            L.d("关注+评论", userName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    try {
                                        Thread.sleep(1000 * 10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            case 5:
                                Caches.get().clear();
                                onResume();
                                break;
                            default:
                                break;
                        }
                    }
                };//.start();
            }
        });

        WallpaperManager wm = WallpaperManager.getInstance(this);
        d = wm.getDrawable();
        userNames.add("青梅茱萸");
        userNames.add("前前前男友_");
        userNames.add("暖胸");
        userNames.add("林中饮歌");
        userNames.add("柚子皮啊_");
        userNames.add("我吃不下啦m");
        userNames.add("划勒巴子");
        userNames.add("后给六天");
        userNames.add("稚杀");
        userNames.add("外烟喵呜酱");
        userNames.add("-高寒-");
        userNames.add("送你三条咸鱼");
        userNames.add("勼欢啊-");
        userNames.add("是球球叭");
        userNames.add("郑的言yy");
        userNames.add("深锞");
        userNames.add("酷甜甜_");
        userNames.add("三巳雨生");
        userNames.add("七囍Sevani");
        userNames.add("玲丫仙女");
        userNames.add("飛昇的壳");
        userNames.add("野馬CHEN");
        userNames.add("逺枳");
        userNames.add("小廖");
        userNames.add("好爱撒娇");
        userNames.add("诱真");
        userNames.add("乔夢婉");
        userNames.add("黒惡");
        userNames.add("kiiiid");
        userNames.add("想和你私通");
        userNames.add("killout");
        userNames.add("家育");
        userNames.add("爱吃麦子的鱼QvQ");
        userNames.add("GuccQ_");
        userNames.add("三好有点慢");
        userNames.add("正月拾肆_");
        userNames.add("天下莫有阻者");
        userNames.add("庸欲染");
        userNames.add("罚酒");
        userNames.add("你好是王哥");
        userNames.add("外烟喵呜酱");
        userNames.add("前前前男友_");
        userNames.add("麓识");
        userNames.add("Engrais");
        userNames.add("谭子煊");
        userNames.add("反射弧战士YoMuLa");
        userNames.add("仙女琼_");
        userNames.add("loKaCo");
        userNames.add("imxq_y");
        userNames.add("玥玥醒了");
        userNames.add("月儿弯弯呃");
        userNames.add("冠希不在犯错");
        userNames.add("是个局外人-");
        userNames.add("软君子");
        userNames.add("怡橙不染");
        userNames.add("异端论");
        userNames.add("阿康SAMA");
        userNames.add("麓识");
        userNames.add("盐画画");
        userNames.add("宛若一只熊");
        userNames.clear();
        //===============================
        userNames.add("一米九的朋友");
        userNames.add("Tebgy");
        userNames.add("b霖-");
        userNames.add("临江独倚");
        userNames.add("无意识共振");
        userNames.add("羊羊有点瘦");
        userNames.add("你愿意让我抱抱你吗");
        userNames.add("_陈婉珊");
        userNames.add("冒气泡");
        userNames.add("生夏潤一");
        userNames.add("黎念NL");
        userNames.add("即兴想你");
        userNames.add("皮特机长");
        userNames.add("郝二狗子");
        userNames.add("Vasity-");
        userNames.add("你很瘦吗");
        userNames.add("携壶与客");
        userNames.add("戰狗·");
        userNames.add("慕秦策");
        userNames.add("江悸");
        userNames.add("一月是诺诺啊");
        userNames.add("小仙女嘤嘤嘤嘤");
        userNames.add("edico王猛");
        userNames.add("秋秋小不点儿");
        userNames.add("庸欲染");
        userNames.add("余姝漫");
        userNames.add("再见不送大猪蹄子");
        userNames.add("_immy");
        userNames.add("肆眄睐");
        userNames.add("神代利世o");
        userNames.add("大丑程晏");
        userNames.add("是救赎阿");
        userNames.add("别叫沫沫");
        userNames.add("叫向广");
        userNames.add("白栀_Erin");
        userNames.add("林中饮歌");
        userNames.add("第1999页");
        userNames.add("苑阿歆");
        userNames.add("L45-");
        userNames.add("奈若何001hee");
        userNames.add("言语不凡");
        userNames.add("猪军臣");
        userNames.add("够运");
        userNames.add("熊扑一怀");
        userNames.add("_Engh1");
        userNames.add("是野九阿");
        userNames.add("投身长夜");
        userNames.add("你好看个屁");
        userNames.add("梨十五");
        userNames.add("羔食青");
        userNames.add("WYMYiaaa-");
        userNames.add("洋鑫宋-");
        userNames.add("白衬萝莉");
        userNames.add("唯一的陆小萱");
        userNames.add("奶酪少女bd");
        userNames.add("林以然9");
        userNames.add("邢圆圆XY");
        userNames.clear();
        //====================================
        userNames.add("吖酱哟");
        userNames.add("扑扑兔叽");
        userNames.add("HI赫本");
        userNames.add("是呀我喜欢你");
        userNames.add("光暖小宇");
        userNames.add("生命的奇迹在这里");
        userNames.clear();
        //----------------------------
        userNames.add("风中一朵小墨花");
        userNames.add("山水有_清音");
        userNames.add("不问归期呀");
        userNames.add("僧糜");
        userNames.add("7Winks");
        userNames.add("缺氧的鱼5zy");
        userNames.add("桥本环熊");
        userNames.add("-代大萍");
        userNames.add("牛郎猪女_");
        userNames.add("趁梨花");
        userNames.add("诗糖谦心");
        userNames.add("混世大魔王不胡闹");
        userNames.add("Felice_Noordhoff");
        userNames.clear();
        //====================================
        userNames.add("_Asadrealty");
        userNames.add("绿癯");
        userNames.add("张起灵带我去倒斗");
        userNames.add("Felice_Noordhoff");
        userNames.add("秋沐梓");
        userNames.add("孤猫慵懒-");
        userNames.add("宠了吧唧");
        userNames.add("R-iizwangvl");
        userNames.add("-川月同游-");
        userNames.add("荣泰健康生活");
        userNames.add("H呐呐na");
        userNames.add("-ReNTy");
        userNames.add("花子声");
        userNames.add("一点点幼稚");
        userNames.add("风中一朵小墨花");
        userNames.add("伏珂珂珂Fyy");
        userNames.add("-清露踏涟漪");
        userNames.add("廷祐");
        userNames.add("桥本环熊");
        userNames.add("王一口啊");
        userNames.add("陳陳Zz_");
        userNames.add("阿柜婷婷");
        userNames.add("-代大萍");
        userNames.add("南瑾cc");
        userNames.add("Cat猫猫w");
        userNames.add("诗糖谦心");
        userNames.add("粉扑扑兔叽");
        userNames.clear();
        add("北北要矜持啊");
        add("心匠丶");
        add("三好有点慢");
        add("孤立人格");
        add("演吻");
        add("荔荔夏");
        add("清水叽叽");
        add("dongzi");
        add("llouo");
        add("大腿姐姐_");
        add("橘子-pp");
        add("持久");
        add("单纯正直的四娃");
        add("沈废三");
        add("如果儿的小号");
        add("阿姨洗铁路啊西");
        add("睡覺起來去玩");
        add("最宝贝的y");
        add("蔡徐坤ol");
        add("甜溺v");
        add("暧胸");
        add("吉時行樂");
        add("林家小聪");
        add("Estelle_Smith");
        add("反射弧战士YoMuLa");
        add("独渡山河");
        add("宛若一只熊");
        add("陳陳Zz_");
        add("饶邦梁");
        add("白龙居居");
        add("emdog");
        add("林更新女朋友耶");
        add("而已eyi");
        add("misheng");
        add("岳吗");
        add("抱你满怀");
        add("姬淮-");
        add("西嬧");
        add("捕梦陳");
        add("_TeFuirL");
        add("偷走芝士");
        add("癫癫啊酱");
        userNames.clear();
        //------------------------
        add("像最后一样");
        add("每天都李小迷");
        add("MkamYu-");
        add("ljyyii");
        add("长眉不似山");
        add("卜叽叽叽叽-");
        add("别躲啊_");
        add("邚味");
        add("过场·");
        add("WEIFUY_");
        add("b霖-");
        add("什一11");
        add("一杯抹茶拿铁儿");
        add("wuli心洁");
        add("段美汝");
        add("南巷十七");
        add("风与鸟");
        add("黎念NL");
        add("陳凯蒂_");
        add("amglz");
        add("春事爱远仪");
        add("麦迪辰夕的故事");
        add("几个荼");
        add("粉红少女Ra");
        add("司杌");
        add("谓贤_");
        add("像最后一样");
        add("大丑程晏");
        add("只有三岁的章若涵");
        add("陈阿秋阿");
        add("捏脸小语");
        add("几身风尘");
        add("长眉不似山");
        add("一只小猪向前跑");
        add("忱温的猫");
        add("野生香菜儿");
        add("吉時行樂");
        add("我喜欢的你喜欢我");
        add("南亦北也");
        add("宛若一只熊");
        add("眉目款款-");
        add("朴所罗门是我的");
        add("癫癫啊酱");
        add("嗯是阿姨");
        add("别躲啊_");
        add("今天练爱了吗");
        add("Xarnud_yi");
        add("废了个丢");
        add("wuli心洁");
        add("丧人三五三");
        add("steady-波");
        add("吴市的箫");
        add("一个什么都发的涵雨");
        add("周宇PG");
        add("风与鸟");
        add("崔三岁同学");
        add("酸奶女人a");

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.comments);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                comments.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void add(String name) {
        if (!userNames.contains(name)) {
            userNames.add(name);
        }
    }

    private class MainGridAdapter extends BaseAdapter {
        private List<String> items = new ArrayList<>();

        public MainGridAdapter(List<String> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.item_grid, null);
                holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.title);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(getItem(position));
            GridView.LayoutParams param = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, widthPixels / 2);
            convertView.setLayoutParams(param);
            //convertView.setBackground(d);
            return convertView;
        }

        private class ViewHolder {
            private TextView title;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cookie = Caches.get().getAsString("Cookie");
        if (TextUtils.isEmpty(cookie)) {
            Toast.makeText(this, "需要登录", Toast.LENGTH_SHORT).show();
            gotoLogin();
        }
    }

    private void gotoLogin() {
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
    }

    public void follow(View view) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                Weibo weibo = new Weibo();

            }
        }.start();
    }
}
