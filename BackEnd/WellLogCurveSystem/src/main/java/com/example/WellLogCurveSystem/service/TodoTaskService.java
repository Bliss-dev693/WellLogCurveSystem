package com.example.WellLogCurveSystem.service;


import com.example.WellLogCurveSystem.dto.TodoQuery;
import com.example.WellLogCurveSystem.entity.TodoStatistics;
import com.example.WellLogCurveSystem.entity.TodoTask;

import java.util.List;
import java.util.Map;

/**
 * 待办事项业务层接口
 */
public interface TodoTaskService {

    /**
     * 分页查询待办列表
     */
    Map<String, Object> getTodoList(TodoQuery query);

    /**
     * 根据ID查询待办详情
     */
    TodoTask getTodoById(Integer id, Integer userId);

    /**
     * 新增待办
     */
    Integer createTodo(TodoTask todoTask);

    /**
     * 修改待办
     */
    boolean updateTodo(Integer id, Integer userId, TodoTask todoTask);

    /**
     * 修改待办状态
     */
    boolean updateTodoStatus(Integer id, Integer userId, Integer status);

    /**
     * 删除待办
     */
    boolean deleteTodo(Integer id, Integer userId);

    /**
     * 获取待办统计信息
     */
    TodoStatistics getTodoStatistics(Integer userId);

    /**
     * 获取临期提醒待办
     */
    List<TodoTask> getExpireReminder(Integer userId);
}