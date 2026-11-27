package com.perfecthousehold.security.service;

import com.perfecthousehold.generic.mapper.DbObjectMapper;
import com.perfecthousehold.security.dao.UserDao;
import com.perfecthousehold.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final DbObjectMapper dbObjectMapper;

    public List<User> findAll() {
        String json = userDao.findAll();
        return dbObjectMapper.mapList(json, User.class);
    }

    public Optional<User> findById(Long id) {
        String json = userDao.findById(id);
        return dbObjectMapper.map(json, User.class);
    }

    public Optional<User> findByUsername(String username) {
        String json = userDao.findByUsername(username);
        return dbObjectMapper.map(json, User.class);
    }

    public Optional<User> findByEmail(String email) {
        return dbObjectMapper.map(userDao.findByEmail(email), User.class);
    }

    @Transactional
    public User registerUser(User user) {
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("ROLE_USER");
        }
        user.setEnabled(true);

        userDao.insert(user);
        return user;
    }

    @Transactional
    public void updatePassword(Long id, String passwordHash) {
        userDao.updatePassword(id, passwordHash);
    }
}
