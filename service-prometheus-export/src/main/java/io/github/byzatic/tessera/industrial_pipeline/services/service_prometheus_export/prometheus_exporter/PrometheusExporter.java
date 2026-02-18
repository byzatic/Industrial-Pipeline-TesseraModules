package io.github.byzatic.tessera.industrial_pipeline.services.service_prometheus_export.prometheus_exporter;

import io.github.byzatic.commons.base_exceptions.OperationIncompleteException;
import io.github.byzatic.tessera.service.api_engine.MCg3ServiceApiInterface;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.byzatic.commons.schedulers.cron.CronScheduler;
import io.github.byzatic.commons.schedulers.cron.CronSchedulerInterface;
import io.github.byzatic.commons.schedulers.cron.CronTask;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PrometheusExporter implements PrometheusExporterInterface {
    private static final Logger logger = LoggerFactory.getLogger(PrometheusExporter.class);

    private final MetricsUpdateManagerInterface metricsUpdateManager;
    private final String httpServerAddress;
    private final Integer httpServerPort;
    /** ВАЖНО: cron должен быть в новой семантике (5 или 6 полей; без '?').*/
    private final String cronMetricUpdateString;
    private final MCg3ServiceApiInterface serviceApiInterface;

    /** Планировщик cron (из вашего cron.zip). */
    private final CronSchedulerInterface cronScheduler;
    /** Grace на остановку запущенной задачи. */
    private final Duration stopGrace = Duration.ofSeconds(5);

    /** Текущий jobId cron-задачи обновления метрик. */
    private volatile UUID cronJobId;
    /** HTTP-сервер метрик. */
    private volatile HTTPServer server;

    /** Сигнал штатной остановки (terminate). */
    private final Object monitor = new Object();
    private volatile boolean stopRequested = false;

    /** Фатальная ошибка, поступившая из cron-задачи. */
    private final AtomicReference<Throwable> fatalError = new AtomicReference<>(null);

    public PrometheusExporter(MetricsUpdateManagerInterface metricsUpdateManager,
                              String httpServerAddress,
                              Integer httpServerPort,
                              String cronMetricUpdateString,
                              MCg3ServiceApiInterface serviceApiInterface) {
        this.metricsUpdateManager = metricsUpdateManager;
        this.httpServerAddress = httpServerAddress;
        this.httpServerPort = httpServerPort;
        this.cronMetricUpdateString = cronMetricUpdateString;
        this.serviceApiInterface = serviceApiInterface;
        this.cronScheduler = new CronScheduler.Builder().build();
    }

    @Override
    public void run() throws OperationIncompleteException {
        // listener для фиксации ошибок cron-задачи
        io.github.byzatic.commons.schedulers.cron.JobEventListener cronListener =
                new io.github.byzatic.commons.schedulers.cron.JobEventListener() {
                    private void fail(UUID jobId, String reason, Throwable error) {
                        if (fatalError.compareAndSet(null, (error != null ? error : new RuntimeException(reason)))) {
                            logger.error("PrometheusExporter fatal: {} (jobId={})", reason, jobId, error);
                            // Разбудим основной поток run()
                            synchronized (monitor) { monitor.notifyAll(); }
                        }
                    }
                    @Override public void onError(UUID jobId, Throwable error) { fail(jobId, "Cron job error", error); }
                    @Override public void onTimeout(UUID jobId)               { fail(jobId, "Cron job timeout", null); }
                    @Override public void onCancelled(UUID jobId)             { /* отмена не фатал */ }
                    @Override public void onComplete(UUID jobId)              { /* успех — просто лог */ }
                };

        try {
            // 1) HTTP server
            this.server = HTTPServer.builder().hostname(httpServerAddress).port(httpServerPort).buildAndStart();
            logger.debug("HTTPServer listening on http://{}:{}{}", httpServerAddress, httpServerPort, "/metrics");
            logger.warn("HTTPServer using default location {}", "/metrics");

            // 2) Listener + cron-задача
            cronScheduler.addListener(cronListener);

            CronTask updateMetricsTask = new CronTask() {
                @Override
                public void run(io.github.byzatic.commons.schedulers.cron.CancellationToken token) throws Exception {
                    token.throwIfStopRequested();
                    try (AutoCloseable ignored = serviceApiInterface.getExecutionContext().getMdcContext().use()) {
                        metricsUpdateManager.updateMetrics();
                        logger.debug("Metrics updated successfully");
                    } catch (Exception e) {
                        logger.error("Error updating metrics", e);
                        // выбрасываем — это приведёт к onError у listener'а
                        throw e;
                    } finally {
                        token.throwIfStopRequested();
                    }
                }
                @Override
                public void onStopRequested() {
                    // если появится кооперативная остановка процесса сбора метрик — вызвать её здесь
                    logger.debug("Stop requested for metrics update task");
                }
            };

            // disallowOverlap=true (без наложений), runImmediately=true (первый запуск сразу, чтобы быстро наполнить метрики)
            this.cronJobId = cronScheduler.addJob(cronMetricUpdateString, updateMetricsTask, true, true);
            logger.info("PrometheusExporter cron scheduled: '{}' (jobId={})", cronMetricUpdateString, cronJobId);

            // 3) Блокируемся до фатала или terminate()
            synchronized (monitor) {
                while (!stopRequested && fatalError.get() == null) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        fatalError.compareAndSet(null, ie);
                        break;
                    }
                }
            }

            // Если случился фатал — пробросим как OperationIncompleteException
            if (fatalError.get() != null) {
                throw new OperationIncompleteException(fatalError.get());
            }

        } catch (OperationIncompleteException e) {
            // уже нормализовано — пробрасываем
            throw e;
        } catch (Exception e) {
            throw new OperationIncompleteException(e);
        } finally {
            // 4) Очистка: снять cron-джобу, закрыть планировщик и HTTP-сервер
            try {
                UUID id = this.cronJobId;
                if (id != null) {
                    try {
                        cronScheduler.removeJob(id, stopGrace);
                    } catch (Throwable t) {
                        try { cronScheduler.stopJob(id, stopGrace); } catch (Throwable ignored) {}
                    } finally {
                        this.cronJobId = null;
                    }
                }
            } catch (Throwable ignored) {}

            try { cronScheduler.close(); } catch (Exception ignored) {}

            try {
                HTTPServer s = this.server;
                if (s != null) {
                    try { s.close(); } catch (Exception ignored) {}
                    this.server = null;
                }
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void terminate() {
        // просим корректно завершиться: разбудим run(); запусков новых задач лучше не допускать
        stopRequested = true;

        // Попросим текущую (или будущие) задачи остановиться как можно быстрее
        try {
            UUID id = this.cronJobId;
            if (id != null) {
                try {
                    cronScheduler.stopJob(id, stopGrace);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        synchronized (monitor) { monitor.notifyAll(); }
    }
}