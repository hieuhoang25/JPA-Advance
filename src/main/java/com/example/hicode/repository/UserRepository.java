package com.example.hicode.repository;

import com.example.hicode.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.util.LinkedCaseInsensitiveMap;

public interface UserRepository  extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

}
