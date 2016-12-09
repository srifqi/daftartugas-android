package srifqi.simetri.daftartugas;

import java.io.File;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PerbaruiActivity extends AppCompatActivity {

	private Resources rsc;
	private TextView textStatus;
	private TextView textProgress;
	private Button buttonPerbarui;
	private Button buttonPasang;
	private Button buttonUnduhUlang;
	private ProgressDialog pd;
	private DownloadAppAPKTask daat;
	private String[] VERSION;
	public boolean activityPaused;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		// Check version
		VERSION = IOFile.read(getApplicationContext(), "version.txt")
				.trim().split("\n");

		if (VERSION.length > 2 && Integer.parseInt(VERSION[2]) < DaftarTugas.VERSION_CODE) {
			Intent intent = new Intent(this, DaftarTugas.class);
			startActivity(intent);

			this.finish();
			this.finishActivity(RESULT_OK);
		}

		// Get resources.
		rsc = getResources();

		setContentView(R.layout.activity_perbarui);

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);

		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

		textStatus = (TextView) findViewById(R.id.textView1);
		textProgress = (TextView) findViewById(R.id.textProgress);
		buttonPerbarui = (Button) findViewById(R.id.btn_perbarui);
		buttonPasang = (Button) findViewById(R.id.btn_pasang);
		buttonUnduhUlang = (Button) findViewById(R.id.btn_unduh_ulang);
		pd = new ProgressDialog(PerbaruiActivity.this);
		pd.setCancelable(false);
		pd.setCanceledOnTouchOutside(false);
	}

	@Override
	protected void onResume() {
		super.onResume();

		activityPaused = false;

		TextView textVersion = (TextView) findViewById(R.id.textVersion);
		textVersion.setText(rsc.getString(R.string.newest_version) + ": " + VERSION[0]);

		File dir = new File(
			Environment.getExternalStorageDirectory() +
			"/DaftarTugas"
		);
		dir.mkdirs();
		File file = new File(dir, "DaftarTugas.apk");

		String STATUS = IOFile.read(getApplicationContext(), "updatestatus.txt");
		if (STATUS.compareToIgnoreCase("INSTALL") == 0 &&
			file.exists()) {
			if(MD5Checksum.fileToMD5(file)
				.compareToIgnoreCase(VERSION[1]) == 0) {
				textStatus.setText(R.string.install_info);
				textProgress.setText(R.string.download_completed);
				buttonPerbarui.setVisibility(View.GONE);
				buttonPasang.setVisibility(View.VISIBLE);
				buttonUnduhUlang.setVisibility(View.VISIBLE);
			} else {
				file.delete();
				IOFile.write(getApplicationContext(), "updatestatus.txt", "");
			}
		} else if (STATUS.compareToIgnoreCase("DOWNLOADSTART") == 0) {
			textProgress.setText(R.string.downloading);
			buttonPerbarui.setVisibility(View.VISIBLE);
			buttonPerbarui.setEnabled(false);
			buttonPasang.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		activityPaused = true;
	}

	public void runDownloader(View view) {
		buttonPerbarui.setEnabled(false);
		textProgress.setText(R.string.downloading);
		IOFile.write(getApplicationContext(), "updatestatus.txt", "DOWNLOADSTART");

		pd.setTitle(R.string.update2);
		pd.setMessage(rsc.getString(R.string.starting_update));
		pd.setIndeterminate(true);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setCancelable(false);
		if (pd != null) pd.show();

		daat = new DownloadAppAPKTask();
		daat.setContext(getApplicationContext());
		daat.progressTextView = textProgress;
		daat.run(
			DaftarTugas.FETCHURL +
			Setting.get(getApplicationContext(), Setting.PROJECT_ID) +
			"/d/DaftarTugas-" + VERSION[0] + ".apk"
		);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void rerunDownloader(String title, String message) {
		// Corrupted packet?

		// Delete it first.
		File dir = new File(
			Environment.getExternalStorageDirectory() +
			"/DaftarTugas"
		);
		dir.mkdirs();
		File file = new File(dir, "DaftarTugas.apk");
		file.delete();

		// Re-run download?
		IOFile.write(getApplicationContext(), "updatestatus.txt", "");

		AlertDialog.Builder dlgb = new AlertDialog.Builder(this);
		dlgb.setTitle(title);
		dlgb.setMessage(message);

		dlgb.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				runDownloader(buttonPerbarui);
			}
		});

		dlgb.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
				finishActivity(RESULT_OK);
			}
		});

		dlgb.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				finish();
				finishActivity(RESULT_OK);
			}

		});

		AlertDialog dlg = dlgb.create();
		dlg.show();
	}

	public void rerunDownloaderBtn(View view) {
		// Re-run download?

		AlertDialog.Builder dlgb2 = new AlertDialog.Builder(this);
		dlgb2.setMessage(R.string.ask_download_retry);

		dlgb2.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				IOFile.write(getApplicationContext(), "updatestatus.txt", "");
				runDownloader(buttonPerbarui);
			}
		});

		dlgb2.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing.
			}
		});

		AlertDialog dlg2 = dlgb2.create();
		dlg2.show();
	}

	public void runChecker(File file) {
		if (null == file) {
			rerunDownloader(
				rsc.getString(R.string.install_failed),
				rsc.getString(R.string.install_failed_download_failure)
			);
		}
		String md5 = "";
		try {
			md5 = MD5Checksum.fileToMD5(file);
		} catch (Exception e) {
			rerunDownloader(
				rsc.getString(R.string.install_failed),
				rsc.getString(R.string.install_failed_download_failure)
			);
		}
		if (VERSION[1].trim().compareToIgnoreCase(md5.trim()) == 0) {
			if (activityPaused == false) runInstaller();
		} else {
			rerunDownloader(
				rsc.getString(R.string.install_failed),
				rsc.getString(R.string.install_failed_packet_mismatch)
			);
		}
	}

	public void runInstaller(String path) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			this.finish();
			this.finishActivity(RESULT_OK);
		} catch(Exception e) {
			rerunDownloader(
				rsc.getString(R.string.install_failed),
				rsc.getString(R.string.install_failed_download_failure)
			);
		}
	}

	public void runInstaller() {
		File dir = new File(
			Environment.getExternalStorageDirectory() +
			"/DaftarTugas"
		);
		dir.mkdirs();
		File file = new File(dir, "DaftarTugas.apk");
		runInstaller(file.getPath());
	}

	public void runInstaller(View view) {
		runInstaller();
	}

	public class DownloadAppAPKTask extends DownloadAppTask  {

		public TextView progressTextView;

		@Override
		public boolean onNoConnection() {
			Toast.makeText(this.getContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();
			progressTextView.setText(R.string.no_connection);
			if (pd != null) pd.dismiss();
			buttonPerbarui.setEnabled(true);
			return true;
		}

		// http://stackoverflow.com/a/18650828/5338238
		private String formatBytes(int bytes) {
			if(bytes == 0) return "0 Byte";
			int k = 1024;
			String[] sizes = {"Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
			int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
			return (Math.floor(bytes / Math.pow(k, i) * 100) / 100) + " " + sizes[i];
		}

		@Override
		public boolean onDownloadProgressUpdate(int downloaded) {
			pd.setMessage(rsc.getString(R.string.downloading) + "\n(" +
				formatBytes(downloaded) +
			")");
			return true;
		}

		@Override
		public boolean onAfterExecute(String result) {
			progressTextView.setText(R.string.download_done_now_installing);
			IOFile.write(getApplicationContext(), "updatestatus.txt", "INSTALL");
			if (pd != null) pd.dismiss();
			runChecker(this.file);
			return true;
		}
	}
}
