package thread_test;

public class ThreadViewer {
	public static void main(String[] args) {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		ThreadGroup topGroup = group;
		// 遍历线程组树，获取根线程组
		while (group != null) {
			topGroup = group;
			group = group.getParent();
		}
		int estimatedSize = topGroup.activeCount() * 2;
		Thread[] slackList = new Thread[estimatedSize];
		// 递归的获取当前组和 子线程组中的线程
		topGroup.enumerate(slackList);
		for(Thread t : slackList){
			if(t != null){
				System.out.println(t.getName());
			}
		}
	}
}
