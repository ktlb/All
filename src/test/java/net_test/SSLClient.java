package net_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.X509TrustManager;

public class SSLClient {
	public static HandshakeStatus  handStatus;
	
	public static SSLEngineResult result;
	
	public static Runnable runnable;
	
	public static SSLEngine sslEngine;

	public static void main(String[] args) throws Exception, IOException {

		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.1", 2233));
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		socket.setSoTimeout(5000);
		// 初始化sslengine
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext
				.init(null,
						new X509TrustManager[] { new SSLClient().new MyX509TrustManager() },
						null);
		sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(true);
		sslEngine.beginHandshake();
		handStatus = sslEngine.getHandshakeStatus();
		log(null,handStatus.toString());
		// 创建通信所用的2个buffer
		ByteBuffer appIn = ByteBuffer.allocate(sslEngine.getSession()
				.getApplicationBufferSize());
		ByteBuffer appOut = ByteBuffer.allocate(sslEngine.getSession()
				.getApplicationBufferSize());
		ByteBuffer netIn = ByteBuffer.allocate(sslEngine.getSession()
				.getPacketBufferSize());
		ByteBuffer netOut = ByteBuffer.allocate(sslEngine.getSession()
				.getPacketBufferSize());
		
		// 开始握手
		while (handStatus != SSLEngineResult.HandshakeStatus.FINISHED) {
			
			switch (handStatus) {
			// 发送探测消息,就是hello消息
			case NEED_WRAP:
				result = sslEngine.wrap(appOut, netOut); // 没有真实的业务数据,握手中所用的加密数据
				doTask();
				handStatus = sslEngine.getHandshakeStatus();
				log(result,"[Wrap]");
//				handStatus = result.getHandshakeStatus();
				out.write(getBytes(netOut));
				appOut.flip();
				netOut.clear();
				out.flush();
				break;

			case NEED_UNWRAP:
				int i = -1;
				try {
					while ((i = in.read()) != -1) {
						netIn.put((byte) i);
					}
				} catch (Exception e) {
				}
				netIn.flip();//netIn.limit(netIn.position()).position(0);
				result = sslEngine.unwrap(netIn, appIn);
				doTask();
				appIn.clear();
				handStatus = sslEngine.getHandshakeStatus();
				log(result,"[UnWrap]");
				netIn.clear();
				break;
			case NEED_TASK:
				handStatus = doTask();
				break;
			case NOT_HANDSHAKING:
				System.out.println("NOT_HANDSHAKING : 握手完成");
				break;
			}

		}
		System.out.println("FINISHED : 握手完成");
		
	}
	
	public static HandshakeStatus doTask(){
		while ((runnable = sslEngine.getDelegatedTask()) != null) {
			runnable.run();
		}
		return sslEngine.getHandshakeStatus();
	}

	public static byte[] getBytes(ByteBuffer net) {
		byte[] b = new byte[net.position()];
		net.position(0);
		net.get(b);
		return b;
	}

	public static void socket() throws Exception {
		byte[] b = new byte[1024];
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("127.0.0.1", 2233));
		System.out.println(socket);
		OutputStream out = socket.getOutputStream();
		out.write("hello ssl".getBytes());// 可以正常连,但是必须传输ssl的数据,普通数据不可以
		InputStream in = socket.getInputStream();
		in.read(b);
		System.out.println(new String(b));
		socket.close();
	}

	public static void sslSocket() throws Exception {
		byte[] b = new byte[1024];
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext
				.init(null,
						new X509TrustManager[] { new SSLClient().new MyX509TrustManager() },
						null); // 只验证服务端
		// sslContext.init(null, null, null); //这样初始化,无法存储server的证书,会报Received
		// fatal alert: certificate_unknown
		Socket socket = sslContext.getSocketFactory().createSocket();
		socket.connect(new InetSocketAddress("127.0.0.1", 2233));
		System.out.println("socket : " + socket);
		socket.getInputStream().read(b);
		socket.getOutputStream().write("sent from client".getBytes());
		System.out.println(new String(b));
		socket.close();
	}

	class MyX509TrustManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}
	
	public static void log(SSLEngineResult rs,String status){
		System.out.println("SSLEngineHandStatus : "+status);
		if(rs!=null)
		System.out.println( "NextHandStatus : "+handStatus+"\r\nresultStatus : "+rs.getStatus()+" , "+"cosumed : "+rs.bytesConsumed()+" , "+"produced : "+rs.bytesProduced());
		System.out.println("-------------------------------------");
	}

}
