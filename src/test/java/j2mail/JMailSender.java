package j2mail;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JMailSender {
	public static void main(String[] args) throws Exception {
		Properties pro = new Properties();
		pro.put("mail.smtp.host", "smtp.qq.com");
		pro.put("mail.smtp.port", "25");
		pro.put("mail.smtp.auth", "true");
		pro.put("mail.transport.protocol", "smtp");  //如果是Transport.send(mailMessage); 则不用配置这个,会自动识别,并且不需要显示的获取Transport 和connect
		Session session = Session.getDefaultInstance(pro, new PrivateAuthenticator());
		//手动获取并且connect
		Transport transport = session.getTransport();
		transport.connect();
		// 根据session创建一个邮件消息
		Message mailMessage = new MimeMessage(session);
		// 创建邮件发送者地址
		Address from = new InternetAddress("475172450@qq.com");
		// 设置邮件消息的发送者
		mailMessage.setFrom(from);
		// 创建邮件的接收者地址，并设置到邮件消息中
		Address to = new InternetAddress("475172450@qq.com");
		mailMessage.setRecipient(Message.RecipientType.TO, to);
		// 设置邮件消息的主题
		mailMessage.setSubject("WJ-Test");
		// 设置邮件消息发送的时间
		mailMessage.setSentDate(new Date());
		// 设置邮件消息的主要内容
		mailMessage.setText("WJ great");
		// 发送邮件
//		Transport.send(mailMessage);
		transport.sendMessage(mailMessage, new Address[]{to});
	}

}

class PrivateAuthenticator extends Authenticator {
	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication("475172450", "wangjian050720");
	}
}





