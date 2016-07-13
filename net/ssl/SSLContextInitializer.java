package ssl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import sun.security.ssl.ProtocolVersion;
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

/**
 * SSlcontext相关类,包括keystore,签名算法,公钥私钥生成算法设置,和根证书和自签名证书
 * 通过init()方法获得sslcontext
 * 在init()之前可设置参数设置
 * 
 * @author Administrator
 *
 */
public class SSLContextInitializer {
	//签名算法
	private String signAlgorithm = "SHA1withRSA";
	//公钥私钥生成器的算法
	private String keyPairAlgorithm = "RSA";
	//键值对生成器的算法的长度,512～2048
	private int keyPairAlgorithmLength = 1024;
	//证书的版本号v1 v2 v3
	private int certificateVersion = CertificateVersion.V3;
	//证书有效期,起始时间
	private Date CertificateValidityStart = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24* 365 * 10);
	//证书有效期,结束时间
	private Date CertificateValidityEnd = new Date(System.currentTimeMillis()+ 1000L * 60 * 60 * 24 * 365 * 10);
	//keyManagerFactory 格式 默认SunX509
	private String keyManagerFactoryAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
	//SSLcontext 算法
	private String SSLContextAlgorithm = "TLS";
	//keystore 格式 默认jks
	private String keyStoreAlgorithm = KeyStore.getDefaultType();
	
	//keystore
	private KeyStore keyStore;
	//SSLContext
	private SSLContext sslContext;
	
	public SSLContextInitializer() {
		
	}
	/**
	 * 获得一个sslcontext,使用默认的keystore
	 */
	public SSLContext init(KeyStore ks,String pwd){
//		if(keyStore == null){
//			keyStore = createRootKeyStore();
//		}
		KeyManagerFactory keyManagerFactory;
		try {
			keyManagerFactory = KeyManagerFactory.getInstance(keyManagerFactoryAlgorithm);
			//初始化,必要参数keystore证书
			keyManagerFactory.init(ks, pwd.toCharArray());
			
			sslContext = SSLContext.getInstance(SSLContextAlgorithm);
//		sslContext = SSLContext.getDefault();// 或许默认sslcontext,不需要初始化
			sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());//null,会默认放入java的cacerts证书库里,密码changeit
			return sslContext;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用默认设置,创建一个根证书
	 * 签发人即自己
	 * 如果要修改设置,在调用此方法前,调用set方法修改参数
	 * @return
	 */
	public KeyStore createRootKeyStore(String as,String pwd,String x500SubjectName){


		try {
			//存放证书的keystore
			keyStore = KeyStore.getInstance(keyStoreAlgorithm);
			keyStore.load(null);// 必须加载,如果没有本地证书,加载null后续添加
			
			// 证书主题信息
			X500Name x500Name = new X500Name(x500SubjectName);

			// 生成密钥对
			KeyPairGenerator keyPairGenerator = KeyPairGenerator
					.getInstance(keyPairAlgorithm);
			keyPairGenerator.initialize(keyPairAlgorithmLength);//最小512
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
			caInfo.set(X509CertInfo.VERSION, new CertificateVersion(certificateVersion));
			// 主题信息
			caInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(
					x500Name));
			// 证书有效期
			caInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(CertificateValidityStart,CertificateValidityEnd));
			// 证书算法信息
//			caInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new AlgorithmId(AlgorithmId.sha1WithRSAEncryption_oid)));
//			caInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(new X500Signer(Signature.getInstance(signAlgorithm), x500Name).getAlgorithmId()));
			caInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(signAlgorithm)));
			// 证书签发者,即签发者自己,如果通过根证书签发子证书,签发者即根证书
			caInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(x500Name));

			// 证书类
			X509CertImpl certImpl = new X509CertImpl(caInfo);

			// 私钥签名,参数(私钥,算法),其中算法和上面的算法信息一致
			certImpl.sign(keyPair.getPrivate(), signAlgorithm);

			//放入keystore,用自己的私钥签名,给alias和密码
			keyStore.setKeyEntry(as, keyPair.getPrivate(),pwd.toCharArray(), new Certificate[] { certImpl });
			return keyStore;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	
	}

	/**
	 * 用跟证书发布一个子证书
	 * 签发人为根证书,签发私钥为根证书的私钥
	 * @param rootKeyStore
	 * @param rootAlias
	 * @param rootPwd
	 * @param subAlias
	 * @param subPwd
	 * @param x500SubjectName
	 * @return
	 */
	public KeyStore issueKeyStore(KeyStore rootKeyStore,String rootAlias,String rootPwd,String subAlias,String subPwd,String x500SubjectName){
		
		try {
			
			KeyStore subKeyStore = KeyStore.getInstance(keyStoreAlgorithm);
			subKeyStore.load(null);
			
			//获得根证书(主要为了签发人信息)和私钥(用来sign)
			Certificate rootCA = rootKeyStore.getCertificate(rootAlias);
			PrivateKey rootPrk = (PrivateKey) rootKeyStore.getKey(rootAlias, rootPwd.toCharArray());
			
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyPairAlgorithm);
			keyPairGenerator.initialize(keyPairAlgorithmLength);
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			
			//证书信息
			X509CertInfo certInfo = new X509CertInfo();
			X509CertImpl certImpl = new X509CertImpl(certInfo);
			//设置公钥
			certInfo.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
			//设置签名算法信息
			certInfo.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(AlgorithmId.get(signAlgorithm)));
			//设置签发者信息,这里为根证书的信息
			certInfo.set(X509CertInfo.ISSUER, new CertificateIssuerName(new X500Name(((X509CertImpl)rootCA).getIssuerX500Principal().getName())));
			//设置序列号
			certInfo.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(new SecureRandom().nextInt(Integer.MAX_VALUE)));
			//设置主题信息
			certInfo.set(X509CertInfo.SUBJECT, new CertificateSubjectName(new X500Name(x500SubjectName)));
			//设置版本号
			certInfo.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			//设置有效期
			certInfo.set(X509CertInfo.VALIDITY, new CertificateValidity(CertificateValidityStart, CertificateValidityEnd));
			//签名,用根证书的私钥签名
			certImpl.sign(rootPrk, signAlgorithm);
			subKeyStore.setKeyEntry(subAlias, keyPair.getPrivate(),subPwd.toCharArray(), new Certificate[]{certImpl,rootCA});
			return subKeyStore;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	
	public static void main(String[] args) throws Exception {
		SSLContextInitializer generator = new SSLContextInitializer();
		KeyStore rootKeyStore = generator.createRootKeyStore("root", "rootpwd","C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJRoot");
		KeyStore subKeyStore = generator.issueKeyStore(rootKeyStore, "root", "rootpwd", "sub", "subpwd", "C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJSub");
//		generator.loadLocalCertificate("d:\\tt2.cer","d:\\tt2.prk","tt2","rootpwd");
//		generator.loadLocalCertificate("d:\\tt.cer","d:\\tt.prk","tt1","rootpwd");
//		generator.loadLocalKeyStore("d:\\tt.keystore", "rootpwd");
		SSLServerSocket serverSocket = (SSLServerSocket) generator.init(subKeyStore,"subpwd").getServerSocketFactory().createServerSocket();
//		serverSocket.setNeedClientAuth(true);//双向验证
		for(String s:serverSocket.getEnabledCipherSuites()){
			System.out.println(s);
		}
		serverSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
		OutputStream out = null;
		Socket socket = null;
		try {
			serverSocket.bind(new InetSocketAddress(3344));
			while((socket=serverSocket.accept())!=null){
				byte [] b = new byte[1024];
				socket.getInputStream().read(b);
				System.out.println(new String(b));
				socket=serverSocket.accept();
				out = socket.getOutputStream();
				out.write("HTTP/1.1 200 OK\r\n".getBytes());
				out.write("Date: Sat, 20 Jun 2015 19:10:59 GMT\r\n".getBytes());
				out.write("Content-Type: text/html;charset=ISO-8859-1\r\n".getBytes());
				out.write("\r\n".getBytes());
				out.write("qweqwe".getBytes());
				
			}
			Thread.sleep(3000);
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				out.flush();out.close();socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 * @param filePath keystore路径
	 * @param pwd      keystore私钥相关的密码
	 */
	public KeyStore loadLocalKeyStore(String filePath,String pwd){
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(keyStoreAlgorithm);
			FileInputStream in = new FileInputStream(filePath);
			ks.load(in, pwd.toCharArray());
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ks;
	}
	
	/**
	 * 
	 * @param CaPath 证书路径
	 * @param prkPath 私钥路径
	 * @param alias    别名
	 * @param keyStore 存放证书的keystore
	 * @param pwd      私钥密码
	 * @return keystore
	 */
	public void loadLocalCertificate(String CaPath,String prkPath,String alias,String pwd){
		try {
			if(keyStore == null){
				keyStore = KeyStore.getInstance(keyStoreAlgorithm);
				keyStore.load(null);
			}
			
			Certificate cert = new X509CertImpl(new FileInputStream(CaPath));
			
			//读取私钥
			FileInputStream f = new FileInputStream(prkPath);
			byte[] b = new byte[f.available()];
			f.read(b);
			f.close();
			
			//转化私钥 PKCS8EncodedKeySpec 转化公钥用X509EncodedKeySpec 
			PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(b);
			PrivateKey key = KeyFactory.getInstance(keyPairAlgorithm).generatePrivate(encodedKeySpec);
//			PrivateKey key = (PrivateKey) keyStore.getKey("root", "rootpwd".toCharArray());私钥不匹配,可以正常放入keystore,但是访问时会报错
			keyStore.setKeyEntry(alias, key,pwd.toCharArray(), new Certificate[]{cert});//密码跟生成时有关,生成证书时设置的密码,这里设置成和keystore一样
			System.out.println("keystore证书实体个数:"+keyStore.size());
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	public KeyStore getKeyStore() {
		return keyStore;
	}
	public void setKeyStore(KeyStore keyStore) {
		this.keyStore = keyStore;
	}
	public SSLContext getSslContext() {
		return sslContext;
	}
	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public String getSignAlgorithm() {
		return signAlgorithm;
	}


	public void setSignAlgorithm(String signAlgorithm) {
		this.signAlgorithm = signAlgorithm;
	}


	public String getKeyPairAlgorithm() {
		return keyPairAlgorithm;
	}


	public void setKeyPairAlgorithm(String keyPairAlgorithm) {
		this.keyPairAlgorithm = keyPairAlgorithm;
	}


	public int getKeyPairAlgorithmLength() {
		return keyPairAlgorithmLength;
	}


	public void setKeyPairAlgorithmLength(int keyPairAlgorithmLength) {
		this.keyPairAlgorithmLength = keyPairAlgorithmLength;
	}


	public int getCertificateVersion() {
		return certificateVersion;
	}


	public void setCertificateVersion(int certificateVersion) {
		this.certificateVersion = certificateVersion;
	}


	public Date getCertificateValidityStart() {
		return CertificateValidityStart;
	}


	public void setCertificateValidityStart(Date certificateValidityStart) {
		CertificateValidityStart = certificateValidityStart;
	}


	public Date getCertificateValidityEnd() {
		return CertificateValidityEnd;
	}


	public void setCertificateValidityEnd(Date certificateValidityEnd) {
		CertificateValidityEnd = certificateValidityEnd;
	}

	public String getKeyManagerFactoryAlgorithm() {
		return keyManagerFactoryAlgorithm;
	}


	public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm) {
		this.keyManagerFactoryAlgorithm = keyManagerFactoryAlgorithm;
	}


	public String getSSLContextAlgorithm() {
		return SSLContextAlgorithm;
	}


	public void setSSLContextAlgorithm(String sSLContextAlgorithm) {
		SSLContextAlgorithm = sSLContextAlgorithm;
	}


	public String getKeyStoreAlgorithm() {
		return keyStoreAlgorithm;
	}


	public void setKeyStoreAlgorithm(String keyStoreAlgorithm) {
		this.keyStoreAlgorithm = keyStoreAlgorithm;
	}
}
