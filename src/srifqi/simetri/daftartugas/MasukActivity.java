package srifqi.simetri.daftartugas;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MasukActivity extends AppCompatActivity {

	private Button btn_masuk;

	private EditText editTextNamaPengguna;
	private EditText editTextKataSandi;
	private EditText editTextProjectID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Thread.setDefaultUncaughtExceptionHandler(new ErrorReporting.CustomUEH(this));

		// Check version
		String[] VERSION = IOFile.read(getApplicationContext(), "version.txt")
				.trim().split("\n");

		if (VERSION.length > 2 && Integer.parseInt(VERSION[2]) > DaftarTugas.VERSION_CODE) {
			this.finish();
			this.finishActivity(RESULT_OK);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_masuk);

		Toolbar toolbar1 = (Toolbar) findViewById(R.id.toolbar1);
		setSupportActionBar(toolbar1);

		toolbar1.setBackgroundResource(R.color.grey);
		toolbar1.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

		btn_masuk = (Button) findViewById(R.id.btn_masuk);
		editTextNamaPengguna = (EditText) findViewById(R.id.editTextNamaPengguna);
		editTextKataSandi = (EditText) findViewById(R.id.editTextKataSandi);
		editTextProjectID = (EditText) findViewById(R.id.editTextProjectID);

		editTextProjectID.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					handled = true;
					doMasuk(v);
				}
				return handled;
			}
		});
	}

	public void doMasuk(View view) {
		btn_masuk.setEnabled(false);
		editTextNamaPengguna.setEnabled(false);
		editTextKataSandi.setEnabled(false);
		editTextProjectID.setEnabled(false);
		String NamaPengguna = editTextNamaPengguna.getText().toString();
		String KataSandi = editTextKataSandi.getText().toString();
		String ProjectID = editTextProjectID.getText().toString();
		Setting.set(getApplicationContext(), Setting.PROJECT_ID, ProjectID);

		if (NamaPengguna.length() < 1 || KataSandi.length() < 1) {
			Toast.makeText(getApplicationContext(), R.string.please_fill_all, Toast.LENGTH_SHORT).show();
			btn_masuk.setEnabled(true);
			editTextNamaPengguna.setEnabled(true);
			editTextKataSandi.setEnabled(true);
			return;
		}

		String strUrl = DaftarTugas.FETCHURL +
			Setting.get(getApplicationContext(), Setting.PROJECT_ID) +
			"/api/validation";
		String strUrlParam = "";
		try {
			strUrlParam = "user|" + URLEncoder.encode(NamaPengguna, "UTF-8") +
						 "|pass|" + URLEncoder.encode(KataSandi, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}

		Random rnd = new Random();
		rnd.setSeed(System.currentTimeMillis());
		int seedb = rnd.nextInt(1024);
		int seeda = rnd.nextInt(1024); seeda += seedb;
		try {
			strUrlParam = "x=" + URLEncoder.encode(VigenereCipher.crypt(strUrlParam, seeda, seedb), "UTF-8") +
					"&y=" + seeda + "&z=" + seedb;
		} catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
		}

		LoginT lT = new LoginT();
		lT.setContext(getApplicationContext());
		lT.dontSave();
		lT.setMethod("POST");

		lT.run(strUrl, strUrlParam);
	}

	public void OK() {
		// Inform user.
		Toast.makeText(getApplicationContext(), R.string.login_success, Toast.LENGTH_SHORT).show();

		Intent intent = new Intent(this, DaftarTugas.class);
		startActivity(intent);

		this.finish();
		this.finishActivity(RESULT_OK);
	}

	private class LoginT extends DownloadTask {

		@Override
		public boolean onAfterExecute(String result) {
			if (result == "") return false;
			if (result.length() == 0) {
				Toast.makeText(this.getContext(), R.string.failed_connect, Toast.LENGTH_SHORT).show();
				this.onNoConnection();
				return false;
			}
			// Login failed.
			// Either both or one of user and password may wrong.
			if (result.trim().length() == 3) {
				Toast.makeText(getApplicationContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();

				btn_masuk.setEnabled(true);
				editTextNamaPengguna.setEnabled(true);
				editTextKataSandi.setEnabled(true);
			} else {
				// Save data in an internal file "userpass.txt".
				IOFile.write(getApplicationContext(), "userpass.txt",
					editTextNamaPengguna.getText().toString()
					+ "\n" +

					// How can I saved password in plain text?
					"..." // editTextKataSandi.getText().toString()
				);
				// Save token in an internal file "token.txt".
				IOFile.write(getApplicationContext(), "token.txt", result);

				// Save the state: has already logged in.
				Setting.set(getApplicationContext(), Setting.HAS_LOGGED_IN, 1);

				OK();
			}

			return true;
		}

		@Override
		public boolean onNoConnection() {
			Toast.makeText(getApplicationContext(), R.string.no_connection, Toast.LENGTH_SHORT).show();

			btn_masuk.setEnabled(true);
			editTextNamaPengguna.setEnabled(true);
			editTextKataSandi.setEnabled(true);

			return true;
		}
	}
}
