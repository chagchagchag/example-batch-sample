package com.example.batch_sample;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
public class MyTestConfiguration {
  @Bean
  public JobLauncherTestUtils jobLauncherTestUtils(){
    return new JobLauncherTestUtils();
  }
}
