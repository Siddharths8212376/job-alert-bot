package com.example.jobalertbot.repository;

import com.example.jobalertbot.model.UserCriteria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCriteriaRepository extends JpaRepository<UserCriteria, Long> {
}