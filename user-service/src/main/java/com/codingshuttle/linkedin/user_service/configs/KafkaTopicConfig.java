package com.codingshuttle.linkedin.user_service.configs;

import com.codingshuttle.linkedin.user_service.constants.AppConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
    @Bean
    public NewTopic sendConnectionRequestTopic() {
        return new NewTopic("send-connection-request-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic acceptConnectionRequestTopic() {
        return new NewTopic("accept-connection-request-topic", 3, (short) 1);
    }

    @Bean
    public NewTopic userCreatedTopic() {
        return new NewTopic(AppConstants.USER_CREATED_TOPIC, 3, (short) 1);
    }
}
