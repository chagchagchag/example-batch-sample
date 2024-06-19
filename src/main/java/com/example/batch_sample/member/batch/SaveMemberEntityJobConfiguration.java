package com.example.batch_sample.member.batch;

import com.example.batch_sample.global.batch.processor.DuplicateValidationProcessor;
import com.example.batch_sample.global.exception.MemberNameNotExistException;
import com.example.batch_sample.member.batch.SaveMemberListener.SaveMemberAnnotationJobExecutionListener;
import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
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
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SaveMemberEntityJobConfiguration {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final EntityManagerFactory entityManagerFactory;
  private final MemberEntityFactory memberEntityFactory;

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

  private ItemWriter<? super MemberEntity> itemWriter() throws Exception {
    JpaItemWriter<MemberEntity> jpaItemWriter = new JpaItemWriterBuilder<MemberEntity>()
        .entityManagerFactory(entityManagerFactory)
        .build();

    ItemWriter<MemberEntity> loggingWriter = items -> log.info("member.size = {}", items.size());

    CompositeItemWriter<MemberEntity> itemWriter = new CompositeItemWriterBuilder<MemberEntity>()
        .delegates(jpaItemWriter, loggingWriter)
        .build();

    itemWriter.afterPropertiesSet();
    return itemWriter;
  }

  private ItemReader<? extends MemberEntity> itemReader() throws Exception{
    DefaultLineMapper<MemberEntity> lineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
    lineTokenizer.setNames("name", "email");
    lineMapper.setLineTokenizer(lineTokenizer);
    lineMapper.setFieldSetMapper(fieldSet -> memberEntityFactory
        .newMember(
          fieldSet.readRawString(0),
          fieldSet.readRawString(1)
        )
    );

    FlatFileItemReader<MemberEntity> itemReader = new FlatFileItemReaderBuilder<MemberEntity>()
        .name("saveMemberItemReader")
        .encoding("UTF-8")
        .linesToSkip(1)
        .resource(new ClassPathResource("member.csv"))
        .lineMapper(lineMapper)
        .build();

    itemReader.afterPropertiesSet();
    return itemReader;
  }

  private ItemProcessor<? super MemberEntity, ? extends MemberEntity> itemProcessor(String allowDuplicate) throws Exception {
    DuplicateValidationProcessor<MemberEntity> duplicateValidationProcessor =
        new DuplicateValidationProcessor<>(MemberEntity::getName, Boolean.parseBoolean(allowDuplicate));

    ItemProcessor<MemberEntity, MemberEntity> validateProcessor = item -> {
      if(item.isNotEmptyName()) return item;

      throw new MemberNameNotExistException();
    };

    CompositeItemProcessor<MemberEntity, MemberEntity> itemProcessor = new CompositeItemProcessorBuilder<MemberEntity, MemberEntity>()
        .delegates(new MemberEntityValidationRetryProcessor(), validateProcessor,
            duplicateValidationProcessor)
        .build();

    itemProcessor.afterPropertiesSet();
    return itemProcessor;
  }

}
