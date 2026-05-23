package com.example.jobalertbot.controller;

import com.example.jobalertbot.dto.SignupRequest;
import com.example.jobalertbot.exception.MailgunRegistrationException;
import com.example.jobalertbot.exception.SubscriberNotFoundException;
import com.example.jobalertbot.service.SubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/subscribers")
public class SubscriberController {

    private final SubscriberService subscriberService;

    public SubscriberController(SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(
            @RequestBody SignupRequest request
    ) {
        subscriberService.signup(request);
        return ResponseEntity.ok("Signup successful. Your request is pending approval.");
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveSubscriber(
            @PathVariable Long id
    ) {
        try {
            subscriberService.approveSubscriber(id);
            return ResponseEntity.status(201).body("Subscriber approved successfully.");
        } catch (SubscriberNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (MailgunRegistrationException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).body("Request interrupted.");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to communicate with Mailgun.");
        }
    }
}