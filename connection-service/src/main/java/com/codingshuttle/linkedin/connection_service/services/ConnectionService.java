package com.codingshuttle.linkedin.connection_service.services;

import com.codingshuttle.linkedin.connection_service.auth.UserContextHolder;
import com.codingshuttle.linkedin.connection_service.entities.Person;
import com.codingshuttle.linkedin.connection_service.kafka.events.AcceptConnectionRequestEvent;
import com.codingshuttle.linkedin.connection_service.kafka.events.SendConnectionRequestEvent;
import com.codingshuttle.linkedin.connection_service.repositories.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionService {
    private final PersonRepository personRepository;
    private final KafkaTemplate<Long, SendConnectionRequestEvent> sendRequestKafkaTemplate;
    private final KafkaTemplate<Long, AcceptConnectionRequestEvent> acceptRequestKafkaTemplate;

    public List<Person> getFirstDegreeConnections() {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Getting first degree connections for user with id: {}", userId);
        return personRepository.getFirstDegreeConnections(userId);
    }

    public List<Person> getSecondDegreeConnections() {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Getting second degree connections for user with id: {}", userId);
        return personRepository.getSecondDegreeConnections(userId);
    }

    public List<Person> getThirdDegreeConnections() {
        Long userId = UserContextHolder.getCurrentUserId();
        log.info("Getting third degree connections for user with id: {}", userId);
        return personRepository.getThirdDegreeConnections(userId);
    }

    public Boolean sendConnectionRequest(Long receiverUserId) {
        Long senderUserId = UserContextHolder.getCurrentUserId();
        log.info("Trying to send connection request: sender: {}, and receiver: {}", senderUserId, receiverUserId);
        if(Objects.equals(senderUserId, receiverUserId)) {
            log.error("Sender and receiver cannot be the same person");
            throw new RuntimeException("Sender and receiver cannot be the same person");
        }
        boolean connectionRequestExists = personRepository.connectionRequestExist(senderUserId, receiverUserId);
        if(connectionRequestExists) {
            log.error("Connection request already exists. Cannot resend another request");
            throw new RuntimeException("Connection request already exists. Cannot resend another request");
        }

        boolean alreadyConnected = personRepository.alreadyConnected(senderUserId, receiverUserId);
        if(alreadyConnected) {
            log.info("Connection already exists, cannot send new connection request");
            throw new RuntimeException("Connection already exists, cannot send new connection request");
        }

        personRepository.createNewConnectionRequest(senderUserId, receiverUserId);
        log.info("Successfully sent the connection request from: {}, to: {}", senderUserId, receiverUserId);
        SendConnectionRequestEvent requestEvent = SendConnectionRequestEvent
                .builder()
                .senderId(senderUserId)
                .receiverId(receiverUserId)
                .build();
        sendRequestKafkaTemplate.send("send-connection-request-topic", requestEvent);
        return true;
    }

    public Boolean acceptConnectionRequest(Long senderUserId) {
        Long receiverUserId = UserContextHolder.getCurrentUserId();
        log.info("Trying to accept connection request: sender: {}, and receiver: {}", senderUserId, receiverUserId);
        if(Objects.equals(senderUserId, receiverUserId)) {
            log.error("Sender and receiver cannot be the same person");
            throw new RuntimeException("Sender and receiver cannot be the same person");
        }
        boolean connectionRequestExists = personRepository.connectionRequestExist(senderUserId, receiverUserId);
        if(!connectionRequestExists) {
            log.error("Connection request doesn't exists. Cannot accept non-existent request");
            throw new RuntimeException("Connection request doesn't exists. Cannot accept non-existent request");
        }

        personRepository.createNewConnection(senderUserId, receiverUserId);
        log.info("Successfully accepted connection request");

        AcceptConnectionRequestEvent acceptRequestEvent = AcceptConnectionRequestEvent
                .builder()
                .senderId(senderUserId)
                .receiverId(receiverUserId)
                .build();
        acceptRequestKafkaTemplate.send("accept-connection-request-topic", acceptRequestEvent);
        return true;
    }

    public Boolean rejectConnectionRequest(Long senderUserId) {
        Long receiverUserId = UserContextHolder.getCurrentUserId();
        log.info("Trying to reject connection request: sender: {}, and receiver: {}", senderUserId, receiverUserId);
        if(Objects.equals(senderUserId, receiverUserId)) {
            log.error("Sender and receiver cannot be the same person");
            throw new RuntimeException("Sender and receiver cannot be the same person");
        }
        boolean connectionRequestExists = personRepository.connectionRequestExist(senderUserId, receiverUserId);
        if(!connectionRequestExists) {
            log.error("Connection request doesn't exists. Cannot reject non-existent request");
            throw new RuntimeException("Connection request doesn't exists. Cannot reject non-existent request");
        }

        personRepository.rejectConnectionRequest(senderUserId, receiverUserId);
        log.info("Successfully rejected connection request");
        return true;
    }
}
