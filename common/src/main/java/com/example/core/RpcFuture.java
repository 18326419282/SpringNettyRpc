package com.example.core;

import lombok.Data;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@Data
public class RpcFuture  implements Future<Object> {

    private RpcRequest rpcRequest;
    private RpcResponse rpcResponse;
    private Sync sync = new Sync();
    public RpcFuture(RpcRequest rpcRequest) {
        this.rpcRequest = rpcRequest;
    }


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }


    public void done(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        sync.release(1);
    }


    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1);
        if (this.rpcResponse != null) {
            return this.rpcResponse.getResult();
        } else {
            return null;
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout));
        if (success) {
            if (this.rpcResponse != null) {
                return this.rpcResponse.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.rpcRequest.getRequestId()

                    + ". Request class name: " + this.rpcRequest.getClassName()
                    + ". Request method: " + this.rpcRequest.getMethodName());
        }
    }


    static class Sync extends AbstractQueuedSynchronizer{
        private static final long serialVersionUID = 1L;

        //future status
        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
             return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }


        protected boolean isDone() {
            return getState() == done;
        }
    }
}
