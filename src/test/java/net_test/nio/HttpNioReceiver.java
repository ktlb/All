package net_test.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class HttpNioReceiver {
	public HttpNioReceiver(){
	}
	/**
	 * nio文件测试,不支持异步
	 * @throws IOException
	 */
	@Test
	public void fileChannelTest() throws IOException {
		FileInputStream fin = new FileInputStream("D:\\test.txt");
		FileOutputStream fout = new FileOutputStream("D:\\test2.txt");
		FileChannel readChannel = fin.getChannel();
		FileChannel writeChannel = fout.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int i = -1;
		while ((i = readChannel.read(buffer)) != -1) {
			// buffer.limit(buffer.position());
			// buffer.position(0);
			buffer.flip();
			writeChannel.write(buffer);
			byte[] b = new byte[i];
			buffer.flip();//写也会改变三要素的位置,需要再次重置
			buffer.get(b);
			System.out.println(new String(b, "UTF-8"));
			// buffer.limit(buffer.capacity());
			// buffer.position(0);
			buffer.clear();
			// buffer.rewind();
		}
		writeChannel.close();
		fout.close();
		readChannel.close();
		fin.close();
	}
	/**
	 * socketChannel 简单使用,这样使用,是完全浪费,没有使用异步io
	 * @throws Exception 
	 */
	@Test
	public void blockChannelTest() throws Exception{
		ServerSocketChannel server = ServerSocketChannel.open(); 
		server.configureBlocking(true);
		server.bind(new InetSocketAddress("127.0.0.1", 6666));
		//两种bind方式没有区别 ,返回的socket是ServerSocketAdaptor 适配,源码还是 ServerSocketChannelImpl.bind
//		server.socket().bind(new InetSocketAddress("127.0.0.1", 6666)); 
		SocketChannel socketChannel = server.accept();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		//直接分配内存方式,有时需要cleaner().clean,大容量时,优于上面方式 
		//ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		while (socketChannel.read(buffer) != -1) {
			toStream(buffer, System.out);
		}
		System.out.println(socketChannel);
	}
	
	/**
	 * 异步io,使用selector
	 * @throws Exception 
	 */
	@Test
	public void nonBlockChannelTest() throws Exception{
		ServerSocketChannel server = ServerSocketChannel.open(); 
		server.configureBlocking(false);
		server.bind(new InetSocketAddress("127.0.0.1", 6666));
		
		Selector selector = Selector.open();
		
		//注册时间,serverSocketChannel 只能注册accept
		server.register(selector, SelectionKey.OP_ACCEPT);
		
	}
	public static Selector selector;
	static {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {
		HttpNioReceiver receiver = new HttpNioReceiver();
		ServerSocketChannel server = ServerSocketChannel.open(); 
		server.configureBlocking(false);
		server.bind(new InetSocketAddress("127.0.0.1", 6666));
		
		//注册时间,serverSocketChannel 只能注册accept
		server.register(selector, SelectionKey.OP_ACCEPT);
		receiver.listen(selector);
		
	}
	
	
	private void listen(Selector sel) throws IOException {
		while(selector.select() != -1){
			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while(iterator.hasNext()){
				SelectionKey key = iterator.next();
				if(key.isAcceptable()){
					ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = serverChannel.accept();
					socketChannel.configureBlocking(false);
					socketChannel.register(selector, SelectionKey.OP_READ);
					
				}else if(key.isConnectable()){
					
				}else if(key.isReadable()){
					SocketChannel socketChannel = (SocketChannel) key.channel();
					boolean bool = socketChannel.isConnected();//无法判断客户端断开,只能通过read -1判断
					if (!bool) {
						socketChannel.close();
						continue;
					}
				}else if(key.isWritable()){
					//一般不用注册此事件,根据系统写入缓冲区来判断, 如果大量的写入,缓冲区满,可以注册,但是写完后最好取消注册
					key.cancel();
				}
					
				
				iterator.remove();
			}
		}
	}
	public void toStream(ByteBuffer buffer,OutputStream out) throws IOException{
		byte [] b = new byte[buffer.position()];
		buffer.limit(buffer.position());
		buffer.position(0);
		buffer.get(b);
		buffer.clear();
		if(out instanceof PrintStream){
			((PrintStream)out).println(new String(b,"UTF-8"));
		}else{
			out.write(b);
		}
	}
}
