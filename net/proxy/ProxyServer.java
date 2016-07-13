package proxy;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import utils.HttpUtils;

/**
 * 虽然是仿写的,但是自己也学了很多
 * @author WJ
 *
 */
public class ProxyServer implements Runnable {

	public static int httpsPort =8081;
	public static int httpPort =8080;
	/**
	 * 浏览器 连上来的socket
	 */
	private Socket browser;
	/**
	 * ie到本代理服务器的输入流 浏览器--->proxyServer
	 */
	private InputStream browserIn;
	/**
	 * 本代理服务器到浏览器的输出流 proxyServer--->浏览器
	 */
	private OutputStream browserOut;
	/**
	 * 需要连到远端服务器实际网址的socket
	 */
	private Socket server;
	/**
	 * 服务器到代理服务器的输入流 server-->proxyServer
	 */
	private InputStream serverIn;
	/**
	 * 代理服务器到服务器的输出流 proxyServer-->server
	 */
	private OutputStream serverOut;
	
	/**
	 * 远端服务器的域名地址
	 */
	private String remoteHost;
	/**
	 * 远端服务器的端口
	 */
	private int remotePort;
	/**
	 * 请求头
	 */
	private HttpHeader requestHeaders;
	/**
	 * 响应头
	 */
	private HttpHeader responseHeaders;
	/**
	 * 请求行	
	 */
	private HttpRequestLine requestLine;
	/**
	 * 响应行	
	 */
	private HttpResponseLine responseLine;
	/**
	 * 隧道穿透模式
	 */
	private boolean tunnel = false;
	
	private boolean ssl = false;
	
	public ProxyServer(Socket socket) throws IOException {
		browser = socket;
		browser.setSoTimeout(10000); //10s
		browserIn = browser.getInputStream();
		browserOut = browser.getOutputStream();
	}

	@Override
	public void run() {
		try {
			
			// 读取第一行请求行
			requestLine = new HttpRequestLine(browserIn);
			//封装请求头
			requestHeaders = new HttpHeader(browserIn);
			//获取远端服务器ip  必须大写,没有做大小写的支持,apache 默认全部是小写放入
			String [] host = requestHeaders.get("Host").split(":",2);
			remoteHost = host[0];
			
			//HTTP CONNECT是在HTTP1.1协议上才新增的命令，用于支撑https加密,一般只开放443
			if("connect".equalsIgnoreCase(requestLine.getMethod())){
				if(tunnel){
					if(host.length==2){
						remotePort = Integer.valueOf(host[1]);
					}else{
						remotePort = 443;
					}
					//建立远端连接
				}else{
					//利用双重转发 browser-->httpProxy-->httpsproxy-->server
					ssl = true;
				}
				initConnections();
				doProxyWithConnect();
			}
			//http
			else{
				if(host.length==2){
					remotePort = Integer.valueOf(host[1]);
				}else{
					remotePort = 80;
				}
				//建立远端连接
				initConnections();
				doProxy();
			}
			
		} catch (Exception e) {
			if(!(e instanceof EOFException ||e instanceof UnknownHostException)){ //eof的异常,就不打出来了,说明那时连接已经关闭
										//找不到这个网址
			}              //异常就不打了,大多是连接关闭,连不上远端服务器
			
		}finally{
				//流没有及时关,导致火狐的Authenticate认证弹出来会延迟很多,可能火狐和其他浏览器的对连接的处理不一样
			close(browserIn,browserOut,browser,serverIn,serverOut,server);
		}
	}
	

	public static void main(String[] args) throws Exception{
//		try {
//			ServerSocket serverSocket = new ServerSocket();
//			serverSocket.bind(new InetSocketAddress("0.0.0.0", 8080));
//			Socket socket = null;
//			while ((socket = serverSocket.accept()) != null) {
//				new Thread(new ProxyServer(socket)).start();
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		//格式的位数不同,也会导致format后的格式不同,详见api
		Format f = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'",Locale.US);
		System.out.println(f.format(new Date()));
		System.out.println((byte)255);
	}
	
