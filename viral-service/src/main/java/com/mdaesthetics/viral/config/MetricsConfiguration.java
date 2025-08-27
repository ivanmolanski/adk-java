package com.mdaesthetics.viral.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Basic metrics configuration. In production replace SimpleMeterRegistry with
 * a vendor-specific registry (Prometheus, Stackdriver via OpenTelemetry, etc.).
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean(MeterRegistry.class)
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
}
