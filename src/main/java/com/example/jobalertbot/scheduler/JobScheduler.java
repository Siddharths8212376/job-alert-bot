package com.example.jobalertbot.scheduler;

import com.example.jobalertbot.service.JobAggregationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobScheduler {

    private final JobAggregationService jobAggregationService;

    public JobScheduler(JobAggregationService jobAggregationService) {
        this.jobAggregationService = jobAggregationService;
    }

    @Scheduled(cron = "${app.scheduler.cron}", zone = "Asia/Kolkata")
    public void runScheduledJobSearch() {
        jobAggregationService.runJobSearch();
    }
}