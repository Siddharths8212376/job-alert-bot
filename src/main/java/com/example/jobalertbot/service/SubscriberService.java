package com.example.jobalertbot.service;

import com.example.jobalertbot.dto.SignupRequest;
import com.example.jobalertbot.model.Subscriber;
import com.example.jobalertbot.model.SubscriberStatus;
import com.example.jobalertbot.model.UserCriteria;
import com.example.jobalertbot.repository.SubscriberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;

    public SubscriberService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
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
    public void approveSubscriber(Long subscriberId) {
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("Subscriber not found."));

        subscriber.setStatus(SubscriberStatus.ACTIVE);
        subscriber.setApprovedAt(LocalDateTime.now());

        subscriberRepository.save(subscriber);
    }
}