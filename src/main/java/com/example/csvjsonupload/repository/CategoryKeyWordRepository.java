package com.example.csvjsonupload.repository;

import com.example.csvjsonupload.entity.CategoryKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryKeyWordRepository extends JpaRepository<CategoryKeyword, Long> {
    List<CategoryKeyword> findByKeywordContaining(String keywordPart);
    List<CategoryKeyword> findAll();
}
