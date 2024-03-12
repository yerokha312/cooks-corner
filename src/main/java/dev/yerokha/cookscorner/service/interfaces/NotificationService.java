package dev.yerokha.cookscorner.service.interfaces;

public interface NotificationService {

    void send(String to, String subject, String body);
}
