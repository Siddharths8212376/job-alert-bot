package com.example.jobalertbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_criteria")
@Getter
@Setter
public class UserCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "subscriber_id", nullable = false, unique = true)
    private Subscriber subscriber;

    @Column(length = 2000)
    private String titles;              // comma-separated

    @Column(length = 2000)
    private String locations;           // comma-separated

    private Integer minExperience;

    private Integer maxExperience;

    @Column(length = 2000)
    private String requiredSkills;      // comma-separated

    @Column(length = 2000)
    private String excludedKeywords;    // comma-separated

    private Integer minimumScore = 70;
}