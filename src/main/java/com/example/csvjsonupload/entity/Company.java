package com.example.csvjsonupload.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class Company {
    @Id
    @Column(name = "company_id")
    @JsonProperty("company_id")
    private String companyId;

    @Column(name = "company_name")
    @JsonProperty("company_name")
    private String companyName;
}
