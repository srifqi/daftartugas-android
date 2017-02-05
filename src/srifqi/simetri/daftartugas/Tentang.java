package srifqi.simetri.daftartugas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

public class Tentang extends Activity {

	public AlertDialog.Builder TOSdlgb;
	public AlertDialog TOSdlg;

	private Resources rsc;
	// Cache
	public String TOS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		// Get resources.
		rsc = getResources();

		TOS = rsc.getString(R.string.tos_cache);

		setContentView(R.layout.activity_tentang);

		TOSdlgb = new AlertDialog.Builder(this);
		TOSdlgb.setTitle(R.string.tos);
		TOSdlgb.setMessage(TOS);
		TOSdlgb.setCancelable(true);
		TOSdlgb.setNeutralButton("Tutup", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing.
			}
		});

		DownloadTOSTask dtost = new DownloadTOSTask();
		dtost.setContext(getApplicationContext());
		dtost.setMethod("GET");
		dtost.setSaveFilename("TOS.txt");
		dtost.run(
			DaftarTugas.FETCHURL +
			Setting.get(getApplicationContext(), Setting.PROJECT_ID) +
			"/tos.txt"
		);
	}

	public void openTOS(View view) {
		TOSdlg = TOSdlgb.create();
		TOSdlg.show();
	}

	public class DownloadTOSTask extends DownloadTask {

		@Override
		public boolean onAfterExecute(String result) {
			if (this.getConnectionStatus() == "blocked") {
				DaftarTugas.AlertBlocked(Tentang.this);
				return false;
			}

			if (this.getConnectionStatus() != "okay") {
				return false;
			}

			if (result != "") {
				TOS = result;
				TOSdlgb.setMessage(TOS);
			}
			return true;

		}
	}
}
