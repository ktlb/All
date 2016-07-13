package regex_test;

public class RegexTest {
	/**
	 *  ? {0,1}
	 * 	* 0+
	 *  + 1+
	 *  | 或
	 *  && 交集
	 *  () 代表组
	 *  [] 代表范围
	 */
	private static String str = "+123";
//	private static String regex = "(.)*";
	private static String regex = "(\\+){1}\\d*";//或者[+]{1}\\d
	public static void main(String[] args) {
		System.out.println(str.matches(regex));
	}

}
