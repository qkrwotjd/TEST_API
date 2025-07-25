package com.example.csvjsonupload.controller;

import com.example.csvjsonupload.entity.Category;
import com.example.csvjsonupload.entity.CategoryKeyword;
import com.example.csvjsonupload.entity.Company;
import com.example.csvjsonupload.entity.TransactionResult;
import com.example.csvjsonupload.repository.CategoryKeyWordRepository;
import com.example.csvjsonupload.repository.CategoryRepository;
import com.example.csvjsonupload.repository.CompanyRepository;
import com.example.csvjsonupload.repository.TransactionResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounting/process")
@RequiredArgsConstructor
public class UploadController {
    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryKeyWordRepository categoryKeyWordRepository;
    private final TransactionResultRepository transactionResultRepository;

    @PostMapping
    public ResponseEntity<String> handleUpload(
            @RequestParam("csvFile") MultipartFile csvFile,
            @RequestParam("jsonFile") MultipartFile jsonFile) {

        try {
            // json 파일 읽고 DB에 저장하기
            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(jsonFile.getInputStream());
            List<Company> companyList = new ArrayList<>();
            List<Category> categoryList = new ArrayList<>();

            Category unknownCategory = new Category();
            unknownCategory.setCompanyId("DEFAULT");
            unknownCategory.setCategoryId("UNKNOWN");
            unknownCategory.setCategoryName("미분류");
            unknownCategory.setKeywords(new ArrayList<>());
            categoryList.add(unknownCategory);

            for (JsonNode node : root.get("companies")) {
                Company company = new Company();
                company.setCompanyId(node.get("company_id").asText());
                company.setCompanyName(node.get("company_name").asText());
                companyList.add(company);

                JsonNode categoriesNode = node.get("categories");
                for (JsonNode categoryNode : categoriesNode) {
                    Category category = new Category();
                    category.setCompanyId(node.get("company_id").asText());
                    category.setCategoryId(categoryNode.get("category_id").asText());
                    category.setCategoryName(categoryNode.get("category_name").asText());

                    List<CategoryKeyword> keywords = new ArrayList<>();
                    for (JsonNode keywordNode : categoryNode.get("keywords")) {
                        CategoryKeyword keyword = new CategoryKeyword();
                        keyword.setKeyword(keywordNode.asText());
                        keyword.setCategory(category); // 연관관계 설정
                        keywords.add(keyword);
                    }

                    category.setKeywords(keywords);
                    categoryList.add(category);
                }
            }

            companyRepository.saveAll(companyList);
            categoryRepository.saveAll(categoryList);

            // csv 파일 읽고 DB에 저장하기
            BufferedReader reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), "UTF-8"));
            String line;
            boolean firstLine = true;

            List<TransactionResult> transactionResultList = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // 헤더 스킵
                }

                String[] parts = line.split(",");
                if (parts.length != 6) continue;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm");
                LocalDateTime trDate = LocalDateTime.parse(parts[0].trim(), formatter);
                String findKeyword = parts[1].trim();
                Long depositAmt = Long.parseLong(parts[2].trim());
                Long withDrawAmt = Long.parseLong(parts[3].trim());
                Long balance = Long.parseLong(parts[4].trim());
                String branch = parts[5].trim();

                List<CategoryKeyword> categoryKeywordList = categoryKeyWordRepository.findAll();

                List<CategoryKeyword> filteredList = categoryKeywordList.stream()
                        .filter(ck -> findKeyword.contains(ck.getKeyword()))
                        .collect(Collectors.toList());

                TransactionResult transactionResult = new TransactionResult();
                // 미분류
                if(filteredList.size() == 0) {
                    Category category = categoryRepository.findByCategoryId("UNKNOWN");
                    transactionResult.setCompanyId(category.getCompanyId());
                    transactionResult.setCategoryId(category.getCategoryId());
                    transactionResult.setCompanyName("");
                    transactionResult.setCategoryName(category.getCategoryName());
                    transactionResult.setKeyword(findKeyword);
                    transactionResult.setDepositAmt(depositAmt);
                    transactionResult.setWithdrawAmt(withDrawAmt);
                    transactionResult.setBalance(balance);
                    transactionResult.setBranch(branch);
                    transactionResult.setTrDate(trDate);
                    transactionResultList.add(transactionResult);
                } else {
                    for(CategoryKeyword categoryKeyword : filteredList) {
                        Category category = categoryKeyword.getCategory();
                        transactionResult.setCompanyId(category.getCompanyId());
                        transactionResult.setCategoryId(category.getCategoryId());
                        Company company = companyRepository.findByCompanyId(category.getCompanyId());
                        if(company != null) transactionResult.setCompanyName(company.getCompanyName());
                        transactionResult.setCategoryName(category.getCategoryName());
                        transactionResult.setKeyword(findKeyword);
                        transactionResult.setDepositAmt(depositAmt);
                        transactionResult.setWithdrawAmt(withDrawAmt);
                        transactionResult.setBalance(balance);
                        transactionResult.setBranch(branch);
                        transactionResult.setTrDate(trDate);
                    }
                    transactionResultList.add(transactionResult);
                }
            }

            transactionResultRepository.saveAll(transactionResultList);

            return ResponseEntity.ok("Files received successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing files: " + e.getMessage());
        }
    }

    public String normalize(String text) {
        if (text == null) return "";
        return text
                .replaceAll("\\(주\\)|㈜|주식회사", "")  // 회사 접두어 제거
                .replaceAll("[^가-힣a-zA-Z0-9]", "")  // 특수문자 제거
                .toLowerCase()                        // 소문자 변환 (비교용)
                .trim();                              // 공백 제거
    }
}
