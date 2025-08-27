package com.mdaesthetics.viral.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests metrics behavior of EmailDispatcherService in simulation mode (no actual Gmail call).
 */
class EmailDispatcherServiceMetricsTest {

    private MeterRegistry registry;
    private EmailDispatcherService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new EmailDispatcherService(registry);
        // Inject required config fields via reflection
        ReflectionTestUtils.setField(service, "recipients", "test@example.com");
        ReflectionTestUtils.setField(service, "enableSend", false); // force simulation
    }

    @Test
    @DisplayName("Simulation increments latency but not sent/error counters")
    void simulationMetrics() {
        double beforeSent = registry.counter("email.sent.count").count();
        double beforeErr = registry.counter("email.send.error").count();
        double beforeLatency = registry.timer("email.send.latency").count();

        service.sendDigest("Subject", "<html>Body</html>", false); // enableSend=false triggers simulation path

    double afterSent = registry.find("email.sent.count").counter().count();
    double afterErr = registry.find("email.send.error").counter().count();
    double afterLatency = registry.find("email.send.latency").timer().count();
    assertThat(afterSent).isEqualTo(beforeSent);
    assertThat(afterErr).isEqualTo(beforeErr);
    assertThat(afterLatency).isEqualTo(beforeLatency + 1.0);
    }
}
