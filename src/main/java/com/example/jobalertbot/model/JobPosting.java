package com.example.jobalertbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "job_postings",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"company", "external_job_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;

    @Column(name = "external_job_id")
    private String externalJobId;

    private String title;

    private String location;

    private Integer minExperience;

    private Integer maxExperience;

    @Column(length = 5000)
    private String description;

    private String url;

    private Integer relevanceScore;

    private Boolean notified = false;

    private LocalDate postedDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return "JobPosting{" +
                "company='" + company + '\'' +
                ", externalJobId='" + externalJobId + '\'' +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", relevanceScore=" + relevanceScore +
                ", postedDate=" + postedDate +
                ", createdAt=" + createdAt +
                ", active=" + active +
                '}';
    }
}