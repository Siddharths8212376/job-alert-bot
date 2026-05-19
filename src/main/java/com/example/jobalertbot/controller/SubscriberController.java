package com.example.jobalertbot.controller;

import com.example.jobalertbot.dto.SignupRequest;
import com.example.jobalertbot.service.SubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        subscriberService.approveSubscriber(id);
        return ResponseEntity.ok("Subscriber approved successfully.");
    }
}