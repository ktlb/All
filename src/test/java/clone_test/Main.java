package clone_test;

import java.io.FileInputStream;

import sun.misc.BASE64Decoder;

public class Main {
	public static void main(String[] args) throws Exception {
		BASE64Decoder decoder = new BASE64Decoder();
		byte[] bs = decoder.decodeBuffer(new FileInputStream("C:\\Users\\Administrator\\Desktop\\qq.txt"));
		System.out.println(new String(bs));
	}
}
