package enum_test;
public class EnumTest {
    
    public static void main(String[] args)
    {
        Season spring = Season.Spring;
        System.out.println(spring.ordinal());
        System.out.println(spring.name());
        System.out.println(spring.getClass());//重写方法后,class Season$2,生成类似代理类的类
        System.out.println(spring.getDeclaringClass()); //原本的类型
        Season.valueOf("Fall").aa();
        for(Season s :Season.values()){
            System.out.println(s);
        }
        Enum.valueOf(Season.class, "Fall").aa();
        
    }
    
}
enum Season{
    Spring{
        public void aa()//枚举的类型,相当于一个本枚举类型的一个对象
        {
            super.aa();
            System.out.println("in spring");
        }
    },
    Summer,
    Fall{
        public void aa()//枚举的类型,相当于一个本枚举类型的一个对象
        {
            super.aa();
            System.out.println("in Fall");
        }
    },
    Winter;
    
    public void aa(){
        System.out.println("in Season");
    }
}