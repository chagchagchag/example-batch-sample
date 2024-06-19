package com.example.batch_sample.member.entity.repository;

import com.example.batch_sample.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberEntityRepository extends JpaRepository<MemberEntity, Long> {

}
