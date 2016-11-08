package thread_test;

public class Single {

	private static Single instance = null;
	public static String a = null;

	public Single() {
		a = "in single";
	}

	public static Single getInstance() {
		if (instance == null) {

			synchronized (Single.class) { // 1
				if (instance == null) // 2
					instance = new Single(); // 3
			}
		}
		return instance;

	}

	public static void main(String[] args) {
		for (int i = 0; i < 2; i++) {

			new Thread() {
				@Override
				public void run() {
					Single.getInstance();
				}
			}.start();
		}
	}
}
