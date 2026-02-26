package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.GeneratedReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GeneratedReportMapper {

    /**
     * 插入新报告
     */
    int insert(GeneratedReport report);

    /**
     * 根据ID查询报告
     */
    GeneratedReport selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询报告列表
     */
    List<GeneratedReport> selectByUserId(@Param("userId") Integer userId);

    /**
     * 增加下载次数
     */
    int incrementDownloadCount(@Param("id") Long id);

    /**
     * 更新报告状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}