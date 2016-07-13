package thread_test;

public class SyncTest2 {
	private String str;
	
	public String getStr() {
		return str;
	}
	public void setStr(String str) {
		this.str = str;
	}

	public static void main(String[] args) {
		final SyncTest2 s1 = new SyncTest2();
		for (int i = 0; i < 3; i++) {

			new Thread() {
				public void run() {
					SyncTest2 s2 = new SyncTest2();
					s2.setStr("sss");
					s1.a(s2);
				};
			}.start();
		}
	}
	public void a(SyncTest2 syc){
		System.out.println(syc);
		synchronized (syc.getStr().intern()) {//intern相当于一个静态的map(string 对象池) ,
			System.out.println("in lock");
		}
	}
}
