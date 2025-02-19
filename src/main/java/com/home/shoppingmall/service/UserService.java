package com.home.shoppingmall.service;

import lombok.RequiredArgsConstructor;
import com.home.shoppingmall.domain.User;
import com.home.shoppingmall.dto.UserJoinRequestDto;
import com.home.shoppingmall.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;


  public User signup(UserJoinRequestDto dto) {
    String encryptPassword = bCryptPasswordEncoder.encode(dto.getPassword());
    User user = dto.toEntity(encryptPassword);
    user = userRepository.save(user);
    return user;
  }

  public User findById(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Unexpected user"));
  }

  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
