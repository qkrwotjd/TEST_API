package com.example.csvjsonupload.repository;

import com.example.csvjsonupload.entity.TransactionResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionResultRepository extends JpaRepository<TransactionResult, Long> {
    List<TransactionResult> findByCompanyId(String companyId);
}
