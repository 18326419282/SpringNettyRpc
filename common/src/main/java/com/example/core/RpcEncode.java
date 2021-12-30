package com.example.core;

import com.example.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@AllArgsConstructor
public class RpcEncode extends MessageToByteEncoder {
    private Class<?> genericClass;
    private Serializer serializer;

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            try {
                byte[] data = serializer.serialize(msg);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex) {
                log.error("Encode error: " + ex.toString());
            }
        }

    }
}
