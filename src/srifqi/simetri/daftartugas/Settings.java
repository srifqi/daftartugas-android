package srifqi.simetri.daftartugas;

import android.content.Context;

/**
 * A class to change settings.
 */
public class Settings {
	
	/**
	 * Get the value of the selected setting.
	 * 
	 * @param ctx
	 *			Application context.
	 * @param name
	 *			Name of the selected setting.
	 * 
	 * @return The value of selected setting, empty if hasn't been set.
	 */
	public static String get(Context ctx, String name) {
		String[] lines = IOFile.read(ctx, "DT.conf").split("\n");
		for (int i = 0; i < lines.length; i ++) {
			String[] li = lines[i].split(" = ");
			if (li[0] == name) return li[1];
		}
		return "";
	}
	
	/**
	 * Get the value of the selected setting as integer.
	 * 
	 * @param ctx
	 *			Application context.
	 * @param name
	 *			Name of the selected setting.
	 * 
	 * @return The value of selected setting (parseInt'ed), -1 if hasn't been set.
	 */
	public static int getAsInt(Context ctx, String name) {
		String value = get(ctx, name);
		if (value != "") return Integer.parseInt(value);
		return -1;
	}
	
	/**
	 * Set the value of the selected setting into given value.
	 * 
	 * @param ctx
	 *			Application context.
	 * @param name
	 *			Name of the selected setting.
	 * @param value
	 *			Value to be set into the selected setting.
	 * 
	 * @return Returns true on success, false on failure.
	 */
	public static boolean set(Context ctx, String name, String value) {
		String[] lines = IOFile.read(ctx, "DT.conf").split("\n");
		boolean set = false;
		for (int i = 0; i < lines.length; i ++) {
			String[] li = lines[i].split(" = ");
			if (li[0] == name) {
				li[1] = value;
				set = true;
			}
			lines[i] = li[0] + " = " + li[1];
		}
		String data = join(lines, "\n");
		if (set == false) {
			data += "\n" + name + " = " + value;
		}
		return IOFile.write(ctx, "DT.conf", data);
	}
	
	private static String join(String[] arr, String glue) {
		String res = "";
		for (int i = 0; i < arr.length; i ++) {
			res += arr[i];
			if (i < arr.length - 1) {
				res += glue;
			}
		}
		return res;
	}
}