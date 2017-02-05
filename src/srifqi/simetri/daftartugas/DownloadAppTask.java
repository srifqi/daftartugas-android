package srifqi.simetri.daftartugas;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;

// Uses AsyncTask to create a task away from the main UI thread. This task takes a
// URL string and uses it to create an HttpUrlConnection. Once the connection
// has been established, the AsyncTask downloads the contents of the webpage as
// an InputStream. Finally, the InputStream is converted into a string, which is
// displayed in the UI by the AsyncTask's onPostExecute method.
/**
 * A class to create an {@link AsyncTask} to download a file.
 */
public class DownloadAppTask extends AsyncTask<String, Integer, String> {
	private Context ctx;
	private int responseCode = 0;
	private int readTimeout = 10000;
	private int connectTimeout = 3600000;
	public int downloaded = 0;
	public String path;
	public File file;

	/**
	 * Function that will be called after done executing.
	 *
	 * @param result
	 *			The status of download.
	 */
	public boolean onAfterExecute(String result) {
		return true;
	}

	/**
	 * Function that will be called when there is progress.
	 *
	 * @param downloaded
	 *			Size of downloaded file (in bytes).
	 */
	public boolean onDownloadProgressUpdate(int downloaded) {
		return true;
	}

	/**
	 * Function that will be called when no connection is avaiable.
	 */
	public boolean onNoConnection() {
		return true;
	}

	/**
	 * Set the context of the task.
	 *
	 * @param context
	 *			Context of the task.
	 */
	public void setContext(Context context){
		this.ctx = context;
	}

	/**
	 * Returns the context of the task.
	 */
	public Context getContext(){
		return this.ctx;
	}

	/**
	 * Set the timeout for request to be read by server.
	 *
	 * @param timeout
	 *			Timeout for request to be read.
	 */
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}

	/**
	 * Set the timeout for request to be connected to server.
	 *
	 * @param timeout
	 *			Timeout for request to be connected.
	 */
	public void setConnectTimeout(int timeout) {
		this.connectTimeout = timeout;
	}

	/**
	 * Returns the response code of the request.
	 */
	public int getResponseCode() {
		return this.responseCode;
	}

	/**
	 * Run the task.
	 *
	 * @param url
	 *			URL of the file.
	 */
	public void run(String url) {
		this.downloaded = 0;

		ConnectivityManager connMgr = (ConnectivityManager) this.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			this.execute(url);
		} else {
			this.onNoConnection();
		}
	}

	@Override
	protected String doInBackground(String... urls) {
		// params comes from the execute() call: params[0] is the url.
		try {
			String res = this.downloadUrl(urls[0]);
			if (
				Pattern.compile("hostinger", Pattern.CASE_INSENSITIVE).matcher(res).find() ||
				Pattern.compile("cpu", Pattern.CASE_INSENSITIVE).matcher(res).find()
			) {
				// Server is busy.
				// Use R.string.server_sibuk to tell.
				return "BUSY";
			} else if (
				Pattern.compile("bitninja", Pattern.CASE_INSENSITIVE).matcher(res).find()
			) {
				return "BLOCKED";
			} else {
				return "OK";
			}
		} catch (IOException e) {
			return "CACHE";
		}
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		if (result != "OK") {
			this.onNoConnection();
		} else {
			this.onAfterExecute(result);
		}
	}

	protected void onProgressUpdate(Integer... progress) {
		this.onDownloadProgressUpdate(progress[0]);
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private String downloadUrl(String myurl) throws IOException {
		InputStream is = null;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(this.readTimeout); // milliseconds
			conn.setConnectTimeout(this.connectTimeout); // milliseconds

			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();

			is = conn.getInputStream();
			this.responseCode = conn.getResponseCode();

			File dir = new File(
				Environment.getExternalStorageDirectory() +
				"/DaftarTugas"
			);
			dir.mkdirs();
			File file = new File(dir, "DaftarTugas.apk");

			if (file.exists()) file.delete();

			FileOutputStream f = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				f.write(buffer, 0, len1);
				this.publishProgress(
					this.downloaded = this.downloaded + len1
				);
			}
			f.close();

			ConnectivityManager connMgr = (ConnectivityManager) this.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

			if (networkInfo != null && networkInfo.isConnected()) {
				// Done.
			} else {
				// Something bad happened.
				if (file.exists()) file.delete();
				this.onNoConnection();
				this.path = "";
				return "ERROR";
			}

			this.path = file.getAbsolutePath();
			this.file = file;

			return "DONE";

			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
}
