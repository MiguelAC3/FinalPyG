package api.pyapi.RestControllers;

import java.util.Map;
import java.util.UUID;

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

	// ✅ Constantes para evitar literales duplicados
	private static final String KEY_MESSAGE       = "message";
	private static final String KEY_ID            = "id";
	private static final String KEY_USERNAME      = "username";
	private static final String KEY_ACCESS_TOKEN  = "accessToken";
	private static final String KEY_REFRESH_TOKEN = "refreshToken";
	private static final String KEY_TOKEN_TYPE    = "tokenType";
	private static final String BEARER            = "Bearer";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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
				KEY_MESSAGE,       "User created",
				KEY_ID,            saved.getId(),
				KEY_USERNAME,      saved.getUsername(),
				KEY_ACCESS_TOKEN,  accessToken,
				KEY_REFRESH_TOKEN, refreshToken,
				KEY_TOKEN_TYPE,    BEARER
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
				KEY_MESSAGE,       "Login successful",
				KEY_ID,            user.getId(),
				KEY_USERNAME,      user.getUsername(),
				KEY_ACCESS_TOKEN,  accessToken,
				KEY_REFRESH_TOKEN, refreshToken,
				KEY_TOKEN_TYPE,    BEARER
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
				KEY_MESSAGE,       "Token refreshed",
				KEY_ACCESS_TOKEN,  newAccessToken,
				KEY_REFRESH_TOKEN, newRefreshToken,
				KEY_TOKEN_TYPE,    BEARER
		));
	}

	@PostMapping("/createrandomuser")
	public ResponseEntity<Map<String, Object>> createRandomUser() {
		byte[] randomBytes = new byte[16];
		SECURE_RANDOM.nextBytes(randomBytes);
		String secureRandomPassword = Base64.getEncoder().encodeToString(randomBytes);

		String randomSuffix = UUID.randomUUID().toString().substring(0, 8);

		UserEntity user = new UserEntity();
		user.setId(System.currentTimeMillis());
		user.setUsername("user_" + randomSuffix);
		user.setPassword(passwordEncoder.encode(secureRandomPassword));

		UserEntity saved = userRepository.save(user);
		String accessToken = jwtService.generateAccessToken(saved.getId());
		String refreshToken = jwtService.generateRefreshToken(saved.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
				KEY_MESSAGE,       "Random user created",
				KEY_ID,            saved.getId(),
				KEY_USERNAME,      saved.getUsername(),
				KEY_ACCESS_TOKEN,  accessToken,
				KEY_REFRESH_TOKEN, refreshToken,
				KEY_TOKEN_TYPE,    BEARER
		));
	}
}
