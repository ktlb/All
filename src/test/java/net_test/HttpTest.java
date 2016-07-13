package net_test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

import org.junit.Test;

import proxy.ProxyServer;
import proxy.ProxyServerForHttps;
import ssl.SSLContextInitializer;
import utils.HttpUtils;

public class HttpTest {

	/**
	 * 测试readLine方法
	 */
	@Test
	public void test1() {
		try {
			ServerSocket serverSocket = new ServerSocket(3355);
			Socket socket = null;
			int i = 0;
			while ((socket = serverSocket.accept()) != null) {
				i++;
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				String line = null;
				while (true) {
					if ("".equals(line = HttpUtils.readLine(in))) {
						System.out.println("分割行");
						break;
					}
					System.out.println(line);
				}
				// in.close();
				// in.read(b);
				// System.out.println(new String(b));
				out.write("HTTP/1.1 200 This is a test!\r\n".getBytes());
				out.write("Date: Sat, 20 Jun 2015 19:10:59 GMT\r\n".getBytes());
				out.write("Content-Type: text/html;charset=utf-8\r\n"
						.getBytes());
				out.write("\r\n".getBytes());
				out.write(("第" + i + "个").getBytes("utf-8"));
				out.flush();
				// out.close();
				socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 代理测试 ProxyServer
	 */
	@Test
	public void test2() {
		try {
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("0.0.0.0", 3355));
			Socket socket = null;
			while ((socket = serverSocket.accept()) != null) {
				new Thread(new ProxyServer(socket)).start();
				;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("0.0.0.0",
					ProxyServer.httpPort));
			new Thread() {
				public void run() {
					SSLContextInitializer generator = new SSLContextInitializer();
					KeyStore rootKeyStore = generator.loadLocalKeyStore(
							"d:\\root.keystore", "rootpwd");
					KeyStore subKeyStore = generator
							.issueKeyStore(rootKeyStore, "root", "rootpwd",
									"sub", "subpwd",
									"C=China,ST=JiangSu,L=Nanjing,OU=WJHome,O=WJ.inc,CN=WJSub");
					SSLContext sslContext = generator.init(subKeyStore,
							"subpwd");
					try {
						ServerSocket serverSocket2 = sslContext
								.getServerSocketFactory().createServerSocket();
						serverSocket2.bind(new InetSocketAddress("0.0.0.0",
								ProxyServer.httpsPort));
						Socket socket = null;
						while ((socket = serverSocket2.accept()) != null) {
							new Thread(new ProxyServerForHttps(socket)).start();
						}
					} catch (IOException e) {
					}
				};
			}.start();
			Socket socket = null;
			while ((socket = serverSocket.accept()) != null) {
				new Thread(new ProxyServer(socket)).start();
				;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试jdk1.7自带的mime类型 2种都不全啊
	 * 
	 * @throws IOException
	 */
	@Test
	public void test3() throws IOException {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor1 = fileNameMap
				.getContentTypeFor("c:\\dasdas\\sdas.lib");
		String contentTypeFor2 = fileNameMap.getContentTypeFor("xzc.rar"); // null
		String contentTypeFor3 = fileNameMap.getContentTypeFor("sdas.zip");
		String type1 = Files.probeContentType(Paths.get("xxx.rar"));
		String type2 = Files.probeContentType(Paths.get("xxx.zip"));
		String type3 = Files.probeContentType(Paths.get("xxx.jpg"));
		String type4 = Files.probeContentType(Paths.get("xxx.exe"));
		System.out.println(contentTypeFor1);
		System.out.println(contentTypeFor2);
		System.out.println(contentTypeFor3);
		System.out.println(type1);
		System.out.println(type2);
		System.out.println(type3);
		System.out.println(type4);
	}

	/**
	 * 测试Inetsocketaddress 不需要http:// 不依赖与协议的
	 * 
	 * @throws IOException
	 */
	@Test
	public void test4() throws IOException {
		Socket socket = new Socket();
		// socket.connect(new InetSocketAddress("www.baidu.com", 443));
		socket.connect(new InetSocketAddress("www.baidu.com", 80));
		socket.setSoTimeout(5000);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		// out.write("CONNECT www.baidu.com:443 HTTP/1.1\r\n".getBytes());
		out.write("GET / HTTP/1.1\r\n".getBytes());
		// out.write("Host:www.baidu.com:443\r\n".getBytes());
		out.write("Host:www.baidu.com:80\r\n".getBytes());
		out.write("User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0\r\n"
				.getBytes());
		out.write("Connection:keep-alive\r\n".getBytes());
		out.write("\r\n".getBytes());
		out.flush();
		int i;
		FileOutputStream f = new FileOutputStream(
				"C:\\Users\\Administrator\\Desktop\\xx.html");
		while ((i = in.read()) != -1) {
			f.write(i);
		}
		f.flush();
		f.close();
		socket.close();
	}

	/**
	 * 测试sslcontext
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	@Test
	public void test5() throws IOException, Exception {
		SSLContext ssl = SSLContext.getInstance("SSL");
		ssl.init(null, null, null);
		SSLParameters p = ssl.getDefaultSSLParameters();// 默认是TLSv1
		for (String s : p.getProtocols()) {
			System.out.println(s);
		}

		SSLContext tls = SSLContext.getInstance("TLS");
		tls.init(null, null, null);
		SSLParameters p2 = tls.getDefaultSSLParameters();// 默认是TLSv1
															// ,获得是一个clone副本,修改了不能应用在连接上
		for (String s : p2.getProtocols()) {
			System.out.println(s);
		}
		System.out.println("-----------");
		for (String s : tls.getSupportedSSLParameters().getProtocols()) {
			System.out.println(s);
		}

	}

	/**
	 * 测试form-data和 www-urlencoded-
	 */
	@Test
	public void test6() throws Exception{
		ServerSocket server = new ServerSocket(1111);
		Socket socket = null;
		while((socket = server.accept())!=null){
			new Processor(socket){
			}.start();;
		}
		server.close();
	}
	class Processor extends Thread{
		private Socket socket;
		public Processor(Socket socket) {
			this.socket = socket;
		}
		public void run() {
			try{
			socket.setSoTimeout(3000);
			InputStream in = socket.getInputStream();
			byte[] b = new byte[1024];
			int i = -1;
			while((i =in.read(b))!=-1){
				System.out.print(new String(b,0,i,"UTF-8"));
			}
			}catch(Exception e){
				
			}finally {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
