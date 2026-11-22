package com.perfecthousehold.security.service;

import com.perfecthousehold.generic.service.GenericService;
import com.perfecthousehold.security.dao.UserDao;
import com.perfecthousehold.security.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService extends GenericService {

    private final UserDao userDao;

    public List<User> findAll() {
        String json = userDao.findAll();
        return mapDbList(json, User.class);
    }

    public User findById(Long id) {
        String json = userDao.findById(id);
        return mapDbObject(json, User.class);
    }

    public User findByUsername(String username) {
        String json = userDao.findByUsername(username);
        return mapDbObject(json, User.class);
    }

    public User findByEmail(String email) {
        return mapDbObject(userDao.findByEmail(email), User.class);
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
