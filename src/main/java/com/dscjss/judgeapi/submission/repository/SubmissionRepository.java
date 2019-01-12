package com.dscjss.judgeapi.submission.repository;

import com.dscjss.judgeapi.submission.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {


}
