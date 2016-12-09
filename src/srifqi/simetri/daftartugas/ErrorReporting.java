package srifqi.simetri.daftartugas;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ErrorReporting extends Activity {

	private String errMsg = "";

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.error_reporting);

		String errMessage = getIntent().getStringExtra("Message");
		String errStackStr = "";
		ArrayList<String> errStack = getIntent().getStringArrayListExtra("StackTrace");
		if (errStack == null) {
			errStack = new ArrayList<String>();
		}
		for (Iterator<String> iter = errStack.iterator(); iter.hasNext();) {
			errStackStr += (String) iter.next();
		}
		String errStack2 = getIntent().getStringExtra("StackTrace2");
		errMsg = (
			errMessage + "\n---------------\n" +
			errStackStr + "\n---------------\n" + errStack2
		);

		EditText errmsg = (EditText) findViewById(R.id.ErrorLog);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			errmsg.setTextIsSelectable(true);
		errmsg.setKeyListener(null);
		errmsg.setText(errMsg);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void copyToClipboard(View v) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < 11) {
			android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(errMsg);
		} else {
			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			android.content.ClipData clip = android.content.ClipData.newPlainText("text label", errMsg);
			clipboard.setPrimaryClip(clip);
		}

		Toast.makeText(getApplicationContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
	}

	// http://stackoverflow.com/a/19945692
	// http://stackoverflow.com/a/26560727
	// private UncaughtExceptionHandler defaultUEH;
	// defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	/**
	 * Custom handler that will open a new Activity that contains error message.
	 */
	public static class CustomUEH implements Thread.UncaughtExceptionHandler {

		private Context context;

		public CustomUEH(Context context) {
			this.context = context;
		}

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			Intent intent = new Intent(this.context, srifqi.simetri.daftartugas.ErrorReporting.class);
			intent.putExtra("Message", ex.getMessage());

			final ArrayList<String> errstack = new ArrayList<String>();
			// Trying to get error stack.
			Writer writer = new Writer() {

				@Override
				public void write(char[] buf, int offset, int count) throws IOException {
					errstack.add(new String(buf));
				}

				@Override
				public void flush() throws IOException {}

				@Override
				public void close() throws IOException {}
			};
			PrintWriter pwriter = new PrintWriter(writer);
			ex.printStackTrace(pwriter);
			intent.putExtra("StackTrace", errstack);

			StackTraceElement[] stackTrace = ex.getStackTrace();
			StringBuilder stackTraceString = new StringBuilder();
			for (StackTraceElement el : stackTrace) {
				stackTraceString.append(el.toString()).append("\n");
			}
			intent.putExtra("StackTrace2", stackTraceString.toString());

			this.context.startActivity(intent);

			/* Maybe not, it disturbs the UI.
			if (defaultUEH != null) {
				// Delegates to Andoid's error handling.
				defaultUEH.uncaughtException(thread, ex);
			} */

			System.exit(2); // Prevents app from freezing.
		}
	}
}
