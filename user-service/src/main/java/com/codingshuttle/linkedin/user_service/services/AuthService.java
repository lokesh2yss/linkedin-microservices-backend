package com.codingshuttle.linkedin.user_service.services;

import com.codingshuttle.linkedin.user_service.constants.AppConstants;
import com.codingshuttle.linkedin.user_service.dto.LoginRequestDto;
import com.codingshuttle.linkedin.user_service.dto.SignupRequestDto;
import com.codingshuttle.linkedin.user_service.dto.UserDto;
import com.codingshuttle.linkedin.user_service.entities.User;
import com.codingshuttle.linkedin.user_service.exceptions.BadRequestException;
import com.codingshuttle.linkedin.user_service.kafka.events.UserCreatedEvent;
import com.codingshuttle.linkedin.user_service.repositories.UserRepository;
import com.codingshuttle.linkedin.user_service.utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final KafkaTemplate<Long, UserCreatedEvent> kafkaTemplate;

    public UserDto signup(SignupRequestDto signupRequestDto) {
        if(userRepository.existsByEmail(signupRequestDto.getEmail())) {
            throw new BadRequestException("User already exists with email: "+ signupRequestDto.getEmail());
        }
        log.info("User is being created in User-Service: {}", signupRequestDto);
        User user = modelMapper.map(signupRequestDto, User.class);
        user.setId(null);
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        User savedUser = userRepository.save(user);
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                        .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .build();
        kafkaTemplate.send(AppConstants.USER_CREATED_TOPIC, userCreatedEvent.getUserId(),  userCreatedEvent);
        log.info("Kafka message sent on topic: {}, for further operations", AppConstants.USER_CREATED_TOPIC);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public String login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found with email: "+loginRequestDto.getEmail()));
        String hashedPassword = user.getPassword();

        boolean isPasswordMatch = PasswordUtil.checkPassword(loginRequestDto.getPassword(), hashedPassword);
        if(!isPasswordMatch) throw new BadRequestException("Incorrect password, please try again with the correct password");

        return jwtService.getAccessToken(user);
    }
}
