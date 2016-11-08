package compiler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class CompilerTester {

	public static void main(String[] args) throws Exception {
		System.out.println(System.getProperty("java.compiler"));
		//此类 在jvm启动时加载,可以指定jvm的编译器
		System.out.println(Compiler.compileClass(CompilerA.class));
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		ClassLoader loader = ToolProvider.getSystemToolClassLoader();
		String intance = "com.sun.tools.javac.api.JavacTool";
		Class<?> forName = Class.forName(intance,false,compiler.getClass().getClassLoader());
//		Class<?> forName = Class.forName(intance);
		System.out.println(forName.getProtectionDomain().getCodeSource().getLocation());
		CompilerTester.class.getResource(intance);
		Enumeration<URL> resources = ClassLoader.getSystemResources(intance);
		while(resources.hasMoreElements()){
			System.out.println(resources.nextElement());
		}
		//url 写法不对,会导致找不到类
//		URLClassLoader loader2 = new URLClassLoader(new URL[]{new URL("file://D:\\java\\jdk1.7.0_15\\lib\\tools.jar")});
		URLClassLoader loader2 = new URLClassLoader(new URL[]{new URL("file:/D:\\java\\jdk1.7.0_15\\lib\\tools.jar")});
		File file = new File("D:\\java\\jdk1.7.0_15\\lib\\tools.jar");
		System.out.println(file.toURI().toURL());
		Class<?> javacTool = loader2.loadClass(intance);
		System.out.println(javacTool);
	}

}
