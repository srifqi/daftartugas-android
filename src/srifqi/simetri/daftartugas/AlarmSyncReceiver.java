package srifqi.simetri.daftartugas;

import java.util.Calendar;

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

public class AlarmSyncReceiver extends BroadcastReceiver {

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
		if (
			intent.getStringExtra("ID") ==
			Setting.get(ctx, Setting.ALARM_AUTO_SYNC_LAST_ID)
		) {
			return;
		}
		Setting.set(ctx, Setting.ALARM_AUTO_SYNC_LAST_ID, intent.getStringExtra("ID"));

		setAutoSync(ctx.getApplicationContext());

		Intent serviceIntent = new Intent(ctx, AutoSyncService.class);
		ctx.startService(serviceIntent);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx)
			.setSmallIcon(R.drawable.ic_sync)
			.setContentTitle(rsc.getString(R.string.app_name))
			.setContentText(rsc.getString(R.string.auto_syncing));

		Intent resultIntent = new Intent(ctx, PengaturanSinkronasiActivity.class);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(
			ctx, Setting.PI_ID_NOTIF_AUTO_SYNC,
			resultIntent, PendingIntent.FLAG_CANCEL_CURRENT
		);

		mBuilder.setContentIntent(resultPendingIntent);

		mBuilder.setAutoCancel(false);
		mBuilder.setOngoing(true);
		mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
		mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);

		NotificationManager mNotificationManager =
			(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.notify(Setting.NOTIF_AUTO_SYNC, mBuilder.build());

		// Toast.makeText(ctx.getApplicationContext(), "[DaftarTugas] Sinkronasi otomatis", Toast.LENGTH_LONG).show();
	}

	/**
	 * Helper to set alarm.
	 *
	 *  @param ctx
	 *  		The application context.
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static void setAutoSync(Context ctx) {
		AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Setting.getAsInt(
			ctx, Setting.AUTO_SYNC_TIME_HOUR
		));
		calendar.set(Calendar.MINUTE, Setting.getAsInt(
			ctx, Setting.AUTO_SYNC_TIME_MINUTE
		));
		calendar.set(Calendar.SECOND, 0);

		if (calendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
			calendar.add(Calendar.HOUR_OF_DAY, 24);
		}

		Intent intent = new Intent(ctx, AlarmSyncReceiver.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("ID",
			"." + calendar.get(Calendar.DAY_OF_YEAR) +
			"." + calendar.get(Calendar.HOUR_OF_DAY) +
			"." + calendar.get(Calendar.MINUTE)
		);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(
			ctx, Setting.PI_ID_AUTO_SYNC, intent, 0
		);

		if (Setting.getAsInt(ctx, Setting.AUTO_SYNC) == 0) {
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
