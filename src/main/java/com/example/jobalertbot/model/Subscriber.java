package com.example.jobalertbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscribers")
@Getter
@Setter
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriberStatus status = SubscriberStatus.PENDING_APPROVAL;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime approvedAt;

    @OneToOne(mappedBy = "subscriber", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserCriteria criteria;
}