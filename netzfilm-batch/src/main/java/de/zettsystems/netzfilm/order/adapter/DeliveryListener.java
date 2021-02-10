package de.zettsystems.netzfilm.order.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Component
public class DeliveryListener {
    private static final Logger LOG = LoggerFactory.getLogger(DeliveryListener.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final String FILE_NAME = "c:/temp/%s_delivery.csv";
    private final JobLauncher jobLauncher;
    private final Job importDeliveries;

    public DeliveryListener(JobLauncher jobLauncher, Job importDeliveries) {
        this.jobLauncher = jobLauncher;
        this.importDeliveries = importDeliveries;
    }

    @JmsListener(destination = "delivery")
    public void processDelivery(List<String> content) {
        Path newFilePath = Paths.get(String.format(FILE_NAME, LocalDateTime.now().format(FORMATTER)));
        try {
            Files.createFile(newFilePath);
            for (String line : content) {
                Files.writeString(newFilePath, line + "\n", StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            }
            LOG.info("Delivery received...starting import...");
            importJob();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importJob() {
        // make unique JobParameters so now instance of job can be started
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("source", "Fake Delivery")
                .addString("pathToFiles", "file:c:/temp/*_delivery.csv")
                .addDate("date", Date.from(Instant.now())) // needed for uniqueness
                .toJobParameters();
        try {
            LOG.info("Look for a delivery.");
            JobExecution ex = jobLauncher.run(importDeliveries, jobParameters);
            LOG.info(String.format("Execution status-----> %s, Execution Start Time ------> %s, Execution End Time %s", ex.getStatus(), ex.getStartTime(), ex.getEndTime()));
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            e.printStackTrace();
        }
    }
}