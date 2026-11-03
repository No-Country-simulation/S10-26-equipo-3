package com.nautiflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class RegistroLancheroExecutorConfig {
    @Bean("registroLancheroUploadExecutor")
    public ThreadPoolTaskExecutor registroLancheroUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(8);
        executor.setThreadNamePrefix("registro-upload-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setRejectedExecutionHandler(this::esperarEspacioEnCola);
        return executor;
    }

    private void esperarEspacioEnCola(Runnable tarea, ThreadPoolExecutor executor) {
        try {
            while (!executor.isShutdown()) {
                if (executor.getQueue().offer(tarea, 100, TimeUnit.MILLISECONDS)) {
                    if (!executor.isShutdown() || !executor.remove(tarea)) {
                        return;
                    }
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Interrumpido mientras se esperaba espacio para una subida.", exception);
        }
        throw new RejectedExecutionException("El executor de subidas está detenido.");
    }
}
