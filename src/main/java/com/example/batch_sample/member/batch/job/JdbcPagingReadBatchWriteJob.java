package com.example.batch_sample.member.batch.job;

import com.example.batch_sample.member.entity.MemberEntity;
import com.example.batch_sample.member.entity.factory.MemberEntityFactory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JdbcPagingReadBatchWriteJob {
  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;
  private final MemberEntityFactory memberEntityFactory;

  private static final int CHUNK_SIZE = 30;
  @Bean
  public Job jdbcPagingReadBatchWriteJob() throws Exception {
    return this.jobBuilderFactory.get("JOB__jdbcPagingReadBatchWriteJob")
        .incrementer(new RunIdIncrementer())
        .start(this.jdbcPagingReadBatchWriteStep())
        .build();
  }

  @Bean
  @JobScope
  public Step jdbcPagingReadBatchWriteStep() throws Exception {
    return this.stepBuilderFactory.get("STEP__jdbcPagingReadBatchWriteStep")
        .<MemberEntity, MemberEntity>chunk(CHUNK_SIZE)
        .reader(memberEntityItemReader())
//        .processor()
        .writer(jdbcBatchItemWriter())
        .build();
  }

  private ItemReader<? extends MemberEntity> memberEntityItemReader() throws Exception {
    Map<String, Order> sortKey = new HashMap<>();
    sortKey.put("created_date", Order.ASCENDING);

    // Statistics 말고 그냥 이름에 1111 더해서 출력하는 예제로 변경
    JdbcPagingItemReader<MemberEntity> jdbcPagingItemReader = new JdbcPagingItemReaderBuilder<MemberEntity>()
        .dataSource(dataSource)
        .rowMapper((rs, i) -> memberEntityFactory
            .readFrom(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3)
            )
        )
        .pageSize(CHUNK_SIZE)
        .name("JOB__jdbcPagingReadBatchWriteJob")
        .selectClause("id, name, email")
        .fromClause("member")
        .sortKeys(sortKey)
        .build();

    jdbcPagingItemReader.afterPropertiesSet();

    return jdbcPagingItemReader;
  }

  private ItemWriter<MemberEntity> jdbcBatchItemWriter(){
    JdbcBatchItemWriter<MemberEntity> itemWriter = new JdbcBatchItemWriterBuilder<MemberEntity>()
        .dataSource(dataSource)
        .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
        .sql("insert into member_duplicated(id, name, email) values(:id, :name, :email")
        .build();

    itemWriter.afterPropertiesSet();

    return itemWriter;
  }
}
