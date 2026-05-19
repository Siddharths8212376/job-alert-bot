package com.example.jobalertbot.repository;

import com.example.jobalertbot.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findByCompanyAndExternalJobId(String company, String externalJobId);

    // Returns all jobs for a given company
    List<JobPosting> findByCompany(String company);

    // Returns all currently active jobs
    List<JobPosting> findByActiveTrue();

    // Returns active jobs above a minimum relevance score
    List<JobPosting> findByActiveTrueAndRelevanceScoreGreaterThanEqual(int minimumScore);
}