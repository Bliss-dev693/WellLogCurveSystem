package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportTemplate {
    private Long id;
    private Integer userId;
    private String name;
    private String description;
    private String category;
    private String documentUrl;
    private String variables;
    private Boolean isPublic;
    private Integer usageCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}