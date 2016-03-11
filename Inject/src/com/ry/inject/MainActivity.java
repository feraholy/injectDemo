package com.ry.inject;


import java.io.IOException;

import com.ry.inject.service.FloatWindowService;

import dalvik.system.PathClassLoader;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * @Title: MainActivity.java
 * @Description:
 * @author yangbing3@ucweb.com
 * @date 2014-9-26 2:14:49
 */
public class MainActivity extends Activity  {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.float_window);
        startService(new Intent(this,FloatWindowService.class));
//        System.loadLibrary("hooker");
////        this.finish();
//		 final String cmd2 = "ppm =abcdefg= " +getFilesDir()+ " -i " + "/sdcard/ssp.apk /data/app/com.togic.livevideo.apk";
//		 try {
//			Runtime.getRuntime().exec(cmd2).waitFor();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Log.e("wzh", ">>>"+0);
//        JNI.startHook(this);
    }
}
