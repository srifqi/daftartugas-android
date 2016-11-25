package srifqi.simetri.daftartugas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	// Set alarm after boot.
	@Override
	public void onReceive(Context ctx, Intent intent) {
		AlarmTugasReceiver.setAlarm(ctx.getApplicationContext());
		AlarmSyncReceiver.setAutoSync(ctx.getApplicationContext());
	}

}
