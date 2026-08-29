package com.codingshuttle.linkedin.connection_service.kafka.consumers;

import com.codingshuttle.linkedin.connection_service.constants.AppConstants;
import com.codingshuttle.linkedin.connection_service.entities.Person;
import com.codingshuttle.linkedin.user_service.kafka.events.UserCreatedEvent;
import com.codingshuttle.linkedin.connection_service.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCreatedKafkaConsumer {
    private final PersonRepository personRepository;
    @KafkaListener(topics = AppConstants.USER_CREATED_TOPIC)
    public void handleUserCreated(UserCreatedEvent userCreatedEvent) {
        log.info("UserCreatedKafkaListener: {}", userCreatedEvent);
        Person savedPerson = personRepository.createNewUser(userCreatedEvent.getName(),
                userCreatedEvent.getEmail(), userCreatedEvent.getUserId());
        log.info("User created successfully in neo4j db: {}", savedPerson);
    }
}
