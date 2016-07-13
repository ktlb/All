package jmx;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.util.List;

public class JMXTest {
	public static void main(String[] args) {
		//类加载情况
		classLoad();
		//编译情况
		compilation();
		//gc后可以查看gc的次数和时间等信息
		System.gc();
		gc();
		//内存管理器
		memoryManager();
		
	}
	
	public static void classLoad(){
		ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
//		classLoadingMXBean.setVerbose(true);//显示加载了哪些类
		System.out.println(classLoadingMXBean.isVerbose());
		System.out.println(classLoadingMXBean.getLoadedClassCount());
		System.out.println(classLoadingMXBean.getTotalLoadedClassCount());
		System.out.println(classLoadingMXBean.getUnloadedClassCount());
		System.out.println(classLoadingMXBean.getObjectName());//java.lang:type=ClassLoading
	}
	
	public static void compilation(){
		CompilationMXBean compilationMXBean = ManagementFactory.getCompilationMXBean();
		System.out.println("Name:"+compilationMXBean.getName());
		System.out.println("TotalCompilationTime:"+compilationMXBean.getTotalCompilationTime());
		System.out.println("CompilationTimeMonitoringSupported"+compilationMXBean.isCompilationTimeMonitoringSupported());
		System.out.println("ObjectName:"+compilationMXBean.getObjectName());
	}
	
	public static void gc(){
		List<GarbageCollectorMXBean> list = ManagementFactory.getGarbageCollectorMXBeans();
		for(GarbageCollectorMXBean bean : list){
			System.out.println("CollectionCount:"+bean.getCollectionCount());
			System.out.println("CollectionTime:"+bean.getCollectionTime());
			System.out.println("Name:"+bean.getName());
			System.out.println("MemoryPoolNames:");
			for(String s : bean.getMemoryPoolNames()){
				System.out.println("\t"+s);
			}
			System.out.println("ObjectName:"+bean.getObjectName());
		}
	}
	
	public static void memoryManager(){
		List<MemoryManagerMXBean> list = ManagementFactory.getMemoryManagerMXBeans();
		for(MemoryManagerMXBean bean : list){
			System.out.println("Name:"+bean.getName());
			System.out.println("MemoryPoolNames:");
			for(String s : bean.getMemoryPoolNames()){
				System.out.println("\t"+s);
			}
			System.out.println("ObjectName:"+bean.getObjectName());
		}
	}
}
