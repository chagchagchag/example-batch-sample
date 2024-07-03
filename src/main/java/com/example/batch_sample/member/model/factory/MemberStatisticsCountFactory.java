package com.example.batch_sample.member.model.factory;

import com.example.batch_sample.member.model.MemberStatisticsCount;
import org.springframework.stereotype.Component;

@Component
public class MemberStatisticsCountFactory {
  public MemberStatisticsCount newMemberStatisticsCount(String n){
    return MemberStatisticsCount.ofAll(n);
  }
}
