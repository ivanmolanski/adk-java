package com.mdaesthetics.viral.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {
    @Bean
    public MeterRegistry meterRegistry() {
        MeterRegistry registry = mock(MeterRegistry.class);

        when(registry.counter("api.trends.list.count")).thenReturn(mock(Counter.class));
        when(registry.counter("api.drafts.list.count")).thenReturn(mock(Counter.class));
        when(registry.counter("api.trends.detail.count")).thenReturn(mock(Counter.class));
        when(registry.counter("api.drafts.detail.count")).thenReturn(mock(Counter.class));

        return registry;
    }
}
