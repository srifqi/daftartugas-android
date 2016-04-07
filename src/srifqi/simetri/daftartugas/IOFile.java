package srifqi.simetri.daftartugas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import android.content.Context;

/**
 * A class for read and write to a file.
 */
public class IOFile {
	
	/**
	 * Read a file as {@link String}.
	 * 
	 * @param ctx
	 *			The context of the file.
	 * @param path
	 *			The path to the file.
	 * 
	 * @return The content of the file.
	 */
	public static String read(Context ctx, String path) {
		File file = new File(ctx.getFilesDir(), path);
		StringBuilder ftext = new StringBuilder();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while((line = br.readLine()) != null) {
				ftext.append(line);
				ftext.append("\n");
			}
			try {
				ftext.deleteCharAt(ftext.length()-1);
			} catch (StringIndexOutOfBoundsException e) {
				// Empty string?
			}
			
			br.close();
		} catch (IOException e) {
			// Failed to read
		}
		
		return ftext.toString();
	}
	
	/**
	 * Write to a file from {@link String}.
	 * 
	 * @param ctx
	 *			The context of the file.
	 * @param path
	 *			The path to the file.
	 * @param text
	 *			The content to be written to the file.
	 * 
	 * @return true on success, false if failed.
	 */
	public static boolean write(Context ctx, String path, String text) {
		FileOutputStream outstream;
		
		try {
			outstream = ctx.openFileOutput(path, Context.MODE_PRIVATE);
			outstream.write(text.getBytes());
			outstream.close();
		} catch (Exception e) {
			// Failed to save
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get timestamp of the time last modified of a file.
	 * 
	 * @param ctx
	 *			The context of the file.
	 * @param path
	 *			The path to the file.
	 * 
	 * @return timestamp of the time last modified.
	 */
	public static long mtime(Context ctx, String path) {
		File file = new File(ctx.getFileStreamPath(path).getAbsolutePath());
		return file.lastModified();
	}
	/*// Not really needed for now.
	public static String eread(Context ctx, String dirpath, String path) {
		File dir = new File(
			Environment.getExternalStorageDirectory() +
			"/" + dirpath
		);
		dir.mkdirs();
		File file = new File(dir, path);
		StringBuilder ftext = new StringBuilder();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			
			while((line = br.readLine()) != null) {
				ftext.append(line);
				ftext.append("\n");
			}
			try {
				ftext.deleteCharAt(ftext.length()-1);
			} catch (StringIndexOutOfBoundsException e) {
				// Empty string?
			}
			
			br.close();
		} catch (IOException e) {
			// Failed to read
		}
		
		return ftext.toString();
	}
	
	public static boolean ewrite(Context ctx, String dirpath, String path, String text) {
		File dir = new File(
			Environment.getExternalStorageDirectory() +
			"/" + dirpath
		);
		dir.mkdirs();
		File file = new File(dir, path);
		
		FileOutputStream outstream;
		
		try {
			outstream = new FileOutputStream(file);
			outstream.write(text.getBytes());
			outstream.close();
		} catch (Exception e) {
			// Failed to save
			return false;
		}
		
		return true;
	}*/
}