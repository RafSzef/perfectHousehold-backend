package com.perfecthousehold.security.dao;

import com.perfecthousehold.security.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

    String findById(@Param("id") Long id);

    String findAll();

    String findByUsername(@Param("username") String username);

    String findByEmail(@Param("email") String email);

    int insert(@Param("user") User user);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
