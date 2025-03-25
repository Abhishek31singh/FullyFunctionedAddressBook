package com.example.addressBook.controller;

import com.example.addressBook.model.User;
import com.example.addressBook.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{name}")
    public User getUser(@PathVariable String name) {
        return userService.getUser(name);
    }
}
