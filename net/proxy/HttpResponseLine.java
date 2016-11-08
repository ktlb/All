package proxy;

import java.io.IOException;
import java.io.InputStream;

import utils.HttpUtils;

public class HttpResponseLine extends HttpLine {

	public HttpResponseLine(InputStream in) throws Exception {
		this.httpLine = HttpUtils.readLine(in);
		analyse();
	}

	private void analyse() throws Exception {
		String[] temp = httpLine.split(" ", 3);
		if (temp.length == 3) {
			protocol = temp[0];
			statusCode = temp[1];
			description = temp[2];
		} else {
			throw new Exception("请求行不符合要求");
		}
	}
	
	@Override
	public String toString() {
		return protocol+" "+statusCode+" "+description+"\r\n";
	}
}
