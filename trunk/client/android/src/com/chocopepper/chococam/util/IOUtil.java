package com.chocopepper.chococam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtil {
	
	public static void DBtoSdcardCopy(final String dir, final String DBName) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// check 		
				File folder = new File(dir, DBName);
				File outfile = new File(Constants.SD_PATH, DBName);
				if (outfile.length() <= 0) {	
					try {	
						FileInputStream is = new FileInputStream(folder);	
						long filesize = is.available();	
						byte [] tempdata = new byte[(int)filesize];	
						is.read(tempdata); 		
						is.close();		
						if (outfile.exists())
							outfile.delete();
						outfile.createNewFile();	
						FileOutputStream fo = new FileOutputStream(outfile);
						fo.write(tempdata);			
						fo.close();	
					} catch (IOException e) { }
				}
			}
		}).start();
	}
	
	/**
	* 파일 복사
	* @param srcFile : 복사할 File
	* @param destFile : 복사될 File
	* @return
	*/
	public static boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try{
			InputStream in = new FileInputStream(srcFile);
			try{
				result = copyToFile(in, destFile);
			} finally{
				in.close();
			}
		} catch(IOException e) {
			result = false;
		}
		return result;
	}
	
	/**
	* Copy data from a source stream to destFile.
	* Return true if succeed, return false if failed.
	*/
	private static boolean copyToFile(InputStream inputStream, File destFile) {
		try{
			OutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.close();
			}
			return true;
		} catch(IOException e) {
			return false;
		}
	}
}
