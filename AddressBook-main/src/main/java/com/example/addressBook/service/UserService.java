package com.example.addressBook.service;

import com.example.addressBook.model.User;
import com.example.addressBook.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String REDIS_USER_PREFIX = "USER_";

    public User getUser(String name) {
        String cacheKey = REDIS_USER_PREFIX + name;

        try {
            // Try fetching from Redis
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                System.out.println("✅ Found in Redis: " + name);
                return cachedUser;
            }
        } catch (DataAccessException e) {
            System.err.println("⚠️ Redis is down! Falling back to database...");
        }

        // Fetch from MySQL if Redis fails
        Optional<User> userFromDb = userRepository.findByName(name);
        if (userFromDb.isPresent()) {
            User user = userFromDb.get();

            // Try caching in Redis
            try {
                redisTemplate.opsForValue().set(cacheKey, user, 10, TimeUnit.MINUTES);
                System.out.println("✅ Stored in Redis: " + name);
            } catch (DataAccessException e) {
                System.err.println("⚠️ Redis is still down! Skipping caching...");
            }

            return user;
        } else {
            throw new RuntimeException("User not found: " + name);
        }
    }
}
