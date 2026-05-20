package com.example.jobalertbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"subscriber_id", "job_posting_id"}
                )
        }
)
@Getter
@Setter
public class JobNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private Subscriber subscriber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;

    @Column(nullable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}