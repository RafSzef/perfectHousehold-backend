package com.perfecthousehold.security.service;

import com.perfecthousehold.generic.service.GenericService;
import com.perfecthousehold.security.dao.UserDao;
import com.perfecthousehold.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService extends GenericService {

    private final UserDao userDao;

    public User findByUsername(String username) {
        String json = userDao.findByUsername(username);
        return mapDbObject(json, User.class);
    }

    public User findByEmail(String email) {
        return mapDbObject(userDao.findByEmail(email), User.class);
    }

    /**
     * Registers a new user directly from JSON payload (String or Map)
     * using mapDbObject from GenericService without intermediate DTOs.
     */
    @Transactional
    public User registerUser(User user) {

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }
        user.setEnabled(true);

        userDao.insert(user);
        return user;
    }
}
