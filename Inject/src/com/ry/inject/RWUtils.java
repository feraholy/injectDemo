package com.ry.inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class RWUtils {
	
	public static final boolean decode(InputStream in, File target)throws IOException{
		if(in == null || target == null) return false;
		
		byte[] b = new byte[1024 * 4];
		File tmp = new File(target.getPath()+".tmp");
		FileOutputStream out = null;
		try{
			target.delete();
			out = new FileOutputStream(tmp);
			do{
				int m = in.read(b);
				if(m < 0) break;
				
				for(int i=0; i<m; i++){
					b[i] = (byte)((b[i] & 0xFF) ^ 0x0055);
				}
				out.write(b, 0, m);
				out.flush();
			}while(true);
			out.close();
			out = null;
			
			return tmp.renameTo(target);
		}finally{
			b = null;
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final boolean write(String context, File target) throws IOException{
		return write(context.getBytes(), target);
	}
	
	public static final boolean write(byte[] context, File target) throws IOException{
		return write(new ByteArrayInputStream(context), target);
	}
	
	public static final boolean write(InputStream in, File target) throws IOException{
		if(in == null || target == null) return false;
		
		byte[] b = new byte[BUFF_SIZE];
		File tmp = new File(target.getPath()+".tmp");
		FileOutputStream out = null;
		try{
			target.delete();
			out = new FileOutputStream(tmp);
			do{
				int m = in.read(b);
				if(m < 0) break;
				
				out.write(b, 0, m);
				out.flush();
			}while(true);
			out.close();
			out = null;
			
			return tmp.renameTo(target);
		}finally{
			b = null;
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final int BUFF_SIZE = 1024 * 4;

	public static final byte[] read(InputStream in) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] b = new byte[BUFF_SIZE];
		try {

			do {
				int m = in.read(b);
				if (m < 0)
					break;
				out.write(b, 0, m);
				out.flush();
			} while (true);

			return out.toByteArray();
		} finally {
			b = null;
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static final String read(File f, String charsetName){
		if(f.exists()){
			try{
				byte[] bs = read(new FileInputStream(f));
				if(bs != null){
					return new String(bs, charsetName);		
				}
			}catch(Exception e){
				
			}
		}
		return null;
		
	}
	
	public static final InputStream openStream(ClassLoader loader, String resName) throws IOException{
		URL url = null;
		while(loader != null){
			if(null != (url = loader.getResource(resName))){
				break;
			}
			loader = loader.getParent();
		}
		return (url == null ? null : url.openStream());
	}
}
