package com.example.batch_sample.member.batch.job;

import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CsvInputOutputJobConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final MemberEntityFactory memberEntityFactory;

  @Bean
  public Job csvInputOutputJob() throws Exception {
    return this.jobBuilderFactory.get("CSV_INPUT_OUTPUT_JOB_1111111")
        .incrementer(new RunIdIncrementer())
        .start(this.csvInputOutputStep())
        .build();
  }

  @Bean
  @JobScope
  public Step csvInputOutputStep() throws Exception {
    return this.stepBuilderFactory.get("csvInputOutputStep")
        .<MemberEntity, MemberEntity>chunk(30)
        .reader(this.csvInputOutputReader_1111111())
        .writer(loggingWriter())
        .build();
  }

  @Bean
  @StepScope
  public FlatFileItemReader<MemberEntity> csvInputOutputReader_1111111() throws Exception {
    FileSystemResource fileSystemResource = new FileSystemResource("src/main/resources/member-test-input.csv");
    log.info("classpathResource >>>>>>> {}", fileSystemResource.getFile().getPath());
    // lineMapper
    DefaultLineMapper<MemberEntity> lineMapper = new DefaultLineMapper<>();
    // tokenizer
    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
    tokenizer.setNames("id", "name", "email");

    lineMapper.setLineTokenizer(tokenizer);

    // fieldMapping 지정
    lineMapper.setFieldSetMapper(fieldSet -> {
      Long id = fieldSet.readLong("id");
      String name = fieldSet.readString("name");
      String age = fieldSet.readString("email");
      log.info(">>>>> id = {}, name = {}, age = {}", id, name, age);

      return memberEntityFactory.readFrom(id, name, age);
    });

    // FlatMapItemReader 정의
    FlatFileItemReader<MemberEntity> itemReader = new FlatFileItemReaderBuilder<MemberEntity>()
        .name("csvInputOutputReader") // READER NAME 지정
        .encoding("UTF-8") // encoding 방식 지정
        .resource(new ClassPathResource("member-test-input.csv")) // Classpath 내의 test.csv 파일
        .linesToSkip(1) // 첫 1줄은 skip (제목)
        .lineMapper(lineMapper) // lineMapper 지정
        .build();

    itemReader.afterPropertiesSet();

    return itemReader;
  }

  private ItemWriter<MemberEntity> loggingWriter(){
    return items -> {
      String s = items.stream().map(MemberEntity::getName).collect(Collectors.joining(", "));
      log.info(">>>>>>> {}", s);
    };
  }
}
