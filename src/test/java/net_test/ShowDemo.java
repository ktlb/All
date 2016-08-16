package net_test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * 做一个小例子. 为后面的SSL 铺路 这里的协议通信很简单. 遇到 连续的 /r/n 则认为读取到了尽头.
 * 
 * @author gongxu
 * 
 */
public class ShowDemo {
	static String pukHexStr = null;

	static String prHexStr = null;

	static byte aessecretKeys[] = null;
	static {
		// KeyPairGenerator 负责密钥对生成器的生成. 通过密钥对生成器 KeyPair 可以生成 公钥 和 私钥
		KeyPairGenerator keyPairGenerator_https = null;
		try {
			keyPairGenerator_https = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}
		// 指定生成公钥 私钥长度. 1024
		keyPairGenerator_https.initialize(1024);
		KeyPair keyPair = keyPairGenerator_https.generateKeyPair();

		// http 公钥 和 私钥 这里 其实指的是 RSAPublicKey 和 RSAPrivateKey
		PublicKey publicKey_https = keyPair.getPublic();

		PrivateKey privateKey_https = keyPair.getPrivate();

		pukHexStr = HexBin.encode(publicKey_https.getEncoded());

		prHexStr = HexBin.encode(privateKey_https.getEncoded());

		KeyGenerator kg = null;
		try {
			kg = KeyGenerator.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {

			e.printStackTrace();
		}

		aessecretKeys = kg.generateKey().getEncoded();
	}

	// 初始化RSA的公钥和私钥
	static Thread server = new Thread() {

		@Override
		public void run() {
			try {

				Selector selector = Selector.open();
				ServerSocketChannel server = ServerSocketChannel.open();
				Set<SelectionKey> dels = new HashSet<SelectionKey>();
				server.configureBlocking(false);
				// Set<E>
				server.register(selector, SelectionKey.OP_ACCEPT);
				server.socket().bind(new InetSocketAddress("127.0.0.1", 8083));
				while (true) {

					if (selector.select() > 0) {
						Set<SelectionKey> selectedKeys = selector
								.selectedKeys();
						for (SelectionKey st : selectedKeys) {

							try {
								// 可以做个策略 . 执行3次失败才移除. 否则保留.
								dels.add(st);
								if (st.isAcceptable()) {
									ServerSocketChannel selectableChannel = (ServerSocketChannel) st
											.channel();
									// 获取client one
									SocketChannel currentclient = selectableChannel
											.accept();

									// server 不存活??
									selectableChannel.close();

									currentclient.configureBlocking(false);

									currentclient.register(selector,
											SelectionKey.OP_READ);
									continue;

								}
								// 小demo 不考虑 是否可写的问题. 以免CUP过高.
								if (st.isReadable()) {
									SocketChannel socketChannel = (SocketChannel) st
											.channel();
									String message = getLine(socketChannel);

									if (message.equals("请求RSA私钥")) {
										// 发送RSA公钥
										socketChannel
												.write(ByteBuffer
														.wrap(("RSA 私钥: "
																+ prHexStr + "\r\n")
																.getBytes()));
										System.out.println("send by client : "
												+ message);
									}
									if (message.equals("请求AES密钥密文")) {

										// 发送AES密钥密文 通过RAS 公钥加密

										X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
												HexBin.decode(pukHexStr));
										KeyFactory keFactory = KeyFactory
												.getInstance("RSA");
										PublicKey publicKey = keFactory
												.generatePublic(x509EncodedKeySpec);
										Cipher cipher = Cipher
												.getInstance("RSA");
										cipher.init(Cipher.ENCRYPT_MODE,
												publicKey);

										// RSA 加密密钥
										byte[] data = cipher
												.doFinal(aessecretKeys);

										// System.out.println(HexBin.encode(data));
										socketChannel
												.write(ByteBuffer
														.wrap(("AES密钥密文 16进制: "
																+ HexBin
																		.encode(data) + "\r\n")
																.getBytes()));
										System.out.println("send by client : "
												+ message);
									}

									// 普通数据. 这里也假装解析吧.
									if (message.matches("data:.*")) {
										// 拆分签名.
										byte[] finaldata = HexBin
												.decode(message.split(":")[1]
														.trim());

										// 取出128 字节 密钥128 签名长度为128
										byte[] sign = new byte[128];
										System.arraycopy(finaldata, 0, sign, 0,
												128);

										// 取出data
										byte[] data = new byte[finaldata.length - 128];
										System.arraycopy(finaldata, 128, data,
												0, data.length);

										// 对数据解密 AES.

										Cipher cipher = Cipher
												.getInstance("AES/ECB/PKCS5Padding");
										// 解密
										cipher.init(Cipher.DECRYPT_MODE,
												new SecretKeySpec(
														aessecretKeys, "AES"));

										// 解密数据
										String datastr = new String(cipher
												.doFinal(data));

										// 签名验证.
										Signature signature = Signature
												.getInstance("MD5WITHRSA");

										X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
												HexBin.decode(pukHexStr));
										KeyFactory keFactory = KeyFactory
												.getInstance("RSA");
										PublicKey publicKey = keFactory
												.generatePublic(x509EncodedKeySpec);

										// 校验签名数据. 如果一致.其实就表明. 签名的数据是合法的.
										signature.initVerify(publicKey);

										// 激活校验数据.即要检查的数据.
										signature.update(datastr.getBytes());

										if (signature.verify(sign)) {
											System.out
													.println("send by client : "
															+ datastr);
										} else {
											System.out.println("数据被篡改");
										}

									}

									continue;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						selectedKeys.removeAll(dels);
						dels.clear();
					}
				}

			} catch (Exception e) {

				e.printStackTrace();
			}
		}

	};

	// 获取RSA的私钥.
	static Thread client = new Thread() {
		@Override
		public void run() {
			try {
				byte[] privatersakey = null;
				byte[] secretKey = null;
				Selector selector = Selector.open();
				SocketChannel client = SocketChannel.open();
				client.configureBlocking(false);
				Set<SelectionKey> dels = new HashSet<SelectionKey>();
				client.register(selector, SelectionKey.OP_CONNECT);
				// client.socket().connect(new InetSocketAddress("127.0.0.1",
				// 8083), 1000);
				client.connect(new InetSocketAddress("127.0.0.1", 8083));
				if (selector.select(1000) > 0) {
					SelectionKey sk = (SelectionKey) selector.selectedKeys()
							.iterator().next();
					if (((SocketChannel) sk.channel()).finishConnect()) {

						// 像SERVER 申请公钥
						// 这里不会阻塞
						sk.interestOps(SelectionKey.OP_READ);

						// 每次通信采取\r\n 为命令结束
						((SocketChannel) sk.channel()).write(ByteBuffer
								.wrap("请求RSA私钥\r\n".getBytes("GBK")));

						while (true) {
							// 只有一个.
							if (selector.select() > 0) {
								Set<SelectionKey> selectedKeys = selector
										.selectedKeys();
								for (SelectionKey st : selectedKeys) {
									dels.add(st);

									// 小demo 不考虑 是否可写的问题. 以免CUP过高.
									if (st.isReadable()) {
										SocketChannel socketChannel = (SocketChannel) st
												.channel();
										String message = getLine(socketChannel);
										System.out.println("send by server : "
												+ message);
										if (message.matches("RSA 私钥:.*")) {
											// 获取并保存私钥信息.
											privatersakey = HexBin
													.decode(message.split(":")[1]
															.trim());

											((SocketChannel) sk.channel())
													.write(ByteBuffer
															.wrap("请求AES密钥密文\r\n"
																	.getBytes("GBK")));

										}

										if (message.matches("AES密钥密文 16进制: .*")) {
											// 获取AES的密钥密文. 进行RSA解密 并且 保存
											PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
													privatersakey);

											Cipher cipher = Cipher
													.getInstance("RSA");
											cipher
													.init(
															Cipher.DECRYPT_MODE,
															KeyFactory
																	.getInstance(
																			"RSA")
																	.generatePrivate(
																			pkcs8KeySpec));

											secretKey = cipher
													.doFinal(HexBin
															.decode(message
																	.split(":")[1]
																	.trim()));

											Random rd = new SecureRandom();
											// 开始随机发送数据了. 这里模拟get请求吧.
											while (true) {

												String data = "当前时间: "
														+ System
																.currentTimeMillis();

												// 先数据签名. 然后加密数据.
												// 对方先分离签名 . 然后解密数据. 然后重新签名
												// 比较签名值. (注意这里是签名 非简单消息摘要)
												// 签名算法的长度固定. 就是密钥的长度 即 1024/8.
												// O(∩_∩)O
												// 还原私钥

												// 初始化签名信息.
												Signature signature = Signature
														.getInstance("MD5WITHRSA");
												signature.initSign(KeyFactory
														.getInstance("RSA")
														.generatePrivate(
																pkcs8KeySpec));
												// 签名.
												signature.update(data
														.getBytes());
												// 获取签名后的信息.
												byte[] sign = signature.sign();

												// 对数据AES 加密.
												cipher = Cipher
														.getInstance("AES/ECB/PKCS5Padding");
												// 加密
												cipher.init(
														Cipher.ENCRYPT_MODE,
														new SecretKeySpec(
																secretKey,
																"AES"));

												// 执行操作. 待加密数据 组装数据
												String finldata = "data: "
														+ HexBin.encode(sign)
														+ HexBin
																.encode(cipher
																		.doFinal(data
																				.getBytes()));

												// 发送数据
												((SocketChannel) sk.channel())
														.write(ByteBuffer
																.wrap((finldata + "\r\n")
																		.getBytes("GBK")));

												Thread.sleep(1000 * rd
														.nextInt(5));

											}
										}

									}

								}
								selectedKeys.removeAll(dels);
								dels.clear();
							}

						}
					}
					// 连接失败
				}
				// 连接超时

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	static String getLine(SocketChannel socketChannel) throws IOException {
		ByteBuffer bf = ByteBuffer.allocate(100);
		bf.limit(1);
		bf.position(0);

		int currentIndex = 1;
		while (true) {
			currentIndex++;
			// 出现错误即终止
			socketChannel.read(bf);

			if (bf.array()[bf.position() - 1] == '\n') {
				// 判断上个字节 是否正确.
				if (bf.array()[bf.position() - 2] == '\r') {
					return Charset.forName("gbk").decode(
							(ByteBuffer) bf.position(0).limit(bf.limit() - 2))
							.toString();
				}
			}

			if (bf.capacity() < currentIndex) {
				byte olddata[] = bf.array();
				// 开始添加
				bf = ByteBuffer.allocate(bf.capacity() + 100);
				bf.put(olddata);
			}
			bf.limit(currentIndex);
		}

	}

	public static void main(String[] args) {
		server.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		client.start();
	}
}
