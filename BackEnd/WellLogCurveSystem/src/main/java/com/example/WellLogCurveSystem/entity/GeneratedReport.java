package com.example.WellLogCurveSystem.entity;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class GeneratedReport {
    private Long id;
    private Long templateId;
    private Integer userId;
    private String reportTitle;
    private String parameters;
    private String content;
    private String documentUrl;
    private String filePath;
    private Integer downloadCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;

}