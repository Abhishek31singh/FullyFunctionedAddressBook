package com.example.addressBook.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class AuthUserDTO {
    private String name;
    private String email;
    private String phone;
    private String password;

    // Getters and Setters
}