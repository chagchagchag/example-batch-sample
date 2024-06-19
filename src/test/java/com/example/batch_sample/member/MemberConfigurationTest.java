package com.example.batch_sample.member;

import com.example.batch_sample.MyTestConfiguration;
import com.example.batch_sample.member.batch.SaveMemberEntityJobConfiguration;
import com.example.batch_sample.member.entity.repository.MemberEntityRepository;
import com.example.batch_sample.member.enums.MemberEntitySteps;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.profiles.active=h2"})
@SpringBatchTest
@ContextConfiguration(
  classes = {
      MyTestConfiguration.class,
      SaveMemberEntityJobConfiguration.class,
  }
)
public class MemberConfigurationTest {
  @Autowired
  private JobLauncherTestUtils jobLauncherTestUtils;

  @Autowired
  private MemberEntityRepository memberEntityRepository;

  @AfterEach
  public void destroy() throws Exception{
    memberEntityRepository.deleteAll();
  }

  @Test
  public void TEST_SAVE_MEMBER_ENTITY_STEP(){
    // 단순 테스트
    String stepName = MemberEntitySteps.SAVE_MEMBER_ENTITY_STEP.getName();
    JobExecution jobExecution = jobLauncherTestUtils.launchStep(stepName);

    int sum = jobExecution.getStepExecutions().stream()
        .mapToInt(StepExecution::getWriteCount)
        .sum();

    Assertions.assertThat(sum).isEqualTo(memberEntityRepository.count());
  }

}
