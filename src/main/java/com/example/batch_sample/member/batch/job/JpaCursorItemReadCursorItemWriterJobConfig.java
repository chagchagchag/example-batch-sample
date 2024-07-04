package com.example.batch_sample.member.batch.job;

import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JpaCursorItemReadCursorItemWriterJobConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;
  private final MemberEntityFactory memberEntityFactory;
  private static final int CHUNK_SIZE = 10;

  @Bean
  public Job jpaCursorItemReadCursorItemWriterJob() throws Exception {
    return this.jobBuilderFactory.get("JOB__jpaCursorItemReadCursorItemWriterJob")
        .incrementer(new RunIdIncrementer())
        .start(this.jpaCursorItemReadCursorItemWriterStep())
        .build();
  }

  @Bean
  @JobScope
  public Step jpaCursorItemReadCursorItemWriterStep() throws Exception {
    return this.stepBuilderFactory.get("STEP__jpaCursorItemReadCursorItemWriterStep")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(jpaCursorItemReader())
        .processor(itemProcessor())
        .writer(jpaItemWriter())
        .build();
  }

  public JpaCursorItemReader<MemberEntity> jpaCursorItemReader() throws Exception {
    JpaCursorItemReader<MemberEntity> itemReader = new JpaCursorItemReaderBuilder<MemberEntity>()
        .name("jpaCursorItemReader")
        .entityManagerFactory(entityManagerFactory)
        .queryString("select m from MemberEntity m")
        .build();

    itemReader.afterPropertiesSet();

    return itemReader;
  }

  public ItemProcessor<MemberEntity, MemberEntity> itemProcessor(){
    return item -> memberEntityFactory.readFrom(item.getId(), item.getName() + " +++ " + item.getName(), item.getEmail());
  }


  public ItemWriter<MemberEntity> jpaItemWriter() throws Exception {
    JpaItemWriter<MemberEntity> itemWriter = new JpaItemWriterBuilder<MemberEntity>()
        .entityManagerFactory(entityManagerFactory)
        .build();

    itemWriter.afterPropertiesSet();
    return itemWriter;
  }

}
