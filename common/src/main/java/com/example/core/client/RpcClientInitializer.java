package com.example.core.client;

import com.example.core.RpcDecode;
import com.example.core.RpcEncode;
import com.example.core.RpcRequest;
import com.example.core.RpcResponse;
import com.example.serializer.Serializer;
import com.example.serializer.kryo.KryoSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = KryoSerializer.class.newInstance();
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new IdleStateHandler(0, 0, 90, TimeUnit.SECONDS));
        channelPipeline.addLast(new RpcEncode(RpcRequest.class,serializer));
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        channelPipeline.addLast(new RpcDecode(RpcResponse.class,serializer));
        channelPipeline.addLast(new RpcClientHandler());
    }
}
