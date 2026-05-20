package com.example.jobalertbot.repository;

import com.example.jobalertbot.model.JobNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobNotificationRepository extends JpaRepository<JobNotification, Long> {

    boolean existsBySubscriberIdAndJobPostingId(Long subscriberId, Long jobPostingId);
}