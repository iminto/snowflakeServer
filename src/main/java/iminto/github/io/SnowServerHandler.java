package iminto.github.io;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

@ChannelHandler.Sharable //标识一个 ChannelHandler可以被多个Channel安全地共享
public class SnowServerHandler extends ChannelInboundHandlerAdapter {
    private final long paramLengthError = -1l;//参数长度不符合
    private final long paramRequireError = -2l;//参数数量不符合
    private final long workIdError = -3l;//worker Id can't be greater than 31 or less than 0
    private final long dataCenterIdError = -4l;//dataCenter Id can't be greater than 31 or less than 0

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SnowServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buffer = (ByteBuf) msg;
        //将消息记录到控制台
        char[] chars = new char[10];
        int length = buffer.readableBytes();
        if (length >= 10 || length <= 1) {
            logger.error("参数长度不正确");
            ctx.writeAndFlush(Unpooled.copyLong(paramLengthError)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        int hadComma = 0;//逗号的位置
        int enter = 0;//回车的位置
        for (int i = 0; i < buffer.readableBytes(); i++) {
            byte b = buffer.getByte(i);
            if (b == 44) {
                hadComma = i;
            }
            if (b == 13) {
                enter = i;
            }
            char ch = (char) b;
            chars[i] = ch;
        }
//        logger.info("接受到:"+new String(chars));
        if (hadComma <= 0) {
            logger.error("缺少一个参数");
            ctx.writeAndFlush(Unpooled.copyLong(paramRequireError)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        String work = new String(chars, 0, hadComma);
        String data;
        if (enter > 0) {//兼容telnet测试，telnet会附加回车换行符号
            data = new String(chars, hadComma + 1, (enter - hadComma - 1));
        } else {
            data = new String(chars, hadComma + 1, length - (hadComma + 1));
        }
        Long workId = Long.parseLong(work);
        Long dataCenterId = Long.parseLong(data);
        if (workId > SnowFlakeIdWorker.maxWorkerId || workId < 0) {
            logger.error("work id 大小不正确");
            ctx.writeAndFlush(Unpooled.copyLong(workIdError)).addListener(ChannelFutureListener.CLOSE);
            return;
        } else if (dataCenterId < 0 || dataCenterId > SnowFlakeIdWorker.maxDataCenterId) {
            logger.error("dataCenterId 大小不正确");
            ctx.writeAndFlush(Unpooled.copyLong(dataCenterIdError)).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        long snowId = 0L;
        SnowFlakeIdWorker worker = new SnowFlakeIdWorker(workId, dataCenterId, 1L);
        snowId = worker.nextId();
//        logger.info("workId={},and dataCenterId={}", workId, dataCenterId);
        logger.info("snowId=" + snowId);
        //将接受到消息回写给发送者
//        ctx.write(Unpooled.copiedBuffer(snowId.toString(), Charset.defaultCharset()));
        ctx.write(Unpooled.copyLong(snowId));

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //将未消息冲刷到远程节点，并且关闭该 Channel
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                .addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //打印异常栈跟踪
        cause.printStackTrace();
        //关闭该Channel
        ctx.close();
    }
}
