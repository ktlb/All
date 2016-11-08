package proxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import utils.HttpUtils;

public class HttpHeader {

	private Map<String, String> headers = new HashMap<String, String>();

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public HttpHeader(InputStream in) {
		try {
			analyse(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析封装成map
	 * 
	 * @param in
	 * @throws IOException
	 */
	private void analyse(InputStream in) throws IOException {
		String headerLine;
		while (!"".equals(headerLine = HttpUtils.readLine(in))) {
			String[] temp = headerLine.split(":", 2);
			if (temp.length == 2) {
				headers.put(temp[0].trim(), temp[1].trim());
			} else {
				headers.put(temp[0].trim(), "");
			}
		}
	}

	/**
	 * 如果这个头有值,则返回覆盖掉的值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public String add(String key, String value) {
		return headers.put(key, value);
	}

	/**
	 * 如果存在并且remove掉,返回true
	 * 其余一律返回false
	 * @param key
	 * @return
	 */
	public String remove(String key) {
		return headers.remove(key);
	}

	/**
	 * 获得头对应的值
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return headers.get(key);
	}

	public int size() {
		return headers.size();
	}

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.bind(new InetSocketAddress(3355));
		Socket socket = null;
		while ((socket = serverSocket.accept()) != null) {
			InputStream in = socket.getInputStream();
			String requestLine = HttpUtils.readLine(in);
			System.out.println("请求行:" + requestLine);
			HttpHeader headers = new HttpHeader(in);
			System.out.println("请求头:" + headers.size());
			System.out.println(headers.get("Connection"));
			socket.close();
		}
		serverSocket.close();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		Set<String> keySet = headers.keySet();
		Iterator<String> it = keySet.iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = headers.get(key);
			sb.append(key + ":" + value + "\r\n");
		}
		return sb.toString();
	}
	
	public byte[] getBytes(){
		return toString().getBytes();
	}
}
