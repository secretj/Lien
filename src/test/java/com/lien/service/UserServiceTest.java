package com.lien.service;

import com.lien.entity.User;
import com.lien.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void 회원가입_성공() {
        Mockito.when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User user = userService.register("test@email.com", "pw1234", "홍길동");
        assertEquals("test@email.com", user.getEmail());
        assertTrue(new BCryptPasswordEncoder().matches("pw1234", user.getPassword()));
    }

    @Test
    void 회원가입_이메일중복_실패() {
        Mockito.when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new User("test@email.com", "pw", "홍길동")));

        assertThrows(IllegalArgumentException.class, () ->
                userService.register("test@email.com", "pw1234", "홍길동"));
    }

    @Test
    void 로그인_성공() {
        String rawPw = "pw1234";
        String encodedPw = new BCryptPasswordEncoder().encode(rawPw);
        User user = new User("test@email.com", encodedPw, "홍길동");
        Mockito.when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        User result = userService.authenticate("test@email.com", rawPw);
        assertEquals("test@email.com", result.getEmail());
    }

    @Test
    void 로그인_실패() {
        Mockito.when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                userService.authenticate("test@email.com", "pw1234"));
    }
} 