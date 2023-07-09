package com.example.hicode.repository;

import com.example.hicode.model.Apple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AppleRepository extends JpaRepository<Apple, Long>, JpaSpecificationExecutor<Apple> {
}
