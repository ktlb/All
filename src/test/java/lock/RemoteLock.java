package lock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 万物乃通信
 * 
 */
public class RemoteLock implements Serializable {
	private static final long serialVersionUID = 7258122488206365441L;

	AtomicInteger count = new AtomicInteger();

	PipedInputStream in = new PipedInputStream();

	PipedOutputStream out = new PipedOutputStream(in);

	BufferedReader read = new BufferedReader(new InputStreamReader(in));

	public RemoteLock() throws IOException {
		out.write("first\r\n".getBytes());
	}

	public void lock() {
		try {
			read.readLine();
		} catch (IOException e) {
		}
		count.incrementAndGet();
	}

	public void unlock() {
		if (count.get() != 1) {
			throw new IllegalMonitorStateException();
		}
		count.addAndGet(-1);
		try {
			out.write("unlock\r\n".getBytes());
		} catch (IOException e) {
		}
	}
}