	private void close(Closeable... close){
		for(Closeable c:close){
			try {
				if(c!=null)
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void doProxy() throws Exception {
		
		//firefox ,默认把Proxy-Connection 替换成Connection,但是firefox出现了proxy-connection 和connection同时存在的情况
		//为了避免不必要的麻烦,替换掉
		String temp ;
		if((temp = requestHeaders.remove("Proxy-Connection"))!=null){
			requestHeaders.add("Connection", temp);         //不替换貌似也不影响  ,没有必要,如果为了伪造原生的请求,可以替换掉
		}
		//从浏览器中读取请求体,发给server
		dispatch(browserIn,serverOut,requestLine,requestHeaders);
		
		//发送完后,读取响应
		responseLine = new HttpResponseLine(serverIn);
		responseHeaders = new HttpHeader(serverIn);
		
		//从服务器器读取响应,发给浏览器
		dispatch(serverIn, browserOut, responseLine, responseHeaders);
	}
	/**
	 * 从in中读取,发给out,需要先发送http行,和headers中参数
	 * @param in
	 * @param out
	 * @param line
	 * @param headers
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	private void dispatch(InputStream in, OutputStream out,HttpLine line,HttpHeader headers)
			throws IOException, URISyntaxException {
		// 向远端发送请求行和头  请求行不能是url,只能是uri 不然有的会访问出错
		if(line instanceof HttpRequestLine){
			URL url = new URL(line.getUrl());
			line.setUrl(url.getFile().toString());
		}
		out.write(line.getBytes());
		out.write(headers.getBytes());
		out.write("\r\n".getBytes());
//		out.flush(); 			//不能这样flush,如果connection是close,发了头,就立马关闭了
		
		String length = headers.get("Content-Length");
		String chunked = headers.get("Transfer-Encoding");
		String connection = headers.get("Connection");
		// content-length模式
//		if (length != null && !length.trim().equals("0")) {                   //这个不需要判断有的content-length为0的情况,在流的读取,就实现了
		if (length!=null) {
			// 发送请求体
			sendByContentLength(Integer.parseInt(length), out, in);
		}
		// chunked 分段模式 第一行表示块大小(16进制表示),结束0块(footer)结束,即0\r\n \r\n 空的foot块
		else if (chunked!=null && "chunked".equalsIgnoreCase(chunked)) {
			sendByChunkedMode(in,out);
		}
		//两者都没有,只能一直读到末尾了,connection:close flash貌似回的响应就是close ,一直异步推送流媒体
		else if(connection!=null && "close".equalsIgnoreCase(connection)){
			sendTillClose(out,in);
		}
		//除了以上三种,应该就是普通的get请求,或者其他的Option,delete,put,HEAD,TRACE 以后研究 TODO
		out.flush();
	}
	
	/**
	 * 一直发送,直到读到-1,连接关闭
	 * @param out
	 * @param in
	 * @throws IOException 
	 */
	private void sendTillClose(OutputStream out, InputStream in) throws IOException {
		int i;
		while((i = in.read())!=-1){
			out.write(i);
		}
	}

	/**
	 * 用chunked模式读取,并发送
	 * @param in
	 * @param out
	 * @throws IOException 
	 */
	private void sendByChunkedMode(InputStream in, OutputStream out) throws IOException {
		String length = null;
		while(!(length = HttpUtils.readLine(in).trim()).equals("0")){
			out.write((length+"\r\n").getBytes());                        //发送长度
			sendByContentLength(Integer.parseInt(length,16), out, in);		//chunked是以16进制来表明长度的
			in.skip(2);			//跳过\r\n
			out.write("\r\n".getBytes());                //这需要再写入\r\n,\r\n并不包含在content-length里 
		}
		out.write((length+"\r\n\r\n").getBytes());						  //发送0块 0\r\n\r\n
	}

	/**
	 * 从in中根据长度读取请求体 ,然后转发给远端out
	 * @param length
	 * @param out
	 * @param in
	 * @throws IOException
	 */
	private void sendByContentLength(int length,OutputStream out,InputStream in) throws IOException {
		int i = 0;  
		ByteArrayOutputStream bout = new ByteArrayOutputStream(length);
		while(i<length){
			int b =in.read();
			if(b!=-1)		//不写入-1
				bout.write(b);
			i++;
		}
		out.write(bout.toByteArray());
		
//		byte [] temp = new byte[length];  没有读满就返回了,网络流,一定都要考虑流中数据不完整的问题
//		in.read(temp);
//		out.write(temp);
//		DataInputStream din = new DataInputStream(in);
//		byte [] temp = new byte[length];
//		din.readFully(temp);
//		out.write(temp);
	}

	/**
	 * https 会直接返回一个响应,表明连接建立,然后才开始通信
	 * @throws Exception
	 */
	private void doProxyWithConnect() throws Exception {
		//首先返回一个连接建立成功的相应
		browserOut.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
//		不允许带任何头,只能发个响应行,不然报错
//		browserOut.write(("Date: "+new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'",Locale.US).format(new Date())+"\r\n\r\n").getBytes());
		browserOut.flush();
		
//		https 内容全部加密了,读取不到content-length chunked close之类的,而且连接是长连接,也读不到流的末尾,只能通过available来判断
		new Thread(){
			public void run() {
				try {
					sendTillClose(serverOut, browserIn);
				} catch (IOException e) {
				}
				
			};
		}.start();
		sendTillClose(browserOut, serverIn); //另起一个线程也不错
//		sendWithAvailable(browserOut, serverOut, browserIn, serverIn);       //这样也可以
	}

	/**
	 *	建立远端连接
	 * @throws IOException
	 */
	private void initConnections() throws IOException{
		
		server = new Socket();
		if(ssl){
			server.connect(new InetSocketAddress("0.0.0.0", ProxyServer.httpsPort));
		}else{
			server.connect(new InetSocketAddress(remoteHost,remotePort), 10000); //设置连接超时
		}
		server.setSoTimeout(10000);
		serverIn = server.getInputStream();
		serverOut = server.getOutputStream();
	}
	
	private void sendWithAvailable(OutputStream browserOut,OutputStream serverOut,InputStream browserIn,InputStream serverIn) throws Exception{
		int total = 0;
		while(true){
			  if (serverIn.available() == 0 && browserIn.available() == 0)
              {
                  Thread.sleep(100);
                  total++;
                  if (total == 50)				//因为网络流不仅仅是读取完才会available为0,流的不确定性
                  {
                      break;
                  }
              }
			byte [] send = new byte[browserIn.available()];
			browserIn.read(send);
			serverOut.write(send);
			serverOut.flush();
			if(send.length>0){
				total = 0;						//不管什么情况下,只要available为0了都要延迟5s,防止读取不全
			}
			byte [] receive = new byte[serverIn.available()];
			serverIn.read(receive);
			browserOut.write(receive);
			browserOut.flush();
		}
		
	}
}
