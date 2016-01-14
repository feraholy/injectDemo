package com.ry.inject;


import java.io.File;
import android.content.Context;

/**
 * @Title: JNI.java
 * @Description:
 * @author yangbing3@ucweb.com
 * @date 2014-9-26 2:29:06
 */
public class JNI {
	

    public static final String INJECT_NAME = "inject";
    
    public static final String HOOKER_SO_NAME = "libhooker.so";
 
 //   public static final String TARGET_PATH = "/data/data/com.ry.target/lib/libtarget.so";
    
	public void startHook(final Context context) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                 
            	 String basePath = context.getFilesDir().getAbsolutePath();
            	 
               	 File injectPath = new File(basePath, INJECT_NAME);
                 File hookerPath = new File(basePath, HOOKER_SO_NAME);
            	 try{
                     RWUtils.write(context.getAssets().open(INJECT_NAME), injectPath);
                     RWUtils.write(context.getAssets().open(HOOKER_SO_NAME), hookerPath);
            	 }catch(Exception e){
            		 return ;
            	 }
                 try {
                	 String[] commands = new String[3];
                	
                	 commands[0] = "chmod 777 " + injectPath;
                	 commands[1] = "chmod 777 " + hookerPath;
                	 Runtime.getRuntime().exec(commands[0]);
                	 Runtime.getRuntime().exec(commands[1]);
//                	 commands[2] = "chmod 777 " + substratePath;
//                	 commands[3] = "chmod 777 " + TARGET_PATH;
                	 
                	 final String pack = "com.android.vending";//GP市场
//                	 final String pack = "com.google.android.gsf.login";//GP登录
                	 StringBuffer sb = new StringBuffer();
                	 sb.append("su -c");
                	 sb.append(" ").append(injectPath);//注入程序
                	 sb.append(" ").append(pack);//目标进程名称
                	 sb.append(" ").append(hookerPath);//注入代码so
                	 sb.append(" ").append("hook_entry");//注入代码入口函数
                	 sb.append(" ").append("hahaha");//注入代码入口函数参数
                	 
                	 commands[2] = sb.toString();
                	 System.out.println(commands[2]);
//                	 ShellUtils.execCommand(commands, true);
                	 Runtime.getRuntime().exec(new String[]{
                			 "su",
                			 "-c",
                			 injectPath + " "+pack+" " + hookerPath + " hook_entry hahaha",
                	 });

				} catch (Exception e) {
					e.printStackTrace();
				}


            }
        }).start();

	}
}
