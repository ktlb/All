package net_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import ssl.SSLContextInitializer;

public class SSLServer {

	/**
	 * 测试sslsocket 和普通socket
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		byte [] b = new byte[1024];
		SSLContextInitializer initializer = new SSLContextInitializer();
		KeyStore rootKeyStore = initializer.createRootKeyStore("root",
				"rootpwd",
				"C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJRoot");
		KeyStore subKeyStore = initializer.issueKeyStore(rootKeyStore, "root",
				"rootpwd", "sub", "subpwd",
				"C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJSub");
		SSLContext sslContext = initializer.init(subKeyStore, "subpwd");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
		tmf.init((KeyStore)null);
		TrustManager[] trustManagers = tmf.getTrustManagers();
		SSLServerSocket serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket();
		serverSocket.bind(new InetSocketAddress("0.0.0.0", 2233));
		SSLSocket socket = (SSLSocket) serverSocket.accept();
		InputStream in = socket.getInputStream();
		OutputStream out = socket.getOutputStream();
		socket.addHandshakeCompletedListener(new HandshakeCompletedListener() {
			
			@Override
			public void handshakeCompleted(HandshakeCompletedEvent arg0) {
				System.out.println("Done");
			}
		});
		socket.startHandshake();
//		out.write("hello ssl from server1".getBytes());
//		out.flush();
		
		in.read(b);
		System.out.println("from client : "+new String(b));
		Thread.sleep(50000000);
		socket.close();
	}

}
