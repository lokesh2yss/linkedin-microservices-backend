package com.codingshuttle.linkedin.connection_service.kafka.events;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AcceptConnectionRequestEvent {
    private Long senderId;
    private Long receiverId;
}
