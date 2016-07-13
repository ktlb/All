package net_test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.junit.Test;

public class HttpNioSender {
	/**
	 * 普通阻塞方式发送
	 * @throws IOException
	 */
	@Test
	public void blockSend() throws IOException {
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(true);
		client.connect(new InetSocketAddress("127.0.0.1", 6666));
		//这种方式,还是根据socketChannel和buffer来实现的,源码进行了重写,可能就是为了保留原本socket的操作习惯,其实没有必要,直接操作socketChannel
//		InputStream in = client.socket().getInputStream();
		
		//通过这种方式,需要注意flip
//		ByteBuffer send = ByteBuffer.allocate(1024);
//		send.put("this is a 测试!".getBytes("UTF-8"));
//		send.flip();

		//这种不能复用buffer
		client.write(ByteBuffer.wrap("this is a 测试!".getBytes("UTF-8")));
		client.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		SocketChannel client = SocketChannel.open();
		client.configureBlocking(false);
		client.register(HttpNioReceiver.selector,SelectionKey.OP_CONNECT);
		client.connect(new InetSocketAddress("127.0.0.1", 6666));
		client.finishConnect();
	}
	
}
