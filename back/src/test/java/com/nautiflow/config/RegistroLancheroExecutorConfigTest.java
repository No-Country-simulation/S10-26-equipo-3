package com.nautiflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistroLancheroExecutorConfigTest {
    @Test
    void limitaLasSubidasConcurrentesYLaColaCompartida() {
        ThreadPoolTaskExecutor executor = new RegistroLancheroExecutorConfig()
                .registroLancheroUploadExecutor();
        executor.initialize();
        try {
            assertEquals(2, executor.getThreadPoolExecutor().getCorePoolSize());
            assertEquals(2, executor.getThreadPoolExecutor().getMaximumPoolSize());
            assertEquals(8, executor.getThreadPoolExecutor().getQueue().remainingCapacity());
        } finally {
            executor.shutdown();
        }
    }
}
