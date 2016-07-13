package lock;

import java.util.concurrent.locks.ReentrantLock;

public class MyLock extends ReentrantLock{

	private static final long serialVersionUID = 3835071352371471443L;

	@Override
	public Thread getOwner() {
		return super.getOwner();
	}
}
