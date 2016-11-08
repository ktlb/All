package javaassist_test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;

public class FieldReplacer {

	public static void main(String[] args) throws Exception {
		ClassPool pool = new ClassPool();
		pool.insertClassPath("D:\\selfspace\\All\\src\\test\\java\\classes_dex2jar.jar"); 
		CtClass ctClass = pool.getCtClass("com.baidu.location.LocationClientOption");
		CtField field = ctClass.getField("enableSimulateGps");
		ctClass.removeField(field);
		CtField ctField = new CtField(pool.get("boolean"), "enableSimulateGps", ctClass);
		ctField.setModifiers(AccessFlag.PRIVATE);
		ctClass.addField(ctField, "true");
		ctClass.writeFile("D:\\jj");
	}

}
