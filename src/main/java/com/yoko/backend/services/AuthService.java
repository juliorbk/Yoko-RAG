package com.yoko.backend.services;

import com.yoko.backend.DTOs.AuthResponse;
import com.yoko.backend.DTOs.RegisterRequest;
import com.yoko.backend.DTOs.UserDTO;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
import com.yoko.backend.repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  //inyeccion de dependencias

  private final EmailService emailService;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    EmailService emailService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.emailService = emailService;
  }

  /**
   * Registers a new user and returns an AuthResponse containing a JWT token and the user object.
   *
   * @param request The RegisterRequest object containing the user's data.
   *
   * @return An AuthResponse object containing the JWT token and the user object.
   *
   * @throws RuntimeException If the user is already registered or the password is invalid.
   */
  public AuthResponse register(RegisterRequest request) {
    //Verificamos que no esté registrado

    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new RuntimeException("User already registered");
    }

    User newUser = User.builder()
      .name(request.getName())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .career(request.getCareer())
      .currentSemester(request.getCurrentSemester())
      .role(UserRole.STUDENT)
      .build();

    User registeredUser = userRepository.save(newUser);
    emailService.sendWelcomeEmail(
      registeredUser.getEmail(),
      registeredUser.getName()
    );

    String jwtToken = jwtService.generateToken(registeredUser.getEmail());

    System.out.println(AuthResponse.builder().token(jwtToken).build());
    return AuthResponse.builder().token(jwtToken).build();
  }

  /**
   * Logs in a user using the provided email and password.
   *
   * @param email Email of the user to log in.
   * @param password Password of the user to log in.
   *
   * @return The user object if the login is successful.
   * @throws UserNotFoundException
   * @throws InvalidPasswordException
   *
   * @throws RuntimeException If the user is not found or the password is invalid.
   */

  public AuthResponse login(String email, String password)
    throws BadCredentialsException {
    User user = userRepository
      .findByEmail(email)
      .orElseThrow(() ->
        new BadCredentialsException("Invalid Email or Password")
      );

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new BadCredentialsException("Invalid Email or Password");
    }

    String jwtToken = jwtService.generateToken(user.getEmail());
    UserDTO userDTO = UserDTO.builder()
      .id(user.getId())
      .name(user.getName())
      .email(user.getEmail())
      .role(user.getRole())
      .career(user.getCareer())
      .currentSemester(user.getCurrentSemester())
      .build();

    return AuthResponse.builder().token(jwtToken).user(userDTO).build();
  }
}
