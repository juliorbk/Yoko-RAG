package com.yoko.backend.services;

import com.yoko.backend.DTOs.RegisterRequest;
import com.yoko.backend.entities.Role;
import com.yoko.backend.entities.User;
import com.yoko.backend.repositories.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  //inyeccion de dependencias

  private final UserRepository userRepository;

  public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User register(RegisterRequest request) {
    //Verificamos que no esté registrado

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("User already registered");
    }

    User newUser = User.builder()
      .name(request.getName())
      .email(request.getEmail())
      .password(request.getPassword())
      .career(request.getCareer())
      .currentSemester(request.getCurrentSemester())
      .role(Role.STUDENT)
      .build();

    return userRepository.save(newUser);
  }

  /**
   * Logs in a user using the provided email and password.
   *
   * @param email Email of the user to log in.
   * @param password Password of the user to log in.
   *
   * @return The user object if the login is successful.
   *
   * @throws RuntimeException If the user is not found or the password is invalid.
   */

  public User login(String email, String password) {
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
      throw new RuntimeException("User not found");
    }
    User user = userOpt.get();
    if (!user.getPassword().equals(password)) {
      throw new RuntimeException("Invalid password");
    }
    return user;
  }
}
