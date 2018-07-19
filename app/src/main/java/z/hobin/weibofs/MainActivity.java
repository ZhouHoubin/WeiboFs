package z.hobin.weibofs;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import z.hobin.weibofs.data.Caches;
import z.hobin.weibofs.log.L;
import z.hobin.weibofs.net.Weibo;

public class MainActivity extends AppCompatActivity {
    private String cookie;
    private int widthPixels;
    private Drawable d;
    private List<String> userNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        widthPixels = displayMetrics.widthPixels;

        GridView grid = findViewById(R.id.grid);
        List<String> list = new ArrayList<>();
        list.add("关注");
        list.add("取消关注");
        list.add("消息");
        list.add("批量关注并私信");
        list.add("批量关注点赞评论");
        grid.setAdapter(new MainGridAdapter(list));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Weibo weibo = new Weibo();
                        switch (position) {
                            case 0://关注
                                weibo.follow("2099181812");
                                break;
                            case 1://取消关注
                                weibo.unfollow("2099181812");
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
                                            weibo.sendMsg(userId, userName + " 已经关注你了哦    ~~~~~来自贴吧");
                                            weibo.sendMsg(userId, userName + " 互粉互关Q group  786122281");
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
                                            msgs.add(userName + " 互粉互关Q 裙 ~~~ 786122281");
                                            msgs.add(userName + " 已粉  ~~~~~来自贴吧");
                                            msgs.add(userName + " 已赞");
                                            weibo.commentLike(userId, msgs);
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
                            default:
                                break;
                        }
                    }
                }.start();
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
