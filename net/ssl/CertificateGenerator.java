package ssl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import sun.security.jca.Providers;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

public class CertificateGenerator {

	public static void main(String[] args) {
		SSLContext sslContext = null;
		try {
			// KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			KeyManagerFactory keyManagerFactory = KeyManagerFactory
					.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			//初始化,必要参数keystore证书
			KeyStore keyStore = createRootKeyStore();
			keyManagerFactory.init(keyStore, "rootpwd".toCharArray());
			
			sslContext = SSLContext.getInstance("Tls");
//			sslContext = SSLContext.getDefault();// 或许默认sslcontext,不需要初始化
			sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());//null,会默认放入java的cacerts证书库里,密码changeit
			SSLServerSocket sslServerSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket();
			sslServerSocket.bind(new InetSocketAddress(3344));
			
//			FileOutputStream fo1 = new FileOutputStream("d:\\tt.prk"); 
//			fo1.write(keyStore.getKey("root", "rootpwd".toCharArray()).getEncoded());//导出私钥
//			fo1.flush();
//			fo1.close();
			
			FileOutputStream fo2 = new FileOutputStream("d:\\root.cer"); 
			fo2.write(keyStore.getCertificate("root").getEncoded());//导出证书
			fo2.flush();
			fo2.close();
			
			FileOutputStream fo3 = new FileOutputStream("d:\\root.keystore");//导出keystore
			keyStore.store(fo3, "rootpwd".toCharArray());
			fo3.flush();
			fo3.close();
			
			
			System.out.println("Done------");
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * 根证书,签发人即自己
	 * 
	 * @return 
	 */
	public static KeyStore createRootKeyStore() {

		try {
			//存放证书的keystore
			KeyStore rootKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			rootKeyStore.load(null);// 必须加载,如果没有本地证书,加载null后续添加
			
			// 证书主题信息
			X500Name x500Name = new X500Name(
					"C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJRoot");

			// 生成密钥对
			KeyPairGenerator keyPairGenerator = KeyPairGenerator
					.getInstance("RSA");
			keyPairGenerator.initialize(1024);//最小512
			KeyPair keyPair = keyPairGenerator.generateKeyPair();

			// set实体类信息 实体属性certificate开头
			X509CertInfo caInfo = new X509CertInfo();
			// 公钥
			caInfo.set(X509CertInfo.KEY,
					new CertificateX509Key(keyPair.getPublic()));
			// 序列号
			caInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(
					new SecureRandom().nextInt(Integer.MAX_VALUE)));
			// 版本号
			caInfo.set(X509CertInfo.VERSION, new CertificateVersion(
					CertificateVersion.V3));
			// 主题信息
			caInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(
					x500Name));
			// 证书有效期
			caInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(
					new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24
							* 365 * 10), new Date(System.currentTimeMillis()
							+ 1000L * 60 * 60 * 24 * 365 * 10)));
			// 证书算法信息
			caInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));
			// 证书签发者,即签发者自己,如果通过根证书签发子证书,签发者即根证书
			caInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(x500Name));

			// 证书类
			X509CertImpl certImpl = new X509CertImpl(caInfo);

			// 私钥签名,参数(私钥,算法),其中算法和上面的算法信息一致
			certImpl.sign(keyPair.getPrivate(), "SHA1withRSA");

			//放入keystore,用自己的私钥签名,给alias和密码
			rootKeyStore.setKeyEntry("root", keyPair.getPrivate(),"rootpwd".toCharArray(), new Certificate[] { certImpl });
			return rootKeyStore;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
