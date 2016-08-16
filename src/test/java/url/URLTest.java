package url;

import java.net.URL;
import java.net.URLEncoder;


public class URLTest {

	public static void main(String[] args) throws Exception {
		URL url = new URL(
				"http://zd-user-auth-test.oss-cn-hangzhou.aliyuncs.com/exp/500028/idcard.png?Expires=1462603698&OSSAccessKeyId=2KKWBU9puaZzhtY3&Signature=tNXjKKFxL1YG3UzmjfWXX4vls+E=");
		System.out.println(url.getProtocol());
		System.out.println(url.getPath());
		System.out.println(url.getHost());
		System.out.println(url.getPort());
		System.out.println(url.getFile());
		System.out.println(url.getQuery());
		String string = url.toURI().toASCIIString();
		System.out.println(string);
		System.out.println(URLEncoder.encode(url.getQuery(), "UTF-8"));

		add("1");
	}

	public static void add(String aa) {
		String a = aa + "qq";
	}

}
