package com.codingshuttle.linkedin.connection_service.entities;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
@Node
@Data
public class Person {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private String name;

    private String email;
}
