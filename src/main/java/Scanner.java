import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
/**
 * 端口扫描器
 * @author WJ
 */
public class Scanner {
	
	private String ip;
	/**
	 * 超时,毫秒级
	 */
	private int timeout;
	/**
	 * 线程数
	 */
	private int thread;
	
	public Scanner(String ip,int timeout,int thread) {
		this.ip = ip;
		this.timeout = timeout;
		this.thread = thread;
	}

	/**
	 * 测试过程中开启20个线程扫描,导致无法上网,初步估计是syn_sent 连接过多,导致无法上网
	 * @param args
	 */
	public static void main(String[] args) {
		new Scanner("localhost", 1000, 20).scan();
	}
	public void scan() {
		int avg = 65535 / thread;
		for(int i = 0;i<thread;i++){
			int start  = i*avg; 
			int end = (i+1)*avg;
			if(i+1 == thread ){ //最后的端口
				end = 65535;
			}
			new Thread(new Task(start, end)).start();
		}
	}
	class Task implements Runnable {
		private int start;
		
		private int end;
		
		public Task(int start,int end) {
			this.start = start;
			this.end = end;
		}
		public void run() {
			for(int port = start;port < end;port++){
				Socket socket = null;
				try{
					socket = new Socket();
					socket.connect(new InetSocketAddress(ip, port),timeout);
				}catch(Exception e){
					//ignore
					continue;
				}finally{
					close(socket);
				}
				System.out.println(port);
			}
		}
	}
	public void close(Closeable... cc){
		for(Closeable c : cc){
			if(c!=null){
				try {
					c.close();
				} catch (IOException e) {
					//ignore
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * tomcat 默认关闭信号
	 * @param args
	 * @throws Exception
	 */
	public static void shutdown(String[] args) throws Exception {
		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("118.178.113.56", 8005),3000);
		OutputStream out = socket.getOutputStream();
		out.write("SHUTDOWN".getBytes());
		out.flush();
		out.close();
		socket.close();
	}
}
