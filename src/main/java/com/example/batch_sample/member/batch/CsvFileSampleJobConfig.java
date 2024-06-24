package com.example.batch_sample.member.batch;

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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CsvFileSampleJobConfig {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final MemberEntityFactory memberEntityFactory;

  private static final int CHUNK_SIZE = 5;
  private static final String jobName = "CSV_FILE_IO_SAMPLE_JOB";

  @Bean
  public Job CsvFileSampleJob() throws Exception {
    return this.jobBuilderFactory.get("CSV_FILE_IO_SAMPLE_JOB")
        .incrementer(new RunIdIncrementer())
        .start(this.csvFileSampleStep())
        .build();
  }

  @Bean
  @JobScope
  public Step csvFileSampleStep() throws Exception {
    return this.stepBuilderFactory.get("CSV_FILE_IO_SAMPLE_STEP")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(csvFileItemReader())
//        .processor(itemProcessor())
        .writer(csvFileItemWriter())
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
        .resource(new ClassPathResource("member-test-input.csv")) // Classpath 내의 test.csv 파일
        .linesToSkip(1) // 첫 1줄은 skip (제목)
        .lineMapper(lineMapper) // lineMapper 지정
        .build();

    itemReader.afterPropertiesSet();

    return itemReader;
  }

  public FlatFileItemWriter<MemberEntity> csvFileItemWriter() throws Exception {
    // fieldExtractor
    // MemberEntity 에 대응되는 POJO 타입에 대응되도록 필드명을 매핑하는 Extractor
    BeanWrapperFieldExtractor<MemberEntity> fieldExtractor = new BeanWrapperFieldExtractor<>();
    fieldExtractor.setNames(new String[]{"id", "name", "email"});

    // lineAggregator
    // delimiter 를 지정해주고, {"id", "name", "email"} 에 대한 fieldExtractor 를 주입 받아서
    // 원하는 필드에 대한 값들을 띄어내는 Aggregator 객체 생성
    DelimitedLineAggregator<MemberEntity> lineAggregator = new DelimitedLineAggregator<>();
    lineAggregator.setDelimiter(",");
    lineAggregator.setFieldExtractor(fieldExtractor);

    // FlatFileItemWriter 객체를 FlatFileItemWriterBuilder 를 통해 생성
    FlatFileItemWriter<MemberEntity> csvFileItemWriter = new FlatFileItemWriterBuilder<MemberEntity>()
        .name("CSV_FILE_ITEM_WRITER")
        .encoding("UTF-8")
        .resource(new FileSystemResource("output/member-test-output.csv"))
        .lineAggregator(lineAggregator)
        .headerCallback(writer -> writer.write("id, 이름, 이메일"))
        .footerCallback(writer -> writer.write("-------"))
        .append(true)
        .build();

    csvFileItemWriter.afterPropertiesSet();

    return csvFileItemWriter;
  }

  public ItemProcessor<MemberEntity, MemberEntity> itemProcessor(){
    return memberEntity -> {
      String newName = memberEntity.getName() + memberEntity.getName();

      MemberEntity newEntity = memberEntityFactory.readFrom(
          memberEntity.getId(),
          newName,
          memberEntity.getEmail()
      );

      return newEntity;
    };
  }

  private ItemWriter<MemberEntity> loggingWriter(){
    return items -> {
      String s = items.stream().map(MemberEntity::getName).collect(Collectors.joining(", "));
      log.info(s);
    };
  }

}
