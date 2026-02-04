package com.example.todo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.todo.entity.AppUser;
import com.example.todo.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Component
public class UserDataInitializer implements CommandLineRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    createUserIfMissing("admin", "password", "admin@example.com");
  }

  private void createUserIfMissing(String username, String rawPassword, String email) {
    if (userRepository.findByUsername(username).isPresent()) {
      return;
    }
    AppUser user = new AppUser();
    user.setUsername(username);
    user.setPassword(passwordEncoder.encode(rawPassword));
    user.setEmail(email);
    user.setEnabled(true);
    userRepository.save(user);
  }
}
