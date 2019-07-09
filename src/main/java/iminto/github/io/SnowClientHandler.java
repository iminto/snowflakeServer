package iminto.github.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class SnowClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println(buf.readLong());
//        String str;
//        if(buf.hasArray()) { // 处理堆缓冲区
//            str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
//        } else { // 处理直接缓冲区以及复合缓冲区
//            byte[] bytes = new byte[buf.readableBytes()];
//            buf.getBytes(buf.readerIndex(), bytes);
//            str = new String(bytes, 0, buf.readableBytes());
//        }
//        System.out.println(str);
        ReferenceCountUtil.release(msg);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("6,7".getBytes()));
    }
}
