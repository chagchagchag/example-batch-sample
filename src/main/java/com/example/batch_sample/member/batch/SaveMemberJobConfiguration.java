package com.example.batch_sample.member.batch;

import com.example.batch_sample.global.exception.MemberNameNotExistException;
import com.example.batch_sample.member.batch.SaveMemberListener.SaveMemberAnnotationJobExecutionListener;
import com.example.batch_sample.member.entity.MemberEntity;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SaveMemberJobConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;

  @Bean
  public Job saveMemberJob() throws Exception {
    return jobBuilderFactory.get("saveMemberJob")
        .incrementer(new RunIdIncrementer())
        .start(saveMemberStep(null))
        .listener(new SaveMemberListener.SaveMemberJobExecutionListener())
        .listener(new SaveMemberListener.SaveMemberAnnotationJobExecutionListener())
        .build();
  }

  @Bean
  @JobScope
  public Step saveMemberStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
    return stepBuilderFactory.get("saveMemberStep")
        .<MemberEntity, MemberEntity>chunk(10)
        .reader(itemReader())
        .processor(itemProcessor(allowDuplicate))
        .writer(itemWriter())
        .listener(new SaveMemberAnnotationJobExecutionListener())
        .faultTolerant()
        .skip(MemberNameNotExistException.class)
        .skipLimit(2)
        .build();
  }

  private ItemWriter<? super MemberEntity> itemWriter() {
    return null;
  }

  private ItemReader<? extends MemberEntity> itemReader() throws Exception{
    return null;
  }

  private ItemProcessor<? super MemberEntity, ? extends MemberEntity> itemProcessor(String allowDuplicate) throws Exception {
    return null;
  }

}
