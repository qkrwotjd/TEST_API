package com.example.csvjsonupload.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Data
public class CategoryPk implements Serializable {
    private String companyId;
    private String categoryId;
}
