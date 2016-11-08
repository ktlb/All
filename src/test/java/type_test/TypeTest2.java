package type_test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeTest2 extends c1<Map<? extends Number, ? super List>> {

	public static void main(String[] args) throws Exception {
		Class<?> c = TypeTest2.class;
		Map<? extends Number, ? extends Number> map = new HashMap<>();
		map.getClass().getGenericInterfaces();
		//WildcardType
		System.out.println(
				((ParameterizedType) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0])
						.getActualTypeArguments()[0] instanceof WildcardType);// 参数化类型中是Map
																				// 的类型,Map中又是个通配符类型
		Type t1 = ((ParameterizedType) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0])
				.getActualTypeArguments()[0];
		Type t2 = ((ParameterizedType) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0])
				.getActualTypeArguments()[1];
		// Number
		((WildcardType) t1).getLowerBounds();// 没有下界
		((WildcardType) t1).getUpperBounds();// 上届 number
		// List
		((WildcardType) t2).getLowerBounds();// 下界List
		((WildcardType) t2).getUpperBounds();// 上界 Object
		//WildcardType
		// --------------------------------
		Type t3 = ((ParameterizedType) TypeTest2.class.getDeclaredMethod("a", Map.class).getGenericParameterTypes()[0])
				.getActualTypeArguments()[0];
		System.out.println(t3 instanceof WildcardType);
		// GenericArrayType								//泛型,就是Object
		Type t4 = TypeTest2.class.getDeclaredMethod("b", Object[].class).getGenericParameterTypes()[0];
		System.out.println(t4 instanceof GenericArrayType);// true
		System.out.println(((GenericArrayType)t4).getGenericComponentType());
		TypeTest2.class.getDeclaredMethod("c", String.class);
		
		System.out.println(1.1234d+2.23456d);
		System.out.println(add(1.1234,2.23456));
	}

	public static strictfp double add(double a,double b){//保证在各平台间结果一致，IEEE标准优先，性能其次,精度并没有加强,可能是只是为了在各平台形成标准
		return a+b;
	}
	
	public void a(Map<? extends Number, ? super Integer> t) {

	}
	
	public <T>void b(T[] t) {

	}
	public int c(String s){
		return 0;
	}

}

interface i1<E extends Number> {

}

class c1<T> {

}
strictfp class c2{
	
	public strictfp void a(){
		
	}
}
