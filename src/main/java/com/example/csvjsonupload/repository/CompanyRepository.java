package com.example.csvjsonupload.repository;

import com.example.csvjsonupload.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Company findByCompanyId(String companyId);
}
