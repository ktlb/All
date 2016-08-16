package net_test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLSession;
/**
 * 模拟和百度进行SSL握手
 * @author WJ
 *
 */
public class SSLClient {

	private SSLEngine sslengine;
	/**
	 * wrap 源数据
	 */
	private ByteBuffer w_src;
	/**
	 * wrap 后发送到对端
	 */
	private ByteBuffer w_dst;

	/**
	 * un_wrap 来自对端的
	 */
	private ByteBuffer u_src;
	/**
	 * un_wrap 后的源数据
	 */
	private ByteBuffer u_dst;

	private Socket socket;
	private OutputStream out;
	private InputStream in;

	public static void main(String[] args) {
//		System.setProperty("javax.net.debug", "all");
		try {
			SSLClient client = new SSLClient();
			client.initSSL();
			client.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void simpleConnect() throws IOException {
		initSocket();
		StringBuilder request = new StringBuilder("GET / HTTP/1.1\r\n");
		request.append("Host: www.baidu.com\r\n")
				.append("Connection: keep-alive\r\n")
				.append("Pragma: no-cache\r\n")
				.append("Cache-Control: no-cache\r\n")
				.append("Pragma: no-cache\r\n")
				.append("User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36\r\n")
				.append("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n")
				.append("Accept-Encoding: gzip, deflate, sdch, br\r\n")
				.append("Accept-Language: zh-CN,zh;q=0.8\r\n")
				.append("\r\n");
		out.write(request.toString().getBytes());
		out.flush();
		skipHeader(in);
		print(in);
		socket.close();
	}

	private void initSocket() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress("180.97.33.108", 443), 3000);
//		socket.connect(new InetSocketAddress("180.96.11.189", 443), 3000);
		socket.setSoTimeout(1000);
		out = socket.getOutputStream();
		in = socket.getInputStream();
	}

	private void connect() throws IOException {
		initSocket();
		handShake();
	}

	private HandshakeStatus handshakeStatus;
	private Status status;

	private void handShake() throws IOException {
		sslengine.beginHandshake(); //显示的调用,否则,handshakestatus 为NOT_HANDSHAKING
		handshakeStatus = sslengine.getHandshakeStatus();
		// HandshakeStatus.FINISHED 只能根据wrap/unwrap获取
		while (handshakeStatus != HandshakeStatus.FINISHED) { 
			switch (handshakeStatus) {
			case NEED_WRAP:
				SSLEngineResult wrapResult = sslengine.wrap(w_src, w_dst);
				out.write(bytes(w_dst));
				out.flush();
				handshakeStatus = wrapResult.getHandshakeStatus();
				status = wrapResult.getStatus();
				w_src.clear();
				break;
			case NEED_UNWRAP:
				read(in, u_src);
				u_src.flip();
//				do{
					SSLEngineResult unwrapResult = sslengine.unwrap(u_src, u_dst);
					handshakeStatus = unwrapResult.getHandshakeStatus();
					status = unwrapResult.getStatus();
//				}while(handshakeStatus == HandshakeStatus.NEED_UNWRAP && status == Status.OK
//						&& u_src.hasRemaining()); //3
					/**
					 * 缓存byte数组设置过大
					 * 1,有的时候,没有读取完,需要多次unwrap(这里需要再次读)
					 * 2,读取完了,但是没有执行完task,需要多次unwrap (这里不能再次读)
					 * 3,读取多了,需要多次unwrap(这里不能再次读)
					 */
					
				if (u_src.hasRemaining()) {
					u_src.compact();// 还没有读完
				} else {
					u_src.clear();
				}
				break;
			case NEED_TASK:
				Runnable task = null;
				while ((task = sslengine.getDelegatedTask()) != null) {
					task.run();
				}
				handshakeStatus = sslengine.getHandshakeStatus();
				break;
			case FINISHED:
				break;
			case NOT_HANDSHAKING:
				System.out.println("握手完成,可以传输业务数据");
				break;
			default:
				break;
			}
		}
		System.out.println("handshakeStatus :" + handshakeStatus);
//		HandshakeStatus handshakeStatus = sslengine.getHandshakeStatus(); //为NOT_HANDSHAKING
	}

	private byte[] bytes(ByteBuffer buff) {
		byte[] b = new byte[buff.position()];
		buff.flip();// 可读
		buff.get(b);
		buff.clear();// 下次可写
		return b;
	}

	private void initSSL() throws Exception {
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, null, null);
		sslengine = context.createSSLEngine();
		sslengine.setUseClientMode(true);
		SSLSession sslSession = sslengine.getSession();
		w_src = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
		w_dst = ByteBuffer.allocate(sslSession.getPacketBufferSize());
		u_src = ByteBuffer.allocate(sslSession.getPacketBufferSize());
		u_dst = ByteBuffer.allocate(sslSession.getApplicationBufferSize());
//		 System.out.println("支持的协议: " +
//		 Arrays.asList(sslengine.getSupportedProtocols()));
//		 System.out.println("启用的协议: " +
//		 Arrays.asList(sslengine.getEnabledProtocols()));
//		 System.out.println("支持的加密套件: " +
//		 Arrays.asList(sslengine.getSupportedCipherSuites()));
//		 System.out.println("启用的加密套件: " +
//		 Arrays.asList(sslengine.getEnabledCipherSuites()));
	}

	/**
	 * 普通socket,读取响应后直接关闭
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void print(InputStream in) throws IOException {
		byte[] temp = new byte[1024];
		int size = -1;
		while ((size = in.read(temp)) != -1) {
			System.out.println(new String(temp, 0, size, "UTF-8"));
		}
		in.close();
	}

	private void read(InputStream in, ByteBuffer buff) throws IOException {
		byte[] temp = new byte[1];
		try {
			int size = in.read(temp);
			if (size > 0) {
				buff.put(temp, 0, size);
			}
		} catch (Exception e) {
			e.printStackTrace();// 读满为止
		}
	}

	private void skipHeader(InputStream in) throws IOException {
		int i = 0;
		while (i != -1) {
			i = in.read();
			if (('\n' == i && '\r' == in.read())) { // 这里如果是'\r' 才读下一个
				in.skip(1);// 跳过下一个\n
				break;
			}
		}
	}
}
