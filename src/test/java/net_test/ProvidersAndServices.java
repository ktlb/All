package net_test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.security.Signature;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

import ssl.SSLContextInitializer;
import sun.security.jca.Providers;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;
//import sun.security.x509.X500Signer;

public class ProvidersAndServices {

	public ProvidersAndServices() {
	}

	public static void main(String[] args) {
		try {
			Signature signature = Signature.getInstance("SHA1WITHRSA");
			System.out.println(signature.getAlgorithm());
			System.out.println(signature.getProvider());
			//1.7 ,1.8这个类已经不支持了.慎用sun包
//			X500Signer signer = new X500Signer(signature, new X500Name(
//					"C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJ"));
//			System.out.println(signer.getAlgorithmId().getOID());
//			System.out.println(signer.getInfo());
//			System.out.println(signer.getName());
//			System.out.println(signer.getSigner());

			System.out.println(new AlgorithmId(
					AlgorithmId.sha1WithRSAEncryption_OIW_oid).getOID());
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Provider> l = Providers.getProviderList().providers();
//		List<Provider> l = Providers.getFullProviderList().providers();
		Provider[] providers = Security.getProviders();
		for (Provider p : providers) {
			System.out.println(p.getName()+" : ");
			Set<Service> s = p.getServices();
			Iterator<Service> i = s.iterator();
			while (i.hasNext()) {
				Service ss = i.next();
				if (ss.getType().equalsIgnoreCase("keyManagerFactory")){ 
//				if(ss.getAlgorithm().equalsIgnoreCase("Sunx509")){
					System.out.println(ss.getAlgorithm());
					System.out.println(ss.getType());
				}
			}
			System.out.println("***************");
		}
	}
}
