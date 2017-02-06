package com.qiubangbang.changeskin;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * 换肤大致有两种
 * 1：写两套style切换，或者在代码中实现
 * 2：通过加载apk文件，即插件化换肤
 * 第一种较为简单 写第二种
 */
public class MainActivity extends AppCompatActivity {

    private Method method;
    private AssetManager assetManager;
    private Resources resources;
    private String pluginPackageName = "com.skin.skin_plugin";
    private RelativeLayout rlMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rlMain = (RelativeLayout) findViewById(R.id.activity_main);
        //初始化assetmanager 获取 method对象
        Class asset = AssetManager.class;
        try {
            assetManager = (AssetManager) asset.newInstance();
            method = asset.getDeclaredMethod("addAssetPath", String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resources = new Resources(assetManager, getResources().getDisplayMetrics(), getResources().getConfiguration());
    }

    public void onChangeSkinClick(View view) {
        try {
            String pluginPath = getPluginLocation();
            method.invoke(assetManager, pluginPath);
            //类加载器
            DexClassLoader dexClassLoader = new DexClassLoader(pluginPath, getDir("skinPlugin.apk",
                    Context.MODE_PRIVATE).getAbsolutePath(), null, getClassLoader());
            Class<?> c = dexClassLoader.loadClass(pluginPackageName + ".R$drawable");
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().equals("activity_bg_a")) {
                    int imgId = field.getInt(R.drawable.class);
                    Drawable drawable = resources.getDrawable(imgId);
                    rlMain.setBackgroundDrawable(drawable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPluginLocation() {
        //默认放在sd卡下面
        File file = new File(Environment.getExternalStorageDirectory(), "skinPlugin.apk");
        return file.getAbsolutePath();
    }
}
