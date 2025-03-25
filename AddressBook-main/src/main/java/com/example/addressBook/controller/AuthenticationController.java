package com.example.addressBook.controller;

import com.example.addressBook.dto.AuthUserDTO;
import com.example.addressBook.dto.LoginDTO;
import com.example.addressBook.model.AuthUser;
import com.example.addressBook.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:4200") // Allow Angular requests
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthUserDTO userDTO) {
        String response = authenticationService.register(userDTO);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginDTO loginDTO) {
        return authenticationService.login(loginDTO);
    }

    @GetMapping("/users")
    public List<AuthUser> getAllUsers() {
        return authenticationService.getAllUsers();  // Use service method
    }


    // Forgot Password Endpoint
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        authenticationService.processForgotPassword(email);
        return ResponseEntity.ok("Password reset email sent successfully.");
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || token.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body("Token and new password are required");
        }

        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successful.");
    }
}
