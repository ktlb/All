package thread_test;

/**
 * 由于没有获取锁,所以无法实例化,导致需要这个实例的对象一直等
 * @author Administrator
 *
 */
public class SyncTest
{
    private static Syn syn = new Syn();
    
    public static void main(String[] args)
        throws InterruptedException
    {
    	new SyncTest().new A();
        new Thread()
        {
            public void run()
            {
                syn.syn();             //持有syn锁对象,,但是没有实例化Static,因为static块没有执行完.所以一直是RUNNABLE状态,导致40行等待
            }
        }.start();
        System.out.println(Static.a+" in main");
    }
    
    static class Static
    {
        static String a = "hellow";
        static
        {
            synchronized (syn)
            {
                System.out.println(a+" in static");                    //main 21行先执行了,实例化Static,但是没有syn锁,等待,导致实例化阻塞
            }
        }
    }
    
    static class Syn
    {
    	public static void  a(){
    		
    	}
        public synchronized void syn()						//非静态同步方法的锁也是实例对象,即第9行的syn
        {
            System.out.println(Static.a+" in syn");          //Thread0,先执行就能正常执行程序
            
        }
    }
    /*
     * 如果CPU先实例化了Static 那么程序就能正常,但是这个实例化是不可预测的,要避免
		hellow in static
		hellow in syn
		hellow in main
     */
    class A extends Syn{
    	public A(){
    		System.out.println("in a");
    	}
    }
}
