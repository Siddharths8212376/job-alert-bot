package com.example.jobalertbot.service;

import com.example.jobalertbot.dto.SignupRequest;
import com.example.jobalertbot.exception.SubscriberNotFoundException;
import com.example.jobalertbot.model.Subscriber;
import com.example.jobalertbot.model.SubscriberStatus;
import com.example.jobalertbot.model.UserCriteria;
import com.example.jobalertbot.repository.SubscriberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final EmailService emailService;

    public SubscriberService(SubscriberRepository subscriberRepository, EmailService emailService) {
        this.subscriberRepository = subscriberRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void signup(SignupRequest request) {

        Optional<Subscriber> existing =
                subscriberRepository.findByEmail(request.getEmail());

        if (existing.isPresent()) {
            throw new RuntimeException(
                    "Subscriber with this email already exists."
            );
        }

        // Create subscriber
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(request.getEmail());
        subscriber.setStatus(SubscriberStatus.PENDING_APPROVAL);

        // Create criteria
        UserCriteria criteria = new UserCriteria();
        criteria.setSubscriber(subscriber);
        criteria.setTitles(request.getTitles());
        criteria.setLocations(request.getLocations());
        criteria.setMinExperience(request.getMinExperience());
        criteria.setMaxExperience(request.getMaxExperience());
        criteria.setRequiredSkills(request.getRequiredSkills());
        criteria.setExcludedKeywords(request.getExcludedKeywords());
        criteria.setMinimumScore(
                request.getMinimumScore() != null
                        ? request.getMinimumScore()
                        : 70
        );

        // Set bidirectional relationship
        subscriber.setCriteria(criteria);

        // Cascade saves criteria automatically
        subscriberRepository.save(subscriber);
    }

    @Transactional
    public void approveSubscriber(Long subscriberId) throws IOException, InterruptedException {
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new SubscriberNotFoundException("Subscriber not found."));

        emailService.addSubscriberToMailgun(subscriber.getEmail());

        subscriber.setStatus(SubscriberStatus.ACTIVE);
        subscriber.setApprovedAt(LocalDateTime.now());

        subscriberRepository.save(subscriber);
    }
}