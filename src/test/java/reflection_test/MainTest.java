package reflection_test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Date;
import java.util.Enumeration;

import org.junit.Test;

import sun.reflect.Reflection;
class T{
	static{
		//class.forName 只会加载静态块,所以jdbc用class.forName,在静态块中直接绑定到jdbcRegister
		System.out.println("in static block");
	}
	{
		System.out.println("in block");
	}
	public T() {
		System.out.println("in construtor");
	}
	private String a ="test";
}
public class MainTest {
	
	public static void main(String[] args) throws Exception {
		Class<?> clazz = Class.forName("reflection_test.T");
	}
	@Test
	public void getField() throws Exception{
		T t = new T();
		Field field = t.getClass().getDeclaredField("a");
//		field.setAccessible(true);//没有这行,就会报错,因为 field为 非本类的private 类型
		System.out.println(field.get(t));
		field.set(t, "test2");
		System.out.println(field.get(t));
	}
	/**
	 * 获得方法名
	 */
	@Test
	public void getMethod() throws Exception{
		Method method = MainTest.class.getMethod("get",new Class[]{String.class,Integer.class});
		System.out.println(method);
		for(Type c :method.getGenericParameterTypes()){
			System.out.println(c);
		}
	}
	
	/**
	 * 扫描classpath下的文件
	 * @throws Exception
	 */
	@Test
	public void scanClasspath() throws Exception{
		System.out.println(this.getClass().getResource("MainTest.class"));//包括自己所在的包名
		System.out.println(this.getClass().getClassLoader().getResource(""));//不包括
		System.out.println(this.getClass().getClassLoader().getResources(""));//enumeration
		System.out.println(this.getClass().getClassLoader().getSystemResource(""));
		System.out.println(this.getClass().getClassLoader().getSystemResources(""));
		Enumeration<URL> r1 = ClassLoader.getSystemResources("");
		while(r1.hasMoreElements()){
			System.out.println(r1.nextElement());
		}
		System.out.println("------------");
		Enumeration<URL> r2 = this.getClass().getClassLoader().getResources("");
		while(r2.hasMoreElements()){
			System.out.println(r2.nextElement());
		}
	}
	/**
	 * 测试spring 是如何扫描指定包中的类
	 */
	@Test
	public void scanClass(){
		//0或小于0,返回本身,大于0,就调用的上层类
		System.out.println(Reflection.getCallerClass(0).getClassLoader());//获得调用该方法的类
		try {
			//获取controller包中的文件
			Enumeration<URL> resources = MainTest.class.getClassLoader().getResources("controller");
			while(resources.hasMoreElements()){
				URL url = resources.nextElement();
				System.out.println(url);
//				File file = new File(url.getFile().replaceAll("%20", " "));
				File file = new File(url.toURI().getPath());   //URI 会内部转码
				System.out.println(url.toURI());
				System.out.println(url.toURI().getPath());
				System.out.println(file.isDirectory());
				File[] files = file.listFiles();
				//拼凑完整类名
				Class<?> clazz = MainTest.class.getClassLoader().loadClass("controller."+files[0].getName().replaceAll(".class", ""));
				
				Method method = clazz.getMethod("sayHello", null);
				method.invoke(clazz.newInstance(), null);
				//结束,原理很简单
				System.out.println(clazz);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断不同加载器加载父子类
	 * @throws Exception 
每个ClassLoader加载Class的过程是：
1.检测此Class是否载入过（即在cache中是否有此Class），如果有到8,如果没有到2
2.如果parent classloader不存在（没有parent，那parent一定是bootstrap classloader了），到4
3.请求parent classloader载入，如果成功到8，不成功到5
4.请求jvm从bootstrap classloader中载入，如果成功到8
5.寻找Class文件（从与此classloader相关的类路径中寻找）。如果找不到则到7.
6.从文件中载入Class，到8.
7.抛出ClassNotFoundException.
8.返回Class.
	 */
	@Test
	public void judgeClassLoader() throws Exception{
		LoaderA a = new LoaderA(new Date());// 总是默认调用父类加载器加载, 先findloaded ,再父类load 再find, 
		LoaderB b = new LoaderB(new Date());//所以重写,最好重写find, 可以故意的让父类加载不了,逼迫自己实现的加载器去加载
		System.out.println(a.getParent());//class.forName 默认使用类加载器去加载,所以总是一样的
		System.out.println(b.getParent());
		Class<?> classA1 = a.loadClass("spring_test.MainTest");
		Class<?> classA2 = a.loadClass("spring_test.MainTest");
		System.out.println(classA1.equals(classA2));
		Class<?> classA3 = b.loadClass("spring_test.MainTest");
		System.out.println(classA1.equals(classA3));
		MainTest la = (MainTest) classA3.newInstance();
		System.out.println(la);
		Class<?> classB = a.loadClass("spring_test.LoaderB");
	}
}
class LoaderA extends ClassLoader{
	public LoaderA(Date date) {
		System.out.println(date);
	}
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return Class.forName(name);//class.forName 默认使用类加载器去加载,所以总是一样的
	}
}
class LoaderB extends ClassLoader{
	public LoaderB(Date date) {
		System.out.println(date);
	}
}
