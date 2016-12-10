package srifqi.simetri.daftartugas;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * MD5 Checksum class.
 * <p/>
 * <p>
 * This method is heavily based on http://stackoverflow.com/a/16938703/5338238
 * </p>
 */
public class MD5Checksum {
	/**
	 * Create an MD5 chekcsum of a file.
	 * 
	 * @param file
	 *            Name of the file.
	 * 
	 * @return MD5 checksum of the file, null on failure.
	 */
	public static String fileToMD5(File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = inputStream.read(buffer);
				if (numRead > 0)
					digest.update(buffer, 0, numRead);
			}
			byte[] md5Bytes = digest.digest();

			String returnVal = "";
			for (int i = 0; i < md5Bytes.length; i++)
				returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);

			return returnVal;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
		}
	}
}