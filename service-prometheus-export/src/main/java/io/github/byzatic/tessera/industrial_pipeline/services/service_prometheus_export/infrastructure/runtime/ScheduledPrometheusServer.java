package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.infrastructure.runtime;

import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.commons.schedulers.cron.CronScheduler;
import io.github.byzatic.commons.schedulers.cron.CronSchedulerInterface;
import io.github.byzatic.commons.schedulers.cron.CronTask;
import io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.application.usecase.SynchronizeMetricsUseCase;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class ScheduledPrometheusServer {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledPrometheusServer.class);

    private final SynchronizeMetricsUseCase synchronizeMetrics;
    private final String serverAddress;
    private final int serverPort;
    private final String updateCron;
    private final MCg3ServiceApiInterface serviceApi;
    private final CronSchedulerInterface scheduler;
    private final Duration stopGrace = Duration.ofSeconds(5);
    private final Object monitor = new Object();
    private final AtomicReference<Throwable> fatalError = new AtomicReference<>();
    private volatile UUID jobId;
    private volatile HTTPServer server;
    private volatile boolean stopRequested;

    public ScheduledPrometheusServer(
            SynchronizeMetricsUseCase synchronizeMetrics,
            String serverAddress,
            int serverPort,
            String updateCron,
            MCg3ServiceApiInterface serviceApi
    ) {
        this.synchronizeMetrics = Objects.requireNonNull(synchronizeMetrics, "synchronizeMetrics must not be null");
        this.serverAddress = Objects.requireNonNull(serverAddress, "serverAddress must not be null");
        this.serverPort = serverPort;
        this.updateCron = Objects.requireNonNull(updateCron, "updateCron must not be null");
        this.serviceApi = Objects.requireNonNull(serviceApi, "serviceApi must not be null");
        this.scheduler = new CronScheduler.Builder().build();
    }

    public void run() throws OperationIncompleteException {
        try {
            server = HTTPServer.builder().hostname(serverAddress).port(serverPort).buildAndStart();
            scheduler.addListener(new io.github.byzatic.commons.schedulers.cron.JobEventListener() {
                @Override
                public void onError(UUID id, Throwable error) {
                    fail(error);
                }

                @Override
                public void onTimeout(UUID id) {
                    fail(new IllegalStateException("Metrics synchronization timed out: " + id));
                }

                @Override
                public void onCancelled(UUID id) {
                }

                @Override
                public void onComplete(UUID id) {
                }
            });
            jobId = scheduler.addJob(updateCron, synchronizationTask(), true, true);
            logger.info("Prometheus endpoint started at http://{}:{}/metrics", serverAddress, serverPort);

            synchronized (monitor) {
                while (!stopRequested && fatalError.get() == null) {
                    monitor.wait();
                }
            }
            if (fatalError.get() != null) {
                throw new OperationIncompleteException(fatalError.get());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OperationIncompleteException(e);
        } catch (OperationIncompleteException e) {
            throw e;
        } catch (Exception e) {
            throw new OperationIncompleteException(e);
        } finally {
            closeResources();
        }
    }

    public void terminate() {
        stopRequested = true;
        UUID currentJob = jobId;
        if (currentJob != null) {
            try {
                scheduler.stopJob(currentJob, stopGrace);
            } catch (Exception e) {
                logger.warn("Failed to stop metrics synchronization job", e);
            }
        }
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    private CronTask synchronizationTask() {
        return new CronTask() {
            @Override
            public void run(io.github.byzatic.commons.schedulers.cron.CancellationToken token) throws Exception {
                token.throwIfStopRequested();
                try (AutoCloseable ignored = serviceApi.getExecutionContext().getMdcContext().use()) {
                    synchronizeMetrics.synchronize();
                }
                token.throwIfStopRequested();
            }

            @Override
            public void onStopRequested() {
                logger.debug("Metrics synchronization stop requested");
            }
        };
    }

    private void fail(Throwable error) {
        if (fatalError.compareAndSet(null, error)) {
            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    private void closeResources() {
        UUID currentJob = jobId;
        jobId = null;
        if (currentJob != null) {
            try {
                scheduler.removeJob(currentJob, stopGrace);
            } catch (Exception e) {
                logger.warn("Failed to remove metrics synchronization job", e);
            }
        }
        try {
            scheduler.close();
        } catch (Exception e) {
            logger.warn("Failed to close scheduler", e);
        }
        HTTPServer currentServer = server;
        server = null;
        if (currentServer != null) {
            try {
                currentServer.close();
            } catch (Exception e) {
                logger.warn("Failed to close Prometheus HTTP server", e);
            }
        }
    }
}
