package app;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

import java.util.Base64;



/**
 * RSA�����㷨������
 * 
 *
 */
public class RSAUtility {

	
	// ˽Կָ��
	private static final String PRIVATE_KEY_EXPONENT = "98534771646870147834544192034956025369";

	// ģ
	private static final String KEY_MODULUS = "172053215288437611692003855080317127571";

	// ��Կָ��
	private static final String PUBLIC_KEY_EXPONENT = "65537";

	private static final String RSA = "RSA";

	/**
	 * ������Կ�Է��� ��Ҫ������Կʱʹ��
	 * 
	 * @return KeyPair
	 * @throws Exception
	 */
	public  KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(
					RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
			// ��С
			final int KEY_SIZE = 128;
			keyPairGen.initialize(KEY_SIZE, new SecureRandom());
			KeyPair keyPair = keyPairGen.generateKeyPair();

			// ��ȡ��Կ��Կ
			RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
			RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

			// ��ӡ��Կ��Ϣ
//			System.out.println(rsaPublicKey.getModulus() + "========"
//					+ rsaPublicKey.getPublicExponent());
//			System.out.println(rsaPrivateKey.getModulus() + "======="
//					+ rsaPrivateKey.getPrivateExponent());
//			System.out.println(rsaPublicKey.toString());
			return keyPair;
		} catch (Exception e) {
			throw new RuntimeException("������Կ�� ���ִ���", e);
		}
	}

	/**
	 * ��ȡ��Կ��Ϣ ����ʱʹ��
	 * 
	 * @return
	 * @throws Exception
	 */
	public static  RSAPublicKey generateRSAPublicKey() {
		try {
			KeyFactory keyFac = null;
			keyFac = KeyFactory.getInstance(RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(new BigInteger(
					RSAUtility.KEY_MODULUS), new BigInteger(
					RSAUtility.PUBLIC_KEY_EXPONENT));

			//System.out.println(keyFac.generatePublic(pubKeySpec));
			return (RSAPublicKey) keyFac.generatePublic(pubKeySpec);
		} catch (Exception e) {
			throw new RuntimeException("��ȡ��Կ���ִ���", e);
		}
	}

	/**
	 * ��ȡ˽Կ��Ϣ����ʱʹ��
	 * 
	 * @return
	 * @throws Exception
	 */
	public static  RSAPrivateKey generateRSAPrivateKey() {
		try {
			KeyFactory keyFac = null;
			keyFac = KeyFactory.getInstance(RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());
			RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
					new BigInteger(RSAUtility.KEY_MODULUS), new BigInteger(
							RSAUtility.PRIVATE_KEY_EXPONENT));

			//System.out.println(keyFac.generatePrivate(priKeySpec));
			return (RSAPrivateKey) keyFac.generatePrivate(priKeySpec);
		} catch (Exception e) {
			throw new RuntimeException("��ȡ˽Կ���ִ���", e);

		}

	}

	/**
	 * ���ܷ���
	 * 
	 * @param dataStr
	 *            �������ַ���
	 * @return
	 * @throws Exception
	 */
	public static  String encrypt(String dataStr) {
		try {
			byte[] data = dataStr.getBytes();
			Cipher cipher = Cipher.getInstance(RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			cipher.init(Cipher.ENCRYPT_MODE, RSAUtility.generateRSAPublicKey());

			// ��ü��ܿ��С���磺����ǰ����Ϊ128��byte����key_size=1024
			int blockSize = cipher.getBlockSize();

			// ���ܿ��СΪ127byte,���ܺ�Ϊ128��byte;��˹���2�����ܿ飬
			// ��һ��127byte�ڶ���Ϊ1��byte
			int outputSize = cipher.getOutputSize(data.length);// ��ü��ܿ���ܺ���С

			int leavedSize = data.length % blockSize;

			int blocksSize = leavedSize != 0 ? data.length / blockSize + 1
					: data.length / blockSize;

			byte[] raw = new byte[outputSize * blocksSize];

			int i = 0;
			while (data.length - i * blockSize > 0) {
				if (data.length - i * blockSize > blockSize) {

					cipher.doFinal(data, i * blockSize, blockSize, raw, i
							* outputSize);
				} else {
					cipher.doFinal(data, i * blockSize, data.length - i
							* blockSize, raw, i * outputSize);
				}
				i++;
			}
			return Base64.getEncoder().encodeToString(raw);
		} catch (Exception e) {
			throw new RuntimeException("���� ���ִ���", e);
		}
	}

	/**
	 * ���ܷ���
	 * 
	 * @param rawStr
	 *            �������ַ���
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("static-access")
	public static String decrypt(String rawStr) {
		try {
			byte[] raw = Base64.getDecoder().decode(rawStr);
			Cipher cipher = Cipher.getInstance(RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			cipher.init(cipher.DECRYPT_MODE, RSAUtility.generateRSAPrivateKey());

			int blockSize = cipher.getBlockSize();

			ByteArrayOutputStream bout = new ByteArrayOutputStream(64);

			int j = 0;
			while (raw.length - j * blockSize > 0) {
				bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
				j++;
			}
			return new String(bout.toByteArray());
		} catch (Exception e) {
			throw new RuntimeException("���ܳ��ִ���", e);
		}
	}

	/**
	 * ����JS���ܣ���JS�д���������ת����Base64���õ��ַ��� ����JSPҳ��ʹ��
	 * 
	 * @param paramStr
	 * @return Java�������Խ���������
	 * @throws Exception
	 */
	public static String decryptStr(String paramStr) {
		StringBuffer sb = new StringBuffer();
		try {
			byte[] raw = new BigInteger(paramStr, 16).toByteArray();
			Cipher cipher = Cipher.getInstance(RSAUtility.RSA,
					new org.bouncycastle.jce.provider.BouncyCastleProvider());

			cipher.init(Cipher.DECRYPT_MODE, RSAUtility.generateRSAPrivateKey());

			int blockSize = cipher.getBlockSize();

			ByteArrayOutputStream bout = new ByteArrayOutputStream(64);

			int j = 0;
			while (raw.length - j * blockSize > 0) {
				bout.write(cipher.doFinal(raw, j * blockSize, blockSize));
				j++;
			}
			byte[] de_result = bout.toByteArray();
			sb.append(new String(de_result));
			// ���ؽ��ܵ��ַ���
			return RSAUtility.encrypt(sb.reverse().toString());
		} catch (Exception e) {
			throw new RuntimeException("JSPҳ����� ���ִ���", e);
		}
	}

	/**
	 * �ڲ����Է���
	 * 
	 * @param args
	 * @throws Exception
	 */
//	public static void main(String[] args) throws Exception {
//		String srcRes = RSAUtility.encrypt("mysql");
//
//		System.out.println(srcRes);
//	}

}
