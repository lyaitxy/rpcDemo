package com.atguigu.cloud.fault.retry;

import com.atguigu.cloud.model.RpcResponse;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔 - 重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)              // 当出现Exception 异常时重试
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))  // 重试等待策略
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))          // 重试停止策略
                .withRetryListener(new RetryListener() {            // 监听重试，每次重试时，除了再次执行任务外，还能够打印当前的重试次数
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();
        return retryer.call(callable);
    }
}
