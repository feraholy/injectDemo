package com.ry.inject;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
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
 
 //   public static final String TARGET_PATH = "/data/data/com.ry.target/lib/libtarget.so";
    
    private static final AtomicBoolean mRunning = new AtomicBoolean(false);
    
	private static final String shell(final String cmd, final String wrCmd) {
		Process process = null;
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = null;

			process = Runtime.getRuntime().exec(cmd);
			DataOutputStream dos = new DataOutputStream(
					process.getOutputStream());
			dos.writeBytes(wrCmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();

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
    
	public static final boolean isPpmByRoot() {
		final File ipm = new File("/system/bin/ipm");
		if (ipm.exists() && ipm.canExecute()) {
			final String result = shell("ipm", "echo ok");
			if (result != null && result.contains("ok")) {
				return true;
			}
		}
		return false;
	}
	
	public static final boolean isSuByRoot(){
     	 final File su = new File("/system/bin/su");
     	 return su.canExecute();
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
     */
	public static final int startHook(final Context context) {
		if (mRunning.getAndSet(true)) {
			return (0);// 正在运行
		}
		try {
			final String pack = "com.android.vending";// GP市场
			// final String pack = "com.google.android.gsf.login";//GP登录
			try {
				if (null == context.getPackageManager().getApplicationInfo(
						pack, 0)) {
					return (-1);//
				}
			} catch (Exception e) {
				return (-1);
			}

			final String basePath = context.getFilesDir().getAbsolutePath();
			final File injectPath = new File(basePath, INJECT_NAME);
			final File hookerPath = new File(basePath, HOOKER_SO_NAME);
			try {
				RWUtils.write(context.getAssets().open(INJECT_NAME), injectPath);
				RWUtils.write(context.getAssets().open(HOOKER_SO_NAME),
						hookerPath);

				Runtime.getRuntime().exec("chmod 777 " + injectPath);
				Runtime.getRuntime().exec("chmod 777 " + hookerPath);
			} catch (Exception e) {
				return (-2);
			}
			final boolean ipm = isPpmByRoot();
			final boolean su = isSuByRoot();
			if (ipm || su) {
				final File[] procs = new File("/proc").listFiles();
				if (procs != null) {
					boolean running = false;
					for (File f : procs) {
						String cmd = RWUtils.read(new File(f, "cmdline"),
								"utf-8");
						Log.e("wzh", "cmd>>" + cmd);
						if (cmd != null && cmd.contains(pack)) {
							running = true;
							break;
						}
					}
					if (!running) {
						return (-4);
					}
				}
				Log.e("wzh", "ok--------------------------");
				new Thread(new Runnable() {
					@Override
					public void run() {
						final String cmd = injectPath + " " + pack + " "
								+ hookerPath + " hook_entry hahaha";

						if (ipm) {
							shell("ipm", cmd);
							return;
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
					}
				}).start();
				return 1;
			} else {
				return (-3);
			}
		} finally {
			mRunning.set(false);
		}
	}
}
