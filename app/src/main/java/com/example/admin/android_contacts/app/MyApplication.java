package com.example.admin.android_contacts.app;

import android.app.Application;

import com.example.admin.android_contacts.bean.SortModel;

/**
 * Created by ${LEO} on 2018/12/27.
 */

public class MyApplication extends Application {
    public static SortModel sortModel;//存储联系人对象（用在联系人信息）

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
