package com.example.batch_sample.member;

import com.example.batch_sample.MyTestConfiguration;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"spring.profiles.active=h2"})
@SpringBatchTest
@ContextConfiguration(
  classes = {
      MyTestConfiguration.class
  }
)
public class MemberConfigurationTest {

}
