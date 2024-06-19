package com.example.batch_sample.member.entity;

import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "ofAll")
@NoArgsConstructor
@Getter
@Entity
public class MemberEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String email;

  public boolean isNotEmptyName(){
    return Objects.nonNull(name) && !name.isEmpty();
  }

  public MemberEntity undefinedName(){
    this.name = "UNDEFINED";
    return this;
  }
}
