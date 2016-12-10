package srifqi.simetri.daftartugas;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

public class AutoSyncService extends IntentService {

	private int DONE = 0;

	public AutoSyncService() {
		super("AutoSyncService");

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		DaftarTugasObj DTO = new DaftarTugasObj(getApplicationContext());

		DownloadDT dlt = new DownloadDT();

		dlt.setContext(getApplicationContext());
		dlt.setSaveFilename("fetchdata.txt");
		dlt.saveOnSuccess();
		dlt.setMethod("POST");

		String strUrlParam = "";
		long lastSaved = (long) (IOFile.mtime(getApplicationContext(), "fetchdata.txt") / 1e3);

		String[] teks = IOFile.read(getApplicationContext(), "fetchdata.txt").split("\n\\|\\|\\|\\|\\|\n");
		if (teks.length == 3) {
			// Parse JSON data
			DTO.L = null;
			try {
				DTO.reader = new JSONObject(teks[2]);
				try {
					DTO.L = DTO.reader.getJSONObject("L");
				} catch (JSONException e) {
					// Bad value.
					// e.printStackTrace();
				}
			} catch (JSONException e) {
				// Bad value.
				// e.printStackTrace();
			}
		}

		try {
			strUrlParam = "token=" + IOFile.read(getApplicationContext(), "token.txt") + "&lastsaved=" + lastSaved
					+ "&dataL=" + URLEncoder.encode(DTO.L.toString(), "UTF-8");
			if (IOFile.read(getApplicationContext(), "fetchdata.txt").length() > 0) {
				strUrlParam += "&save=1";
			} else {
				strUrlParam += "&save=0";
			}
			strUrlParam += "&version=" + DaftarTugas.VERSION_CODE;
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}

		dlt.run(DaftarTugas.FETCHURL + Setting.get(getApplicationContext(), Setting.PROJECT_ID) + "/api/transaction",
				strUrlParam);
	}

	private class DownloadDT extends DownloadTask {
		@Override
		public boolean onAfterExecute(String result) {
			DONE++;
			if (DONE >= 2) {
				NotificationManager mNotificationManager = (NotificationManager) this.getContext()
						.getSystemService(Context.NOTIFICATION_SERVICE);

				mNotificationManager.cancel(Setting.NOTIF_AUTO_SYNC);
			}
			return true;
		}
	}
}
