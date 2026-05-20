package com.example.jobalertbot.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String email;

    // Job titles (comma-separated)
    private String titles;

    // Preferred locations (comma-separated)
    private String locations;

    // Experience range
    private Integer minExperience;
    private Integer maxExperience;

    // Skills and exclusions (comma-separated)
    private String requiredSkills;
    private String excludedKeywords;

    // Minimum relevance score
    private Integer minimumScore = 70;
}