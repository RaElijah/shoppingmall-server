package com.home.shoppingmall.service;

import lombok.RequiredArgsConstructor;
import com.home.shoppingmall.domain.User;
import com.home.shoppingmall.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public User loadUserByUsername(String username) {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException(username));
  }
}
