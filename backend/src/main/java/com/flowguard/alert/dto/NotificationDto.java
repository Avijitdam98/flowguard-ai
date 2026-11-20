package com.flowguard.alert.dto;

import com.flowguard.alert.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private UUID id;
    private String recipientEmail;
    private String channel;
    private String title;
    private String message;
    private String status;
    private Instant sentAt;

    public static NotificationDto fromEntity(Notification notification) {
        if (notification == null) return null;
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientEmail(notification.getRecipientEmail())
                .channel(notification.getChannel())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .build();
    }
}

