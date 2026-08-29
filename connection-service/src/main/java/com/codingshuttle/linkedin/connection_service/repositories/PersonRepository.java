package com.codingshuttle.linkedin.connection_service.repositories;

import com.codingshuttle.linkedin.connection_service.entities.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {
    List<Person> getByName(String name);
    Optional<Person> getByUserId(Long userId);

    @Query("MATCH (personA: Person)-[:CONNECTED_TO]- (personB: Person) " +
            "WHERE personA.userId = $userId " +
            "RETURN personB")
    List<Person> getFirstDegreeConnections(Long userId);

    @Query("MATCH (personA: Person)-[:CONNECTED_TO]- (friend: Person) -[:CONNECTED_TO] - (secondDegree: Person) " +
            "WHERE personA.userId = $userId " +
            "AND NOT(personA) -[:CONNECTED_TO] - (secondDegree) " +
            "RETURN secondDegree")
    List<Person> getSecondDegreeConnections(Long userId);


    @Query("MATCH (personA: Person)-[:CONNECTED_TO]- (friend: Person) -[:CONNECTED_TO] - (secondDegree: Person) " +
            "-[:CONNECTED_TO]- (thirdDegree: Person) "+
            "WHERE personA.userId = $userId " +
            "AND NOT(personA) -[:CONNECTED_TO] - (secondDegree) " +
            "AND NOT(personA) -[:CONNECTED_TO] - (thirdDegree) " +
            "AND NOT(friend) -[:CONNECTED_TO] - (thirdDegree) " +
            "RETURN thirdDegree")
    List<Person> getThirdDegreeConnections(Long userId);

    @Query("MATCH (p1: Person) -[r: REQUESTED_TO]-> (p2: Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "return count(r) > 0")
    boolean connectionRequestExist(Long senderId, Long receiverId);


    @Query("MATCH (p1: Person) -[r: CONNECTED_TO]- (p2: Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "return count(r) > 0")
    boolean alreadyConnected(Long senderId, Long receiverId);

    @Query("MATCH (p1: Person), (p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "CREATE (p1) - [r: REQUESTED_TO]-> (p2) ")
    void createNewConnectionRequest(Long senderId, Long receiverId);

    @Query("MATCH (p1: Person) - [r:REQUESTED_TO] -> (p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r " +
            "CREATE (p1) - [: CONNECTED_TO]-> (p2) ")
    void createNewConnection(Long senderId, Long receiverId);

    @Query("MATCH (p1: Person) - [r:REQUESTED_TO] -> (p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r")
    void rejectConnectionRequest(Long senderUserId, Long receiverUserId);

    @Query("CREATE (p: Person {name= $name, email: $email, userId = $userId}) " +
            "Return p")
    Person createNewUser(String name, String email, Long userId);
}
