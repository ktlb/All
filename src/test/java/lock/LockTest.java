package lock;
/**
 * 测试持有锁的时候,其他线程能不能修改锁对象
 * @author Administrator
 *
 */
public class LockTest {
	private TestLock lock = new TestLock("lock-1");
	public static void main(String[] args) {
		final LockTest test = new LockTest();
		new Thread(){
			public void run() {
				test.lock();
			};
		}.start();
		new Thread(){
			public void run() {
				test.change();
			};
		}.start();
	}
	
	public void lock(){
		synchronized (lock) {
			synchronized (this) {//debug这里,可以看到该线程持有两个锁
				System.out.println(lock);
			}
		}
	}
	
	public void change(){
		lock.setName("lock-2");
	}
	
	
}
class TestLock {
	private String name;
	
	public TestLock(String aa){
		name = aa;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
	
}
