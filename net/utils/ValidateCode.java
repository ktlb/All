package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;

public class ValidateCode {
	/*
	 * 验证码的宽度,高度,验证码的个数和内容,干扰的线和黑点,字体,由于尺寸有限,最多清晰的支持6位的验证码,可以通过调整image_width,来或许更多位的验证码
	 */
	private int image_width = 65;
	private int image_height = 20;
	private int code_num = 4;
	private String code_chars = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private int line_num = 8;
	private int black_point = 20;
	private Font font = new Font("CENTER_BASELINE", Font.CENTER_BASELINE, 18);
	private Random random = new Random();
	private StringBuffer sb = new StringBuffer();
	
	/*
	 * 绘制验证码的主要方法
	 */
	public String sendValidateCode(OutputStream out){
		BufferedImage image = new BufferedImage(image_width, image_height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, image_width, image_height);
		g.setFont(font);
		drawChars(g, code_num);
		drawPoints(g, black_point);
		drawLines(g, line_num);
		try {
			ImageIO.write(image, "jpg",out);
//			ImageIO.write(image, "jpg", new FileOutputStream("D://vc.jpg"));
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally{
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	/*
	 * 绘制验证码
	 */
	private void drawChars(Graphics g,int num){
		for(int i = 1;i<=num;i++){
			int begin = random.nextInt(code_chars.length()-1);
			String str = code_chars.substring(begin,begin+1);
			g.setColor(new Color(random.nextInt(120), random.nextInt(120), random.nextInt(120)));
			sb.append(str);
			g.drawString(str, image_width/num*i-15,image_height-5);
		}
	}

	/*
	 * 绘制干扰线
	 */
	private void drawLines(Graphics g,int num){
		for(int i =0;i<num;i++){
			g.drawLine(random.nextInt(image_width), random.nextInt(image_height), random.nextInt(image_width)+10, random.nextInt(image_height)+10);
		}
	}
	/*
	 * 绘制干扰点
	 */
	private void drawPoints(Graphics g,int num){
		g.setColor(Color.black);
		for(int i = 0;i<num;i++){
			g.drawOval(random.nextInt(image_width), random.nextInt(image_height), 1, 1);
		}
	}
	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public int getImage_width() {
		return image_width;
	}

	public void setImage_width(int image_width) {
		this.image_width = image_width;
	}

	public int getImage_height() {
		return image_height;
	}

	public void setImage_height(int image_height) {
		this.image_height = image_height;
	}

	public int getCode_num() {
		return code_num;
	}

	public void setCode_num(int code_num) {
		this.code_num = code_num;
	}

	public String getCode_chars() {
		return code_chars;
	}

	public void setCode_chars(String code_chars) {
		this.code_chars = code_chars;
	}

	public int getLine_num() {
		return line_num;
	}

	public void setLine_num(int line_num) {
		this.line_num = line_num;
	}

	public int getBlack_point() {
		return black_point;
	}

	public void setBlack_point(int black_point) {
		this.black_point = black_point;
	}
	
}
