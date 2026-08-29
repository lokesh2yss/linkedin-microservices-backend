package com.codingshuttle.linkedin.user_service.kafka.events;

import lombok.*;

@Data
@Builder
public class UserCreatedEvent {
    private Long userId;
    private String name;
    private String email;
}
