package com.example.batch_sample.member.batch;

import com.example.batch_sample.global.reader.LinkedListItemReader;
import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import com.example.batch_sample.member.fixtures.MemberEntityFixtures;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class CsvToTableMemberDataBatchConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;
  private final EntityManagerFactory entityManagerFactory;
  private final MemberEntityFactory memberEntityFactory;

  @Bean
  public Job csvToTableMemberDataJob() throws Exception{
    return jobBuilderFactory.get("csvToTableMemberDataJob")
        .incrementer(new RunIdIncrementer())
        .start(readDumpDataStep())
        .next(csvFileReadStep())
//        .next(this.jdbcStep())
//        .next(this.jpaStep())
        .build();
  }

  @Bean
  public Step readDumpDataStep(){
    return this.stepBuilderFactory.get("readDumpDataStep")
        .<MemberEntity, MemberEntity>chunk(10)
        .reader(new LinkedListItemReader<MemberEntity>(MemberEntityFixtures.normalMemberList()))
        .writer(loggingWriter())
        .build();
  }

  @Bean
  public Step csvFileReadStep() throws Exception {
    return stepBuilderFactory.get("csvFileReadStep")
        .<MemberEntity, MemberEntity>chunk(10)
        .reader(csvFileItemReader())
        .writer(loggingWriter())
        .build();
  }

  public FlatFileItemReader<MemberEntity> csvFileItemReader() throws Exception {
    DefaultLineMapper<MemberEntity> lineMapper = new DefaultLineMapper<>();
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setNames("id", "name", "email");
    lineMapper.setLineTokenizer(tokenizer);

    lineMapper.setFieldSetMapper(fieldSet -> {
      int id = fieldSet.readInt("id");
      String name = fieldSet.readString("name");
      String age = fieldSet.readString("email");

      return new MemberEntityFactory().readFrom(Long.parseLong(String.valueOf(id)), name, age);
    });

    FlatFileItemReader<MemberEntity> itemReader = new FlatFileItemReaderBuilder<MemberEntity>()
        .name("csvFileItemReader")
        .encoding("UTF-8")
        .resource(new ClassPathResource("test.csv"))
        .linesToSkip(1)
        .lineMapper(lineMapper)
        .build();
    itemReader.afterPropertiesSet();

    return itemReader;
  }

  private ItemWriter<MemberEntity> loggingWriter(){
    return items -> {
      String s = items.stream().map(MemberEntity::getName).collect(Collectors.joining(", "));
      log.info(s);
    };
  }

}
