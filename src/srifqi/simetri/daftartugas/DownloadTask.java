package srifqi.simetri.daftartugas;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

// Uses AsyncTask to create a task away from the main UI thread. This task takes a
// URL string and uses it to create an HttpUrlConnection. Once the connection
// has been established, the AsyncTask downloads the contents of the webpage as
// an InputStream. Finally, the InputStream is converted into a string, which is
// displayed in the UI by the AsyncTask's onPostExecute method.
/**
 * A class to create an {@link AsyncTask} to download a page as {@link String}.
 *
 * <b>Cache handling</b>
 * There are three possible options to save the downloaded page to storage:
 * - {@link DownloadTask.dontSave()}
 *   This option will never save the downloaded page.
 * - {@link DownloadTask.alwaysSave()}
 *   This option will always save the downloaded page.
 * - {@link DownloadTask.saveOnSuccess()}
 *   This option will only save the downloaded page when the server responds 200.
 */
public class DownloadTask extends AsyncTask<String, Void, String> {
	private Context ctx;
	private String saveFilename = "temp.txt";
	private String method = "GET";
	private int responseCode = 0;
	private String saveData = "onSuccess";
	private int readTimeout = 10000;
	private int connectTimeout = 15000;

	/**
	 * Function that will be called after done executing.
	 *
	 * @param result
	 *			The downloaded page as String.
	 */
	public boolean onAfterExecute(String result){
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
	 * Set the name of file to save the result.
	 *
	 * @param filename
	 *			Name of the file.
	 */
	public void setSaveFilename(String filename){
		this.saveFilename = filename;
	}

	/**
	 * Set the method to send request to the server (GET or POST).
	 *
	 * @param method
	 *			Method to send request.
	 */
	public void setMethod(String method) {
		this.method = method;
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
	 * Set to not to save to a file.
	 */
	public void dontSave() {
		this.saveData = "false";
	}

	/**
	 * Set to always save to a file.
	 */
	public void alwaysSave() {
		this.saveData = "true";
	}

	/**
	 * Set to save to a file only when the server returned HTTP 200 OK status.
	 */
	public void saveOnSuccess() {
		this.saveData = "onSuccess";
	}

	/**
	 * Run the task.
	 *
	 * @param url
	 *			URL of the page.
	 * @param param
	 *			Parameter for POST request.
	 */
	public void run(String url, String param){
		this.onAfterExecute(readCache());

		ConnectivityManager connMgr = (ConnectivityManager) this.ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			this.execute(url, param);
		} else {
			this.onNoConnection();

			if (this.saveData.compareToIgnoreCase("false") != 0) {
				this.onAfterExecute(readCache());
			}
		}
	}

	/**
	 * Run the task.
	 *
	 * @param url
	 *			URL of the page.
	 */
	public void run(String url){
		run(url, "");
	}

	/**
	 * Returns the cache of the page (saved page as file) if avaiable.
	 */
	public String readCache(){
		// Read data in an internal file.
		return IOFile.read(this.ctx, this.saveFilename);
	}

	@Override
	protected String doInBackground(String... urls) {
		// params comes from the execute() call: params[0] is the url.
		try {
			String res = this.downloadUrl(urls[0], urls[1]);
			if (
				res.contains("cpu") || res.contains("hostinger") ||
				res.contains("CPU") || res.contains("Hostinger")
			) {
				// Server is busy.
				// Use R.string.server_busy to tell.
				return this.readCache();
			} else {
				return res;
			}
		} catch (IOException e) {
			return this.readCache();
		}
	}

	// onPostExecute displays the results of the AsyncTask.
	@Override
	protected void onPostExecute(String result) {
		this.onAfterExecute(result);

		// Save the downloaded page after calling the onAfterExecute, so when there
		// is an error, the page would not be saved.
		if (
			this.saveData.compareToIgnoreCase("true") == 0 ||
			(this.saveData.compareToIgnoreCase("onSuccess") == 0 && this.responseCode == 200)
		) {
			IOFile.write(this.ctx, this.saveFilename, result);
		}
	}

	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as
	// a string.
	private String downloadUrl(String myurl, String param) throws IOException {
		InputStream is = null;
		InputStream es = null;

		try {
			URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(this.readTimeout); // milliseconds
			conn.setConnectTimeout(this.connectTimeout); // milliseconds

			if (this.method=="GET") {
				conn.setRequestMethod("GET");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
			} else if (this.method=="POST") {
				conn.setRequestMethod("POST");

				conn.setDoOutput(true);
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(param);

				// Starts the query
				wr.flush();
				wr.close();
			}

			is = conn.getInputStream();
			es = conn.getErrorStream();

			// Hotspot log-in to pass.
			if (!url.getHost().equals(conn.getURL().getHost())) {
				return this.readCache();
			}

			this.responseCode = conn.getResponseCode();

			// Convert the InputStream into a string
			String contentAsString;
			if (this.responseCode >= 200 & this.responseCode < 300) {
				contentAsString = readIt(is);
			} else {
				contentAsString = readIt(es);
			}

			return contentAsString;

		// Makes sure that the InputStream is closed after the app is
		// finished using it.
		} finally {
			if (is != null) {
				is.close();
			}
			if (es != null) {
				es.close();
			}
		}
	}

	public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (stream == null) {
			return this.readCache();
		}
		BufferedReader brd = new BufferedReader(
			new InputStreamReader(stream, "UTF-8")
		);
		String line;
		int i = 0;

		while ((line = brd.readLine()) != null) {
			if (i > 0) sb.append("\n");
			sb.append(line);
			i ++;
		}

		return sb.toString();
	}
}
