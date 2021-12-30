package com.example.core.service;

import com.example.core.RpcRequest;
import com.example.core.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * RPC Handler（RPC request processor）
 *
 * @author luxiaoxun
 */
@Slf4j
@AllArgsConstructor
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {


    private Map<String, Object> serviceMap = new HashMap<>();

    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        // filter beat ping
        log.info("rpcRequest"+rpcRequest);
        if ("BEAT_PING_PONG".equalsIgnoreCase(rpcRequest.getRequestId())) {
            log.info("Server read heartbeat ping");
            return;
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                log.info("Receive request " + rpcRequest.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(rpcRequest.getRequestId());
                try{
                    Object result = handle(rpcRequest);
                    response.setResult(result);
                }catch (Throwable throwable){
                    response.setError(throwable.toString());
                    log.error("RPC Server handle request error", throwable);
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        log.info("Send response for request " + rpcRequest.getRequestId());
                    }
                });
            }
        });


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

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = serviceMap.get(className);
        if(serviceBean == null){
            log.error("Can not find service implement with interface name: {} and version: {}", className);
            return null;
        }
        FastClass serviceFastClass = FastClass.create(serviceBean.getClass());
        FastMethod method = serviceFastClass.getMethod(request.getMethodName(),request.getParameterTypes());
        Object o = method.invoke(serviceBean,request.getParameters());
        return o;
    }
}
