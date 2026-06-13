package api.pyapi.RestControllers;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.pyapi.DTO.RefreshTokenDTO;
import api.pyapi.DTO.UserLoginDTO;
import api.pyapi.Entities.UserEntity;
import api.pyapi.Repository.UserRepository;
import api.pyapi.Security.JwtService;
import java.security.SecureRandom;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(UserRepository userRepository, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
		this.jwtService = jwtService;
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@Valid @RequestBody UserEntity request) {
		if (userRepository.existsById(request.getId())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("User id already exists");
		}

		request.setUsername(request.getUsername().trim());
		request.setPassword(passwordEncoder.encode(request.getPassword()));

		UserEntity saved = userRepository.save(request);
		String accessToken = jwtService.generateAccessToken(saved.getId());
		String refreshToken = jwtService.generateRefreshToken(saved.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				"message", "User created",
				"id", saved.getId(),
				"username", saved.getUsername(),
				"accessToken", accessToken,
				"refreshToken", refreshToken,
				"tokenType", "Bearer"
            ));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO request) {
		UserEntity user = userRepository.findById(request.getId()).orElse(null);
		if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}

		String accessToken = jwtService.generateAccessToken(user.getId());
		String refreshToken = jwtService.generateRefreshToken(user.getId());

		return ResponseEntity.ok(Map.of(
				"message", "Login successful",
				"id", user.getId(),
				"username", user.getUsername(),
				"accessToken", accessToken,
				"refreshToken", refreshToken,
				"tokenType", "Bearer"
		));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenDTO request) {
		String refreshToken = request.getRefreshToken();
		if (!jwtService.isRefreshTokenValid(refreshToken)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
		}

		long userId;
		try {
			userId = Long.parseLong(jwtService.extractSubject(refreshToken));
		} catch (NumberFormatException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token subject");
		}

		UserEntity user = userRepository.findById(userId).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found for token");
		}

		String newAccessToken = jwtService.generateAccessToken(userId);
		String newRefreshToken = jwtService.generateRefreshToken(userId);

		return ResponseEntity.ok(Map.of(
				"message", "Token refreshed",
				"accessToken", newAccessToken,
				"refreshToken", newRefreshToken,
				"tokenType", "Bearer"
		));
	}

@PostMapping("/createrandomuser")
public ResponseEntity<UserEntity> createRandomUser() {
    // 1. Generar una contraseña aleatoria y segura dinámicamente
    byte[] randomBytes = new byte[16];
    new SecureRandom().nextBytes(randomBytes);
    String secureRandomPassword = Base64.getEncoder().encodeToString(randomBytes);

    // 2. Preparar la entidad de usuario
    String randomSuffix = java.util.UUID.randomUUID().toString().substring(0, 8);
    UserEntity user = new UserEntity();
    user.setUsername("user_" + randomSuffix);
    
    // 3. Codificar la contraseña segura generada
    user.setPassword(passwordEncoder.encode(secureRandomPassword));
    
    UserEntity saved = userRepository.save(user);
    
    // 4. El tipo de retorno ahora es explícitamente ResponseEntity<UserEntity>
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
}
}
