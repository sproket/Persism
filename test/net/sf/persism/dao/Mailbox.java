package net.sf.persism.dao;

import java.time.LocalDateTime;

// test for es name guess coverage
public final class Mailbox {
    private int id;
    private int userId;
    private String subject;
    private String body;
    private LocalDateTime receivedDate;
    private LocalDateTime readDate;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDateTime getReadDate() {
        return readDate;
    }

    public void setReadDate(LocalDateTime readDate) {
        this.readDate = readDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Mailbox{");
        sb.append("id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", subject='").append(subject).append('\'');
        sb.append(", body='").append(body).append('\'');
        sb.append(", receivedDate=").append(receivedDate);
        sb.append(", readDate=").append(readDate);
        sb.append('}');
        return sb.toString();
    }
}
