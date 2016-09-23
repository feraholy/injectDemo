package com.ry.inject;


import java.io.IOException;

import com.ry.inject.service.FloatWindowService;

import dalvik.system.PathClassLoader;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
        
        finish();
    }
    
    
}
