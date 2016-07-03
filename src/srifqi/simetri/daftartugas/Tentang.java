package srifqi.simetri.daftartugas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Tentang extends Activity {

	public AlertDialog.Builder TOSdlgb;
	public AlertDialog TOSdlg;
	// Cache
	public String TOS = "Syarat dan Ketentuan Aplikasi Daftar Tugas\n"+
"\n\n"+
"Sedang mengambil…\n"+
"Coba tutup lalu sentuh Syarat dan Ketentuan lagi.\n"+
"Info lebih lanjut buka Daftar Tugas versi web."+
"\n\n"+
"Hak Cipta Muhammad Rifqi Priyo Susanto dkk. Simetri Creative Code Labs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(_UEH);
		
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
		dtost.run(DaftarTugas.FETCHURL + "/tos.txt");
	}
	
	public void openTOS(View view) {
		TOSdlg = TOSdlgb.create();
		TOSdlg.show();
	}
	
	public class DownloadTOSTask extends DownloadTask {
		
		@Override
		public boolean onAfterExecute(String result) {
			if (result != "") {
				TOS = result;
				TOSdlgb.setMessage(TOS);
			}
			return true;
			
		}
	}
	
	// http://stackoverflow.com/a/19945692
	// http://stackoverflow.com/a/26560727
	// private UncaughtExceptionHandler defaultUEH;
	private Thread.UncaughtExceptionHandler _UEH = new Thread.UncaughtExceptionHandler() {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			Intent intent = new Intent(getApplicationContext(), srifqi.simetri.daftartugas.ErrorReporting.class);
			intent.putExtra("Message", ex.getMessage());
			
			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuilder stackTraceString = new StringBuilder();
			for (StackTraceElement el : stackTrace) {
				stackTraceString.append(el.toString()).append("\n");
			}
			intent.putExtra("StackTrace", stackTraceString.toString());
			
			startActivity(intent);
			
			/* Maybe not, it disturbs the UI.
			if (defaultUEH != null) {
				// Delegates to Andoid's error handling.
				defaultUEH.uncaughtException(thread, ex);
			} */

			System.exit(2); // Prevents app from freezing.
		}
		
	};
}
