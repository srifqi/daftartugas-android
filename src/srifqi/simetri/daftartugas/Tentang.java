package srifqi.simetri.daftartugas;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
}
