package xunqaing.bwie.com.day0528_oldyuekaob;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xunqaing.bwie.com.day0528_oldyuekaob.bean.bean;
import xunqaing.bwie.com.xlistview.XListView;

import static android.app.ProgressDialog.STYLE_HORIZONTAL;

public class MainActivity extends Activity implements XListView.IXListViewListener {

    private XListView xListView;


    private List<String> list = new ArrayList<>();
    private List<String> listurl = new ArrayList<>();
    private MyAdapter adapter;
    private ProgressBar progressBar;
    // 声明进度条对话框
    ProgressDialog xh_pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xListView = (XListView) findViewById(R.id.listview);

        initData(true);

        xListView.setPullRefreshEnable(true);
        xListView.setPullLoadEnable(true);
        xListView.setXListViewListener(this);

        final String[] items = new String[]{"手机流量", "wifi"};

        xListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                // 创建对话框构建器
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.drawable.notification_bg_normal_pressed)
                        .setTitle("网络设置")
                        .setMultiChoiceItems(items,
                                new boolean[]{false, false},
                                new DialogInterface.OnMultiChoiceClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which, boolean isChecked) {

                                        switch (which) {

                                            case 0:

                                                dialog.dismiss();

                                                // 创建构建器
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                // 设置参数
                                                builder.setTitle("版本更新").setIcon(R.drawable.abc_textfield_activated_mtrl_alpha)
                                                        .setMessage("现在检测到新版本，是否更新")
                                                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {

                                                            @Override
                                                            public void onClick(DialogInterface dialog,
                                                                                int which) {

                                                            }
                                                        }).setNegativeButton("确定", new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {


                                                        // 创建ProgressDialog对象
                                                        xh_pDialog = new ProgressDialog(MainActivity.this);
                                                        View view = View.inflate(MainActivity.this,R.layout.progressbar,null);
                                                        xh_pDialog.setView(view);
                                                        xh_pDialog.setProgressStyle(STYLE_HORIZONTAL);
                                                        xh_pDialog.show();


                                                        String path = Environment.getExternalStorageDirectory().getPath() + "/"+list.get(position)+".apk";
                                                        //2.发送请求，获取apk，并放到指定路径
                                                        RequestParams rp = new RequestParams(listurl.get(position-1));
                                                        rp.setSaveFilePath(path);
                                                        rp.setAutoRename(true);


                                                        x.http().get(rp, new Callback.ProgressCallback<File>() {
                                                            //下载成功
                                                            @Override
                                                            public void onSuccess(File result) {
                                                                Toast.makeText(MainActivity.this, "下载完成,开始安装!", Toast.LENGTH_SHORT).show();
                                                                installApk(result);
                                                            }

                                                            //下载出现问题
                                                            @Override
                                                            public void onError(Throwable ex, boolean isOnCallback) {
                                                            }

                                                            @Override
                                                            public void onCancelled(CancelledException cex) {

                                                            }

                                                            @Override
                                                            public void onFinished() {

                                                                xh_pDialog.dismiss();
                                                            }

                                                            @Override
                                                            public void onWaiting() {
                                                            }

                                                            //刚刚开始下载
                                                            @Override
                                                            public void onStarted() {
                                                                Log.v("tag", "开始");
                                                            }

                                                            //下载过程中方法
                                                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                                            @Override
                                                            public void onLoading(long total, long current, boolean isDownloading) {

                                                                // 创建一个数值格式化对象
                                                                NumberFormat numberFormat = NumberFormat.getInstance();
                                                                // 设置精确到小数点后2位
                                                                numberFormat.setMaximumFractionDigits(2);
                                                                String result = numberFormat.format((float) current / (float) total * 100);
                                                                Double a = Double.valueOf(result);
                                                                int b = (int) Math.round(a.doubleValue());
                                                                xh_pDialog.setProgress(b);
                                                                Log.d("msg", Integer.valueOf(result) + "====百分之多少=====");


                                                            }
                                                        });
                                                    }
                                                });


                                                builder.create().show();

                                                break;

                                            case 1:

                                                if (isChecked == true) {

                                                    dialog.dismiss();

                                                    Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                                    startActivity(intent);
                                                }

                                                break;
                                        }
                                    }
                                });


                builder.create().show();


            }
        });

    }

    private void installApk(File file) {
        //系统应用界面,源码,安装apk入口
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //文件作为数据源
        //设置安装的类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);

    }


    private void initData(final boolean b) {

        RequestParams rp = new RequestParams("http://mapp.qzone.qq.com/cgi-bin/mapp/mapp_subcatelist_qq?yyb_cateid=-10&categoryName=%E8%85%BE%E8%AE%AF%E8%BD%AF%E4%BB%B6&pageNo=1&pageSize=20&type=app&platform=touch&network_type=unknown&resolution=412x732");

        x.http().get(rp, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                String[] aa = result.split(";");

                String s = aa[0];

                bean bean = JSON.parseObject(s, bean.class);

                List<xunqaing.bwie.com.day0528_oldyuekaob.bean.bean.AppBean> app = bean.getApp();

                for (xunqaing.bwie.com.day0528_oldyuekaob.bean.bean.AppBean xa : app) {

                    list.add(xa.getName());
                    listurl.add(xa.getUrl());

                }
                Log.e("=====", list.size() + "");

                if (b == true) {

                    adapter = new MyAdapter();
                    xListView.setAdapter(adapter);

                } else {

                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

                Log.e("=====", "onError");

            }

            @Override
            public void onCancelled(CancelledException cex) {


            }

            @Override
            public void onFinished() {

                Log.e("=====", "onFinished");
            }
        });

    }

    @Override
    public void onRefresh() {

        list.clear();
        adapter.notifyDataSetChanged();

        initData(true);

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
        xListView.setRefreshTime(simpleDateFormat.format(date));

        xListView.stopRefresh();
    }

    @Override
    public void onLoadMore() {

    }

    class MyAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder vh;

            if (convertView == null) {

                vh = new ViewHolder();

                convertView = View.inflate(MainActivity.this, R.layout.listview, null);

                vh.tv = (TextView) convertView.findViewById(R.id.listview_tv);

                convertView.setTag(vh);

            } else {

                vh = (ViewHolder) convertView.getTag();
            }
            vh.tv.setText(list.get(position));

            return convertView;
        }

        class ViewHolder {

            TextView tv;
        }
    }
}
