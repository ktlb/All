package lock;

import java.util.Collection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyLock extends ReentrantLock{

	private static final long serialVersionUID = 3835071352371471443L;

	@Override
	public Thread getOwner() {
		return super.getOwner();
	}
	
	@Override
	public Collection<Thread> getQueuedThreads() {
		return super.getQueuedThreads();
	}
	/**
	 * 扩展
	 */
	@Override
	public Collection<Thread> getWaitingThreads(Condition condition) {
		return super.getWaitingThreads(condition);
	}
	public static void main(String[] args) {
		ReentrantLock lock = new ReentrantLock();
//		lock 无法获取下述方法
		MyLock myLock = new MyLock();
		myLock.getOwner();
		myLock.getQueuedThreads();
		myLock.getWaitQueueLength(null);
		
	}
}
