package com.example.jobalertbot.repository;

import com.example.jobalertbot.model.Subscriber;
import com.example.jobalertbot.model.SubscriberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    List<Subscriber> findByStatus(SubscriberStatus status);
}