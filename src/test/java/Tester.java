import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Tester {

	public static void main(String[] args) {
		final List<String> list = new ArrayList<>();
//		list.add("YqB5JF6i6HGg3QcQzjw5Ag==TyLnJD");
		list.add("PjrAWvaFuIFDZI7fIl+6Yw==TyLnJD");
//		list.add("9n63ATMMIkGMWqQ+IyETdw==TyLnJD");
//		list.add("QMO2H7M/LlPs/b48hZkb/w==TyLnJD");
//		list.add("RLNsgz8HB+k45y6CIVomkg==TyLnJD");
//		list.add("hYEnugifcvgiJlhOXDutNg==TyLnJD");
//		list.add("F7HvE7fdzTLTn9Y/3GW7hg==TyLnJD");
//		list.add("llbXn5OzM0Blvjp014wAhw==TyLnJD");
//		list.add("J68BMb/ONWmzuhByvkQ/dQ==TyLnJD");
//		list.add("W2ADepTTW7d8XjvALj6Hzg==TyLnJD");
		for(int i = 0;i<1;i++){
			final String temp = list.get(i);
			new Thread(){
				public void run() {
					try {
						HttpURLConnection con = (HttpURLConnection) new URL("http://127.0.0.1:8082/axp-acl/user/login.htm").openConnection();
//						HttpURLConnection con = (HttpURLConnection) new URL("http://127.0.0.1:1111/axp-acl/user/login.htm").openConnection();
						con.setDoOutput(true);
						con.setDoInput(true);
						con.setReadTimeout(3000);
						con.setRequestMethod("POST");
						OutputStream out = con.getOutputStream();
						String param = "data={\"userName\":\""+temp+"\",\"clientId\":\"12456\",\"accountPwd\":\"KzfIBfMP/ga5TWs77CH7Vw==TyLnJD\",\"imei\":\"123456\"}&appid=axp.ios&timestamp=20160602163929123&format=json";
						out.write(param.getBytes());
						
						InputStream in = con.getInputStream();
						byte [] buffer = new byte[1024];
						int count = -1;
						while((count = in.read(buffer))!=-1){
							System.out.print(new String(buffer,0,count));
						}
						System.out.println(temp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				};
			}.start();
		}
	}

}
