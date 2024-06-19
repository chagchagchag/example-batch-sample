package com.example.batch_sample.member.enums;

import lombok.Getter;

@Getter
public enum MemberEntitySteps {
  SAVE_MEMBER_ENTITY_STEP("SAVE_MEMBER_ENTITY_STEP");
  private final String name;
  MemberEntitySteps(String name){
    this.name = name;
  }
}
