package com.example.WellLogCurveSystem.dto;


import lombok.Data;

@Data
public class TemplateQueryDTO {
    private String keyword;
    private String category;
    private Integer userId;
    private Boolean isPublic;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}