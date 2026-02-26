package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.dto.TodoQuery;
import com.example.WellLogCurveSystem.entity.TodoStatistics;
import com.example.WellLogCurveSystem.entity.TodoTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办事项Mapper
 */
@Mapper
public interface TodoTaskMapper {

    /**
     * 分页查询待办事项列表
     */
    List<TodoTask> selectTodoList(TodoQuery query);

    /**
     * 查询列表总数
     */
    Integer selectTodoCount(TodoQuery query);

    /**
     * 根据ID查询待办事项
     */
    TodoTask selectTodoById(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 新增待办事项
     */
    int insertTodo(TodoTask todoTask);

    /**
     * 修改待办事项
     */
    int updateTodo(TodoTask todoTask);

    /**
     * 修改待办事项状态
     */
    int updateTodoStatus(@Param("id") Integer id,
                         @Param("userId") Integer userId,
                         @Param("status") Integer status,
                         @Param("finishTime") LocalDateTime finishTime,
                         @Param("updateTime") LocalDateTime updateTime);

    /**
     * 删除待办事项
     */
    int deleteTodo(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 统计状态分布
     */
    List<TodoStatistics.StatItem> selectStatusStats(@Param("userId") Integer userId);

    /**
     * 统计优先级分布
     */
    List<TodoStatistics.StatItem> selectPriorityStats(@Param("userId") Integer userId);

    /**
     * 统计分类分布
     */
    List<TodoStatistics.StatItem> selectCategoryStats(@Param("userId") Integer userId);

    /**
     * 查询临期提醒待办（3天内到期）
     */
    List<TodoTask> selectExpireReminder(@Param("userId") Integer userId,
                                        @Param("now") LocalDateTime now,
                                        @Param("expireTime") LocalDateTime expireTime);
}