package com.kalasetu.concurrency;

import com.kalasetu.domain.model.NotificationRecord;
import com.kalasetu.persistence.MarketplaceRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background daemon thread consuming a BlockingQueue to persist simulated buyer/artisan alerts.
 */
public class BackgroundNotificationWorker implements AutoCloseable {
    public record NotificationTask(java.util.UUID orderId, java.util.UUID recipientUserId,
                                   String channel, String message) {}

    private final MarketplaceRepository repository;
    private final BlockingQueue<NotificationTask> queue = new LinkedBlockingQueue<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final Thread workerThread;

    public BackgroundNotificationWorker(MarketplaceRepository repository) {
        this.repository = repository;
        this.workerThread = new Thread(this::runLoop, "weavo-notif-worker-1");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    public void enqueue(NotificationTask task) {
        if (running.get()) {
            queue.offer(task);
        }
    }

    public int getPendingCount() {
        return queue.size();
    }

    public int getProcessedCount() {
        return processedCount.get();
    }

    public String getWorkerThreadName() {
        return workerThread.getName();
    }

    private void runLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                NotificationTask task = queue.poll(300, TimeUnit.MILLISECONDS);
                if (task == null) continue;
                NotificationRecord rec = new NotificationRecord();
                rec.setOrderId(task.orderId());
                rec.setRecipientUserId(task.recipientUserId());
                rec.setChannel(task.channel());
                rec.setMessage(task.message());
                rec.setStatus("SENT_SIMULATED");
                rec.setWorkerThreadName(Thread.currentThread().getName());
                repository.saveNotification(rec);
                processedCount.incrementAndGet();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ignored) {
                // Keep background worker resilient
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        workerThread.interrupt();
    }
}
