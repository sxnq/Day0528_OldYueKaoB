package xunqaing.bwie.com.day0528_oldyuekaob;

import android.app.Application;

import org.xutils.x;

/**
 * Created by : Xunqiang
 * 2017/5/28 08:14
 */

public class IApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        x.Ext.init(this);
        //输出Deb日志
        x.Ext.setDebug(BuildConfig.DEBUG);
    }
}
