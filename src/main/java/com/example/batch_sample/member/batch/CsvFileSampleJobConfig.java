package com.example.batch_sample.member.batch;

import com.example.batch_sample.global.reader.LinkedListItemReader;
import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import com.example.batch_sample.member.fixtures.MemberEntityFixtures;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CsvFileSampleJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;

  private static final int CHUNK_SIZE = 5;

  @Bean
  public Job CsvFileSampleJob() throws Exception {
    return this.jobBuilderFactory.get("CSV_FILE_IO_SAMPLE_JOB")
        .incrementer(new RunIdIncrementer())
        .start(this.CsvFileSampleStep())
        .build();
  }

  @Bean
  @JobScope
  public Step CsvFileSampleStep() throws Exception {
    return this.stepBuilderFactory.get("CSV_FILE_IO_SAMPLE_STEP")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(csvFileItemReader())
        .writer(loggingWriter())
        .build();
  }

  public FlatFileItemReader<MemberEntity> csvFileItemReader() throws Exception {
    // tokenizer
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setNames("id", "name", "email");

    // lineMapper
    DefaultLineMapper<MemberEntity> lineMapper = new DefaultLineMapper<>();
    lineMapper.setLineTokenizer(tokenizer);

    // fieldMapping 지정
    lineMapper.setFieldSetMapper(fieldSet -> {
      int id = fieldSet.readInt("id");
      String name = fieldSet.readString("name");
      String age = fieldSet.readString("email");

      return new MemberEntityFactory().readFrom(Long.parseLong(String.valueOf(id)), name, age);
    });

    // FlatMapItemReader 정의
    FlatFileItemReader<MemberEntity> itemReader = new FlatFileItemReaderBuilder<MemberEntity>()
        .name("CSV_FILE_ITEM_READER") // READER NAME 지정
        .encoding("UTF-8") // encoding 방식 지정
        .resource(new ClassPathResource("member-test-1.csv")) // Classpath 내의 test.csv 파일
        .linesToSkip(1) // 첫 1줄은 skip (제목)
        .lineMapper(lineMapper) // lineMapper 지정
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
