package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;       // 总记录数
    private Integer pageNum;  // 当前页码
    private Integer pageSize; // 每页条数
    private List<T> records;  // 当前页数据
    private Integer totalPages; // 总页数

    public PageResult(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.records = records;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }
}