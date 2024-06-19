package com.example.batch_sample.member.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.core.annotation.BeforeStep;

@Slf4j
public class SaveMemberListener {
  public static class SaveMemberStepExecutionListener {
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
      log.info("Before Step");
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
      log.info("After Step : {}", stepExecution.getWriteCount());
      return stepExecution.getExitStatus();
    }
  }

  public static class SaveMemberJobExecutionListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution) {
      log.info("Before Job");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
      int sum = jobExecution.getStepExecutions().stream()
          .mapToInt(StepExecution::getWriteCount)
          .sum();

      log.info("After Job : {}", sum);
    }
  }

  public static class SaveMemberAnnotationJobExecutionListener {
    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
      log.info("annotationBeforeJob");
    }

    @AfterJob
    public void afterJob(JobExecution jobExecution) {
      int sum = jobExecution.getStepExecutions().stream()
          .mapToInt(StepExecution::getWriteCount)
          .sum();

      log.info("annotationAfterJob : {}", sum);
    }
  }
}
