package client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class HelloWorldClient {

      public static void main(String args[]) {
           // Client服务启动器 3.x的ClientBootstrap 改为Bootstrap，且构造函数变化很大，这里用无参构造。
          Bootstrap bootstrap = new Bootstrap();
           // 指定channel类型
          bootstrap.channel(NioSocketChannel. class );
           // 指定Handler
          bootstrap.handler( new HelloClientHandler());
           // 指定EventLoopGroup
          bootstrap.group( new NioEventLoopGroup());
           // 连接到本地的8000端口的服务端
          bootstrap.connect( new InetSocketAddress("127.0.0.1" , 8000));
     }

      private static class HelloClientHandler extends
              ChannelInboundHandlerAdapter {

           /**
           * 当绑定到服务端的时候触发，打印"Hello world, I'm client."
           *
           * @alia OneCoder
           * @author lihzh
           * @date 2013年11月16日 上午12:50:47
           */
           @Override
           public void channelActive(ChannelHandlerContext ctx) throws Exception {
              System. out .println("Hello world, I'm client.");
          }
     }
}