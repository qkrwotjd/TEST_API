package com.example.csvjsonupload.controller;

import com.example.csvjsonupload.ResponseUtils;
import com.example.csvjsonupload.entity.TransactionResult;
import com.example.csvjsonupload.repository.TransactionResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/accounting/records")
@RequiredArgsConstructor
public class SelectController {
    private final TransactionResultRepository transactionResultRepository;

    @GetMapping
    public Map<String, Object> getTransactionResultData(@RequestParam("companyId") String companyId) {
            List<TransactionResult> resultList = transactionResultRepository.findByCompanyId(companyId);
            Map<String, Object> dataMap = new HashMap<>();
            return ResponseUtils.response(dataMap, resultList);
    }
}
