package com.example.csvjsonupload.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class TransactionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seq_no")
    @JsonProperty("seq_no")
    private Long seqNo;

    @Column(name = "company_id")
    @JsonProperty("company_id")
    private String companyId;

    @Column(name = "category_id")
    @JsonProperty("category_id")
    private String categoryId;

    @Column(name = "company_name")
    @JsonProperty("company_name")
    private String companyName;

    @Column(name = "category_name")
    @JsonProperty("category_name")
    private String categoryName;

    @Column(name = "keyword")
    @JsonProperty("keyword")
    private String keyword;

    @Column(name = "deposit_amt")
    @JsonProperty("deposit_amt")
    private Long depositAmt;

    @Column(name = "withdraw_amt")
    @JsonProperty("withdraw_amt")
    private Long withdrawAmt;

    @Column(name = "balance")
    @JsonProperty("balance")
    private Long balance;

    @Column(name = "tr_dt")
    @JsonProperty("tr_dt")
    private LocalDateTime trDate;

    @Column(name = "branch")
    @JsonProperty("branch")
    private String branch;
}
