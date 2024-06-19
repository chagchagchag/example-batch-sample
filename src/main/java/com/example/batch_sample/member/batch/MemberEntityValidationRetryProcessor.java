package com.example.batch_sample.member.batch;

import com.example.batch_sample.global.exception.MemberNameNotExistException;
import com.example.batch_sample.member.entity.MemberEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;

@Slf4j
public class MemberEntityValidationRetryProcessor implements ItemProcessor<MemberEntity, MemberEntity> {
  private final RetryTemplate retryTemplate;

  public MemberEntityValidationRetryProcessor(){
    this.retryTemplate = new RetryTemplateBuilder()
        .maxAttempts(3)
        .retryOn(MemberNameNotExistException.class)
        .withListener(new SaveMemberEntityRetryListener())
        .build();
  }

  @Override
  public MemberEntity process(MemberEntity memberEntity) throws Exception {
    return retryTemplate.execute(context -> {
      if(memberEntity.isNotEmptyName()) return memberEntity;
      throw new MemberNameNotExistException();
    });
  }

  public static class SaveMemberEntityRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext retryContext,
        RetryCallback<T, E> retryCallback) {
      return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext retryContext,
        RetryCallback<T, E> retryCallback, Throwable throwable) {
      log.info(" >>> close <<< ");
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext retryContext,
        RetryCallback<T, E> retryCallback, Throwable throwable) {
      log.info(" >>> onError <<< ");
    }
  }
}
