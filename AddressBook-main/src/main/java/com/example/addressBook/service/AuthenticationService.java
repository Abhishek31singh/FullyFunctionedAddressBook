package com.example.addressBook.service;

import com.example.addressBook.dto.AuthUserDTO;
import com.example.addressBook.dto.LoginDTO;
import com.example.addressBook.model.AuthUser;
import com.example.addressBook.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationService {

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;  // ✅ Fixed Bean Type

    private static final String REDIS_USER_PREFIX = "USER_";

    public String register(AuthUserDTO userDTO) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already in use");
            }

            // Validate password (must not be empty)
            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty");
            }

            // Create new user
            AuthUser user = new AuthUser();
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setHashPass(passwordEncoder.encode(userDTO.getPassword()));

            userRepository.save(user);

            // Try saving in Redis
            try {
                redisTemplate.opsForValue().set("USER_" + user.getEmail(), user, 10, TimeUnit.MINUTES);
            } catch (Exception e) {
                System.err.println("⚠️ Redis error, skipping caching...");
            }

            return "User registered successfully!";
        } catch (Exception e) {
            e.printStackTrace(); // ✅ This will show the real issue in logs!
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User registration failed. Email might be already in use or data is invalid.");
        }
    }



    public Map<String, String> login(LoginDTO loginDTO) {
        String cacheKey = REDIS_USER_PREFIX + loginDTO.getEmail();

        AuthUser user = null;

        // First, check Redis for cached user
        try {
            user = (AuthUser) redisTemplate.opsForValue().get(cacheKey);
            if (user != null) {
                System.out.println("✅ Found user in Redis: " + loginDTO.getEmail());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Redis is down! Falling back to database...");
        }

        // If not found in Redis, get from database
        if (user == null) {
            user = userRepository.findByEmail(loginDTO.getEmail())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Store user in Redis cache for future requests
            try {
                redisTemplate.opsForValue().set(cacheKey, user, 10, TimeUnit.MINUTES);
                System.out.println("✅ User cached in Redis: " + loginDTO.getEmail());
            } catch (Exception e) {
                System.err.println("⚠️ Redis is still down! Skipping caching...");
            }
        }

        // Validate password
        if (passwordEncoder.matches(loginDTO.getPassword(), user.getHashPass())) {
            String token = jwtTokenService.createToken(user.getId());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            return response;
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    /**
     * 🔹 **Forgot Password**
     * Generates a password reset token and sends an email with the reset link.
     */
    public void processForgotPassword(String email) {
        AuthUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // Token expires in 15 minutes

        userRepository.save(user);

        // Send email with reset token
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    /**
     * 🔹 **Reset Password**
     * Allows the user to reset their password using a valid token.
     */
    public void resetPassword(String token, String newPassword) {
        AuthUser user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or expired token"));

        // Check if token is expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        // Update password
        user.setHashPass(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    public List<AuthUser> getAllUsers() {
        return userRepository.findAll();  // Fetch all users from the database
    }

}
