package com.example.batch_sample.member.entity.factory;

import com.example.batch_sample.member.entity.MemberEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberEntityFactory {

  public MemberEntity newMember(String name, String email){
    return MemberEntity.ofAll(
      null, name, email
    );
  }
}
