package zip_test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipReader {
	
	public static void main(String[] args) throws Exception {
//		InputStream fin = ZipReader.class.getResourceAsStream("/xx.zip"); //当前包下取 用xx.zip  ,classpath下只能/xx.zip
		FileInputStream fin = new FileInputStream("xx.zip");
		ZipInputStream in = new ZipInputStream(fin);
		
		ZipEntry zipEntry ;
		while((zipEntry= in.getNextEntry())!=null){
			System.out.println("name : "+zipEntry.getName());   //目录和目录下的文件也会单独输出 ,递归的找出, 用zipentry.isDirectory()判断
			System.out.println("Method : "+zipEntry.getMethod());
			System.out.println("Size : "+zipEntry.getSize());
			System.out.println("CompressedSize : "+zipEntry.getCompressedSize());
		}
		in.close();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream("zz.zip"));
		out.putNextEntry(new ZipEntry("aa.txt"));;
		out.write("hello world!".getBytes());
		out.putNextEntry(new ZipEntry("hh/aa.txt"));;	//带有目录
		out.write("hello world!".getBytes());
		out.close();
	}

}
