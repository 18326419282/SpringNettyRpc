package com.example.core.client;

import com.example.core.RpcFuture;
import com.example.core.RpcProtocol;
import com.example.core.RpcRequest;
import com.example.core.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;

/**
 * RPC Handler（RPC request processor）
 *
 * @author luxiaoxun
 */
@Slf4j
@Data
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private RpcProtocol rpcProtocol;
    private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();
    private volatile Channel channel;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getRequestId();
        log.debug("Receive response: " + requestId);
        RpcFuture rpcFuture = pendingRPC.get(requestId);
        if (rpcFuture != null) {
            pendingRPC.remove(requestId);
            rpcFuture.done(rpcResponse);
        } else {
            log.warn("Can not get pending response for request id: " + requestId);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            log.warn("Channel idle in last {} seconds, close it", 90);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    public RpcFuture sendRequest(RpcRequest request){
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRPC.put(request.getRequestId(),rpcFuture);
        try {
            ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return rpcFuture;

    }
}
