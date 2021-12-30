package com.example.core;

import com.example.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
public class RpcDecode extends ByteToMessageDecoder {

    private Class<?> genericClass;
    private Serializer serializer;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if(in.readableBytes()<4){
            return;
        }
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] dataBytes = new byte[dataLength];
        in.readBytes(dataBytes);
        Object obj = null;
        try {
            obj = serializer.deserialize(dataBytes, genericClass);
            out.add(obj);
        } catch (Exception ex) {
            log.error("Decode error: " + ex.toString());
        }
    }
}
