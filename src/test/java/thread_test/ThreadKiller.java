package thread_test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ThreadKiller {
	public static void main(String[] args)  {
		Thread2 t = new Thread2();
		t.start();
		t.aa();
		synchronized (t) {
		try {
			t.wait();
		} catch (InterruptedException e) {
		}	
		}
		System.out.println(t.isInterrupted());
	}
	
}

class Thread2 extends Thread{
	@Override
	public void run() {
		MessageDigest m = null;
		BASE64Encoder base64Encoder = new BASE64Encoder();
		BASE64Decoder base64Decoder = new BASE64Decoder();
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		for(int i=0;i<1000000;i++){
			try {
				base64Decoder.decodeBuffer(base64Encoder.encode(m.digest("hahaqweqweqweqweqweqweqweqweqasdadasdzcxzcxzcxzcxzcxzczxcxzcxzdasasdasdsadasdasdas".getBytes())));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public void aa(){
		try{
			int a = 1/0;
		}catch(Exception e){
			this.stop();
		}
	}
}