package clone_test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CloneTest {

	public static void main(String[] args) throws Exception {
		//重写clone 方式深度拷贝,必须要声明Cloneable接口,如果多个内部引用,可能每个都要重写,比较麻烦
		Teacher t = new Teacher();
		Student s = new Student();
		t.setTname("Tom");
		t.setStu(s);
		s.setSname("Jerry");
		
		System.out.println(t);
		System.out.println(t.getStu());
		System.out.println(s);
		System.out.println("----after clone-----");
		
		t = (Teacher) t.clone();
		s = (Student) s.clone();
		System.out.println(t);
		System.out.println(t.getStu());
		System.out.println(s);
		System.out.println("*********************");
		//使用Object对象流方式实现深度拷贝,不要重写clone ,但是得实现Serializable接口
		Teacher t2 = new Teacher();
		Student s2 = new Student();
		t2.setTname("Jack");
		t2.setStu(s2);
		s2.setSname("Jerry");
		System.out.println(t2);
		System.out.println(t2.getStu());
		//写入
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		oout.writeObject(t2);
		//读出
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
		ObjectInputStream oin = new ObjectInputStream(bin);
		Object object = oin.readObject();
		if(object instanceof Teacher)
		{
			t2 = (Teacher)object;
			System.out.println("----after clone-----");
			System.out.println(t2);
			System.out.println(t2.getStu());
		}
	}

}
class Teacher implements Cloneable,Serializable{
	private String tname;
	private Student stu;
	public String getTname() {
		return tname;
	}
	public void setTname(String tname) {
		this.tname = tname;
	}
	public Student getStu() {
		return stu;
	}
	public void setStu(Student stu) {
		this.stu = stu;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Teacher t = (Teacher) super.clone();
		t.setStu((Student)stu.clone());;
		return t;
	}
}
class Student implements Cloneable,Serializable{
	private String sname;
	public String getSname() {
		return sname;
	}
	public void setSname(String sname) {
		this.sname = sname;
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
