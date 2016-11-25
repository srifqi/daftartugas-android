package srifqi.simetri.daftartugas;

import android.content.Context;

/**
 * A class to change settings.
 */
public class Setting {

	public static final String PROJECT_ID = "projectid";
	public static final String HAS_LOGGED_IN = "loggedin";

	public static final String ALARM_ENABLED = "alarm";
	public static final String ALARM_TIME_HOUR = "alarmtimehour";
	public static final String ALARM_TIME_MINUTE = "alarmtimeminute";
	public static final String ALARM_ONLY_IF_HASNT_DONE = "alarmifhasntdone";
	public static final String ALARM_ONLY_TOMORROW = "alarmonlytommorow";

	public static final String AUTO_SYNC = "autosync";
	public static final String AUTO_SYNC_TIME_HOUR = "autosynctimehour";
	public static final String AUTO_SYNC_TIME_MINUTE = "autosynctimeminute";

	public static final String ALARM_NOTIF_LAST_ID = "alarmnotiflastid";
	public static final String ALARM_AUTO_SYNC_LAST_ID = "alarmautosynclastid";

	public static final int NOTIF_ALARM = 1;
	public static final int NOTIF_AUTO_SYNC = 2;

	public static final int PI_ID_NOTIF_ALARM = 1;
	public static final int PI_ID_NOTIF_AUTO_SYNC = 2;
	public static final int PI_ID_ALARM = 11;
	public static final int PI_ID_AUTO_SYNC = 12;

	/**
	 * Only for debugging purpose.
	 *
	 * @return The content of DT.conf, empty if hasn't been set.
	 */
	public static String writeAll(Context ctx) {
		return IOFile.read(ctx, "DT.conf");
	}

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
			if (li[0].compareTo(name) == 0) return li[1];
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
			if (li[0].compareTo(name) == 0) {
				li[1] = value;
				lines[i] = li[0] + " = " + li[1];
				set = true;
			}
		}
		String data = join(lines, "\n");
		if (set == false) {
			data += "\n" + name + " = " + value;
		}
		return IOFile.write(ctx, "DT.conf", data);
	}

	/**
	 * Set the value of the selected setting into given integer value.
	 *
	 * @param ctx
	 *			Application context.
	 * @param name
	 *			Name of the selected setting.
	 * @param value
	 *			Integer value to be set into the selected setting.
	 *
	 * @return Returns true on success, false on failure.
	 */
	public static boolean set(Context ctx, String name, int value) {
		return set(ctx, name, "" + value);
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
