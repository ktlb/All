package lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Apple implements Runnable {
	private int num = 50;
	// private final Lock lock = new ReentrantLock(true);
	private final Lock lock = new ReentrantLock();

	public void run() {
		for (int i = 0; i < 50; i++) {
			eat();
		}
	}

	private void eat() {
		lock.lock();
		try {
			if (num > 0) {// 进入方法立马加锁
				System.out.println(Thread.currentThread().getName() + " 吃了编号为 : " + num-- + " 的苹果");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放锁
			lock.unlock();
		}
	}
}

public class T {
	public static void main(String[] args) {
		// 创建三个线程
		Apple a = new Apple();
		new Thread(a, "A").start();
		new Thread(a, "B").start();
		new Thread(a, "C").start();
	}
}