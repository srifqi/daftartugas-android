package srifqi.simetri.daftartugas;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class AlarmTugasReceiver extends BroadcastReceiver {

	private Resources rsc;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(ctx));

		// Get resources.
		rsc = ctx.getResources();

		/*
		 * Trying to make sure no double notifying.
		 *
		 * http://stackoverflow.com/a/21314662
		 */
		if (intent.getStringExtra("ID") == Setting.get(ctx, Setting.ALARM_NOTIF_LAST_ID)) {
			return;
		}
		Setting.set(ctx, Setting.ALARM_NOTIF_LAST_ID, intent.getStringExtra("ID"));

		setAlarm(ctx.getApplicationContext());

		DaftarTugasObj DTO = new DaftarTugasObj(ctx);
		DTO.read();

		int hasntdone = 0;
		int hasntdonetomorrow = 0;
		for (int i = 0; i < DTO.SortedDaftarTugas.size(); i++) {
			String[] tugas = DTO.SortedDaftarTugas.get(i);
			if (tugas[6].compareTo("0") == 0) {
				hasntdone++;
				String[] date = tugas[5].split(",");
				int[] date2 = { Integer.parseInt(date[0]), Integer.parseInt(date[1]) - 1, Integer.parseInt(date[2]) };
				Calendar calendar = new GregorianCalendar();
				calendar.add(Calendar.HOUR_OF_DAY, 24);
				if (date2[0] == calendar.get(Calendar.YEAR) && date2[1] == calendar.get(Calendar.MONTH)
						&& date2[2] == calendar.get(Calendar.DAY_OF_MONTH)) {
					hasntdonetomorrow++;
				}
			}
		}

		if (Setting.getAsInt(ctx, Setting.ALARM_ONLY_IF_HASNT_DONE) == 1
				&& Setting.getAsInt(ctx, Setting.ALARM_ONLY_TOMORROW) == 1 && hasntdonetomorrow == 0) {
			return;
		}

		if (Setting.getAsInt(ctx, Setting.ALARM_ONLY_IF_HASNT_DONE) == 1 && hasntdone == 0) {
			return;
		}

		String msg = "";
		if (Setting.getAsInt(ctx, Setting.ALARM_ONLY_TOMORROW) == 1) {
			msg = rsc.getString(R.string.task_hasnt_done_tomorrow_count) + " " + hasntdonetomorrow;
		} else if (hasntdone == 0) {
			msg = rsc.getString(R.string.all_task_done);
		} else {
			msg = rsc.getString(R.string.task_hasnt_done_count) + " " + hasntdone;
		}

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx).setSmallIcon(R.drawable.ic_alarm)
				.setContentTitle(rsc.getString(R.string.app_name)).setContentText(msg);

		Intent resultIntent = new Intent(ctx, DaftarTugas.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(ctx, Setting.PI_ID_NOTIF_ALARM, resultIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		mBuilder.setContentIntent(resultPendingIntent);

		mBuilder.setAutoCancel(true);
		mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
		mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);

		NotificationManager mNotificationManager = (NotificationManager) ctx
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(Setting.NOTIF_ALARM, mBuilder.build());

		Intent intent2 = new Intent(ctx, AlarmTugasActivity.class);
		intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent2);
	}

	/**
	 * Helper to set alarm.
	 *
	 * @param ctx
	 *            The application context.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void setAlarm(Context ctx) {
		AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Setting.getAsInt(ctx, Setting.ALARM_TIME_HOUR));
		calendar.set(Calendar.MINUTE, Setting.getAsInt(ctx, Setting.ALARM_TIME_MINUTE));
		calendar.set(Calendar.SECOND, 0);

		if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			calendar.add(Calendar.HOUR_OF_DAY, 24);
		}

		Intent intent = new Intent(ctx, AlarmTugasReceiver.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("ID", "." + calendar.get(Calendar.DAY_OF_YEAR) + "." + calendar.get(Calendar.HOUR_OF_DAY) + "."
				+ calendar.get(Calendar.MINUTE));
		PendingIntent alarmIntent = PendingIntent.getBroadcast(ctx, Setting.PI_ID_ALARM, intent, 0);

		if (Setting.getAsInt(ctx, Setting.ALARM_ENABLED) == 0) {
			alarmMgr.cancel(alarmIntent);
			return;
		}

		if (Build.VERSION.SDK_INT >= 19) {
			alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
		} else {
			alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
		}
	}
}
