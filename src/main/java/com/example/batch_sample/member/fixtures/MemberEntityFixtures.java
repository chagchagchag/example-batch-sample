package com.example.batch_sample.member.fixtures;

import com.example.batch_sample.member.entity.MemberEntity;
import java.util.ArrayList;
import java.util.List;

public class MemberEntityFixtures {
  public static List<MemberEntity> normalMemberList(){
    List<MemberEntity> members = new ArrayList<>();

    for(int i=0; i<100; i++){
      String name = "member" + i;
      members.add(MemberEntity.ofAll(Long.parseLong(String.valueOf(i)), name, name+"@gmail.com"));
    }

    return members;
  }
}
