package com.github.jvm.io.protocol.c2d.codc.protostuff;

import com.github.jvm.io.protocol.c2d.message.C2DHeader;
import com.github.jvm.io.protocol.c2d.message.C2DMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 *  基于Protostuff的反序列化必须指定明确的反序列化对象才能正确反序列化成功
 *  对于指定为{@code Object.class}是不能被正确反序列化的
 *  所以应用场景的选定很重要
 *
 *  @author : Crab2Died
 * 	2017/12/15  10:21:13
 */
@Deprecated
public class C2DProtostuffMsgEncoder extends MessageToMessageEncoder<C2DMessage> {

    private static final Logger logger = LoggerFactory.getLogger(C2DProtostuffMsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, C2DMessage msg, List<Object> out)
            throws Exception {
        if (null == msg || null == msg.getHeader()) {
            logger.warn("The encoder massage is null");
            throw new Exception("The encoder massage is null");
        }
        ByteBuf buf = Unpooled.buffer();
        C2DHeader header = msg.getHeader();
        buf.writeInt(header.getMagic());
        buf.writeInt(header.getLength());
        buf.writeLong(header.getSerial());
        buf.writeLong(header.getSessionId());
        buf.writeByte(header.getSignal());
        buf.writeByte(header.getPriority());
        buf.writeInt(header.getAttachment() == null ? 0 : header.getAttachment().size());

        for (Map.Entry<String, Object> param : header.getAttachment().entrySet()) {
            byte[] keyArr = param.getKey().getBytes(CharsetUtil.UTF_8);
            buf.writeInt(keyArr.length);
            buf.writeBytes(keyArr);
            byte[] valArr = ProtostuffCodec.encode(param.getValue());
            buf.writeInt(valArr.length);
            buf.writeBytes(valArr);
        }
        if (null != msg.getBody()) {
            byte[] bodyArr = ProtostuffCodec.encode(msg.getBody());
            buf.writeInt(bodyArr.length);
            buf.writeBytes(bodyArr);
        } else
            buf.writeInt(0);
        buf.setInt(4, buf.readableBytes());
        out.add(buf);
    }

}
