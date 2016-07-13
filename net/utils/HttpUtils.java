package utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class HttpUtils {
	/**
	 * 读取http 流中的一行
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	public static String readLine(InputStream in) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
		int i = 0;
		while(i!=-1){						
			i = in.read();
			out.write(i);
			if(('\r'==i && '\n'== in.read())){   //这里如果是'\r' 才读下一个
				out.write('\n');				//补上'\n'
				return new String(out.toByteArray(),"utf-8").replaceAll("\r\n",""); //写的太烂了
			}
		}
		throw new EOFException("连接断开");
	}
	
	public static String readLine2(InputStream in)
	        throws Exception
	    {
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int temp = 1;
	        int before = 1;
	        while (temp != -1)
	        {
	            temp = in.read();
	            out.write(temp);
	            
	            if ('\n' == temp && '\r' == before)
	            {
	                String message = new String(out.toByteArray(), "UTF-8").replaceAll("\r\n", "");
	                return message;
	            }
	            before = temp;
	        }
	        
	        throw new EOFException("到达流的末尾");
	    }
	public static void main(String[] args) {
		System.out.println(new Date((System.currentTimeMillis()+1000L*60*60*24*365*10)));
		System.out.println(1000*60*60*24*365*10);
		System.out.println(Integer.MAX_VALUE);
	}
}
