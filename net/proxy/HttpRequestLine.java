package proxy;

import java.io.IOException;
import java.io.InputStream;

import utils.HttpUtils;

public class HttpRequestLine extends HttpLine {

	public HttpRequestLine(String requestLine) {
		this.httpLine = requestLine;
		try {
			analyse();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public HttpRequestLine(InputStream in) throws Exception {
		this.httpLine = HttpUtils.readLine(in);
		analyse();
	}

	private void analyse() throws Exception {
		String[] temp = httpLine.split(" ", 3);
		if (temp.length == 3) {
			method = temp[0].trim();
			url = temp[1].trim();
			protocol = temp[2].trim();
		} else {
			throw new Exception("请求行不符合要求");
		}
	}

	public static void main(String[] args) {
		// HttpRequestLine line = new HttpRequestLine("GET /xx HTTP/1.1");
		HttpRequestLine line = new HttpRequestLine(
				"GET /yLsHczq6KgQFm2e88IuM_a/s?callback=jQuery110207110524577781864_1436094431725&request_type=8&sample_name=bear_brain&_=1436094431727 HTTP/1.1");
		System.out.println(line.getMethod());
		System.out.println(line.getUrl());
		System.out.println(line.getProtocol());
	}
	
	@Override
	public String toString() {
		return method+" "+url+" "+protocol+"\r\n";
	}
}
