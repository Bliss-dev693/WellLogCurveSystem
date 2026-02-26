package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import java.util.List;

/**
 * 待办事项统计结果
 */
@Data
public class TodoStatistics {
    /**
     * 状态统计
     */
    private List<StatItem> statusStats;

    /**
     * 优先级统计
     */
    private List<StatItem> priorityStats;

    /**
     * 分类统计
     */
    private List<StatItem> categoryStats;

    /**
     * 统计子项
     */
    @Data
    public static class StatItem {
        /**
         * 状态/优先级/分类值
         */
        private Object key;

        /**
         * 文本描述
         */
        private String text;

        /**
         * 数量
         */
        private Integer count;
    }
}
