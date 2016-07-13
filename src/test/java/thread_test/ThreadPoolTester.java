package thread_test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTester {

	public static void main(String[] args) {
		ScheduledExecutorService newScheduledThreadPool = Executors.newScheduledThreadPool(1);
		newScheduledThreadPool.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				System.out.println("In runnable");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}, 1, 1, TimeUnit.SECONDS);
//		newScheduledThreadPool.scheduleAtFixedRate(new Runnable() {
//
//			@Override
//			public void run() {
//				System.out.println("In runnable");
//			}
//		}, 1, 10, TimeUnit.SECONDS);
	}

}
