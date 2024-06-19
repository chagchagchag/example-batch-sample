package com.example.batch_sample.member.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "ofAll")
@NoArgsConstructor
@Getter
@Entity
public class MemberEntity {
  @Id @GeneratedValue
  private Long id;
  private String name;
  private String email;
}
