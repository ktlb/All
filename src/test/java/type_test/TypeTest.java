package type_test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

public class TypeTest<T, E> implements Bb2.Bb<String, Integer> {

	public static void main(String[] args) throws Exception {
		// Method method = TypeTest.class.getMethod("a",
		// String.class);//获取public 方法
		Method method = TypeTest.class.getDeclaredMethod("a", String.class);// 获取所有
		System.out.println(method.getReturnType());// class [[I
		// -----------------
		Class<?> c = TypeTest.class;
		System.out.println(c.getTypeParameters());// 泛型类型
		System.out.println(c.getGenericInterfaces()[0] instanceof ParameterizedType);// ParameterizedType类型
		System.out.println(((ParameterizedType) c.getGenericInterfaces()[0]).getActualTypeArguments()[0]);// String
		System.out.println(((ParameterizedType) c.getGenericInterfaces()[0]).getActualTypeArguments()[1]);// Integer
		System.out.println(((ParameterizedType) c.getGenericInterfaces()[0]).getRawType());// 原本的类型,这里就是Bb
		System.out.println(((ParameterizedType) c.getGenericInterfaces()[0]).getOwnerType());// 如果是内部类,返回外部类类型 这里是Bb2
	
	}

	public int[] a(String s) {
		return null;
	}

}

class Aa<T> {

}

interface Bb2<T, E> {

	public abstract interface Bb<T, E> {

	}
}

