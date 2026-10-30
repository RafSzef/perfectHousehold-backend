package com.perfecthousehold.security.dao;

import com.perfecthousehold.security.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {

    String findByUsername(@Param("username") String username);

    String findByEmail(@Param("email") String email);

    int insert(@Param("user") User user);
}
