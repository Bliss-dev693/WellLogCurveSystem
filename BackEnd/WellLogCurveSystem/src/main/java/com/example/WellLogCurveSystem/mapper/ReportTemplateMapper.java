package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ReportTemplateMapper {

    /**
     * 根据条件查询模板列表
     */
    List<ReportTemplate> selectTemplateList(@Param("keyword") String keyword,
                                            @Param("category") String category,
                                            @Param("userId") Integer userId,
                                            @Param("isPublic") Boolean isPublic);

    /**
     * 根据ID获取模板详情
     */
    ReportTemplate selectById(@Param("id") Long id);

    /**
     * 插入新模板
     */
    int insert(ReportTemplate template);

    /**
     * 更新模板
     */
    int updateById(ReportTemplate template);

    /**
     * 根据ID删除模板（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 增加模板使用次数
     */
    int incrementUsageCount(@Param("id") Long id);

    /**
     * 获取模板使用统计
     */
    List<ReportTemplate> getTemplateUsageStats(@Param("userId") Integer userId);

    /**
     * 统计符合条件的模板数量
     */
    int countTemplates(@Param("keyword") String keyword,
                       @Param("category") String category,
                       @Param("userId") Integer userId,
                       @Param("isPublic") Boolean isPublic);
}