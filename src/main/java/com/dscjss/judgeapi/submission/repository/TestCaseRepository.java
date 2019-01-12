package com.dscjss.judgeapi.submission.repository;

import com.dscjss.judgeapi.submission.model.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {
}
