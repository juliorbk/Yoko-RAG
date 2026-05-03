package com.yoko.backend.services;

/**
 * FIXED VERSION - Code review fixes applied on 2026-05-02
 * Fixes applied:
 * 1. Eliminated user enumeration vulnerability in register/login methods
 * 2. Standardized error messages to prevent email existence leakage
 */
import com.yoko.backend.DTOs.AuthResponse;
import com.yoko.backend.DTOs.OrgRegisterRequest;
import com.yoko.backend.DTOs.RegisterRequest;
import com.yoko.backend.DTOs.UserDTO;
import com.yoko.backend.entities.Organization;
import com.yoko.backend.entities.User;
import com.yoko.backend.entities.UserRole;
import com.yoko.backend.repositories.OrganizationRepository;
import com.yoko.backend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

  //inyeccion de dependencias

  private final EmailService emailService;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtService jwtService,
    EmailService emailService,
    OrganizationRepository organizationRepository
  ) {
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
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

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    // FIX: Eliminar verificación previa para evitar user enumeration
    // El registro continúa y se guarda siempre, sin revelar si el email ya existe

    Organization org = organizationRepository
      .findBySlug(request.getOrganizationSlug())
      .orElseThrow(() -> new RuntimeException("Organization not found"));

    User newUser = User.builder()
      .name(request.getName())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .role(UserRole.USER)
      .organization(org)
      .build();

    User registeredUser = userRepository.save(newUser);

    try {
      emailService.sendWelcomeEmail(
        registeredUser.getEmail(),
        registeredUser.getName()
      );
    } catch (Exception e) {
      log.error(
        "Failed to send welcome email to {}: {}",
        registeredUser.getEmail(),
        e.getMessage()
      );
    }

    String jwtToken = jwtService.generateToken(registeredUser.getEmail());

    log.debug(
      "Registered user: " +
        registeredUser +
        " in organization: " +
        org.getName()
    );
    return AuthResponse.builder()
      .token(jwtToken)
      .user(UserDTO.fromUser(registeredUser))
      .build();
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
    UserDTO userDTO = UserDTO.fromUser(user);

    log.debug("User logged in: " + userDTO);
    return AuthResponse.builder().token(jwtToken).user(userDTO).build();
  }

  @Transactional
  public AuthResponse organizationRegister(OrgRegisterRequest request) {

    String baseSlug = request
      .getOrganizationName()
      .toLowerCase()
      .replaceAll("[^a-z0-9\\s]", "")
      .trim()
      .replaceAll("\\s+", "-");

    String slug = generateUniqueSlug(baseSlug);

    Organization organization = Organization.builder()
      .name(request.getOrganizationName())
      .slug(slug)
      .plan("free")
      .active(true)
      .sector(request.getSector())
      .build();

    Organization savedOrg = organizationRepository.save(organization);

    User adminUser = User.builder()
      .name(request.getAdminName())
      .email(request.getAdminEmail())
      .password(passwordEncoder.encode(request.getAdminPassword()))
      .role(UserRole.ADMIN)
      .organization(savedOrg)
      .build();

    User savedAdmin = userRepository.save(adminUser);

    String jwtToken = jwtService.generateToken(savedAdmin.getEmail());

    UserDTO userDTO = UserDTO.fromUser(savedAdmin);

    return AuthResponse.builder().token(jwtToken).user(userDTO).build();
  }

  /**
   * Método de apoyo para generar un slug único en la base de datos.
   * Verifica si existe, y si es así, le anexa un sufijo numérico incremental.
   */
  @Transactional
  private String generateUniqueSlug(String baseSlug) {
    String slugToTry = baseSlug;
    int counter = 1;

    // Mientras exista una organización con ese slug, seguimos intentando
    while (organizationRepository.findBySlug(slugToTry).isPresent()) {
      slugToTry = baseSlug + "-" + counter;
      counter++;
    }

    return slugToTry;
  }
}
