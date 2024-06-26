package com.example.batch_sample.member.batch.job;

import com.example.batch_sample.global.reader.LinkedListItemReader;
import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import com.example.batch_sample.member.fixtures.MemberEntityFixtures;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
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
public class SampleDataToCsvJdbcJpaBatchJobConfig {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;
  private final EntityManagerFactory entityManagerFactory;
  private final MemberEntityFactory memberEntityFactory;

  private static final String JOB_NAME = "SAMPLE_DATA_TO_CSV_JDBC_JPA_JOB";
  private static final int CHUNK_SIZE = 30;

  @Bean
  public Job sampleDataToCsvJdbcJpaJob() throws Exception{
    return jobBuilderFactory.get(JOB_NAME)
        .incrementer(new RunIdIncrementer())
        .start(readDumpDataStep())
        .next(csvFileReadStep())
        .next(jdbcStep())
        .next(jpaStep())
        .build();
  }

  private Step readDumpDataStep(){
    return this.stepBuilderFactory.get("READ_DUMP_DATA_STEP")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(new LinkedListItemReader<MemberEntity>(MemberEntityFixtures.normalMemberList()))
        .writer(loggingWriter())
        .build();
  }

  private Step csvFileReadStep() throws Exception {
    return stepBuilderFactory.get("CSV_FILE_READ_STEP")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(csvFileItemReader())
        .writer(loggingWriter())
        .build();
  }

  private FlatFileItemReader<MemberEntity> csvFileItemReader() throws Exception {
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
        .name("CSV_FILE_ITEM_READER")
        .encoding("UTF-8")
        .resource(new ClassPathResource("test.csv"))
        .linesToSkip(1)
        .lineMapper(lineMapper)
        .build();
    itemReader.afterPropertiesSet();

    return itemReader;
  }

  private Step jdbcStep() throws Exception {
    return stepBuilderFactory.get(JOB_NAME + "__JDBC_READ_MEMBER_AND_LOGGING")
            .<MemberEntity, MemberEntity>chunk(10)
            .reader(memberEntityItemJdbcReader())
            .writer(loggingWriter())
            .build();
  }

  private ItemReader<MemberEntity> memberEntityItemJdbcReader() throws Exception {
    Map<String, Order> sortKey = new HashMap<>();
    sortKey.put("id", Order.ASCENDING);
    JdbcPagingItemReader<MemberEntity> reader = new JdbcPagingItemReaderBuilder<MemberEntity>()
            .name("MEMBER_ENTITY_ITEM_JDBC_READER")
            .dataSource(dataSource)
            .rowMapper((rs, rowNum) -> memberEntityFactory.readFrom(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3)
            ))
            .pageSize(CHUNK_SIZE)
            .name(JOB_NAME + "_MEMBER_ENTITY_ITEM_JDBC_READER")
            .selectClause("id,name,email")
            .fromClause("member")
            .sortKeys(sortKey)
            .build();

    reader.afterPropertiesSet();
    return reader;
  }

  private Step jpaStep() throws Exception {
    return stepBuilderFactory.get(JOB_NAME + "__JPA_READ_MEMBER_AND_LOGGING")
            .<MemberEntity, MemberEntity>chunk(10)
            .reader(memberEntityItemJpaReader())
            .writer(loggingWriter())
            .build();
  }

  private ItemReader<MemberEntity> memberEntityItemJpaReader() throws Exception {
    return new JpaPagingItemReaderBuilder<MemberEntity>()
            .queryString("select m from MemberEntity m")
            .name("memberEntityItemJpaReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(CHUNK_SIZE)
            .build();
  }

  private ItemWriter<MemberEntity> loggingWriter(){
    return items -> {
      String s = items.stream().map(MemberEntity::getName).collect(Collectors.joining(", "));
      log.info(s);
    };
  }

}
