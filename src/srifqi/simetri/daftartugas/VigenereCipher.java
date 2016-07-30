package srifqi.simetri.daftartugas;

/**
 * A Vigen�re Cipher to crypt.
 */
public class VigenereCipher {

	public static String crypt(String text, int seeda, int seedb) {
		StringBuilder res = new StringBuilder();

		PRNG seed = new PRNG(seeda, seedb);

		for (int i = 0; i < text.length(); i++) {
			int u = (int) ((65536 + Character.codePointAt(text, i) + seed.next()) % 65536);
			res.appendCodePoint(
				u
			);
		}

		return res.toString();
	}

	private static class PRNG {

		private int a;
		private int b;
		private long value;

		public PRNG(int a, int b) {
			if (b < 2) {
				throw new Error("Couldn't start with modulus 1");
			}

			this.a = a;
			this.b = b;

			this.value = 1;
		}

		public long next() {
			this.value = (this.value * this.a) % this.b;
			return this.value;
		}
	}
}
