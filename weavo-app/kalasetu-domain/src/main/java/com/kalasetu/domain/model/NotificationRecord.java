package com.kalasetu.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificationRecord {
    private UUID id;
    private UUID orderId;
    private UUID recipientUserId;
    private String channel;
    private String message;
    private String status;
    private String workerThreadName;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(UUID recipientUserId) { this.recipientUserId = recipientUserId; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWorkerThreadName() { return workerThreadName; }
    public void setWorkerThreadName(String workerThreadName) { this.workerThreadName = workerThreadName; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
