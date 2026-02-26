package com.example.WellLogCurveSystem.service.impl;


import com.example.WellLogCurveSystem.dto.TodoQuery;
import com.example.WellLogCurveSystem.entity.TodoStatistics;
import com.example.WellLogCurveSystem.entity.TodoTask;
import com.example.WellLogCurveSystem.mapper.TodoTaskMapper;
import com.example.WellLogCurveSystem.service.TodoTaskService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 待办事项业务层实现
 */
@Service
public class TodoTaskServiceImpl implements TodoTaskService {

    @Resource
    private TodoTaskMapper todoTaskMapper;

    /**
     * 分页查询待办列表
     */
    @Override
    public Map<String, Object> getTodoList(TodoQuery query) {
        // 查询列表数据
        List<TodoTask> todoList = todoTaskMapper.selectTodoList(query);
        // 初始化状态/优先级文本
        todoList.forEach(TodoTask::initText);

        // 查询总数
        Integer total = todoTaskMapper.selectTodoCount(query);

        // 封装结果
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", todoList);
        return result;
    }

    /**
     * 根据ID查询待办详情
     */
    @Override
    public TodoTask getTodoById(Integer id, Integer userId) {
        if (id == null || userId == null) {
            return null;
        }
        TodoTask todoTask = todoTaskMapper.selectTodoById(id, userId);
        if (todoTask != null) {
            todoTask.initText();
        }
        return todoTask;
    }

    /**
     * 新增待办
     */
    @Override
    public Integer createTodo(TodoTask todoTask) {
        // 设置默认值
        if (todoTask.getStatus() == null) {
            todoTask.setStatus(0); // 默认待完成
        }
        if (todoTask.getPriority() == null) {
            todoTask.setPriority(1); // 默认中优先级
        }
        if (todoTask.getCreateTime() == null) {
            todoTask.setCreateTime(LocalDateTime.now());
        }
        if (todoTask.getUpdateTime() == null) {
            todoTask.setUpdateTime(LocalDateTime.now());
        }

        // 插入数据
        todoTaskMapper.insertTodo(todoTask);
        return todoTask.getId();
    }

    /**
     * 修改待办
     */
    @Override
    public boolean updateTodo(Integer id, Integer userId, TodoTask todoTask) {
        if (id == null || userId == null) {
            return false;
        }

        // 设置修改时间
        todoTask.setId(id);
        todoTask.setUserId(userId);
        todoTask.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int rows = todoTaskMapper.updateTodo(todoTask);
        return rows > 0;
    }

    /**
     * 修改待办状态
     */
    @Override
    public boolean updateTodoStatus(Integer id, Integer userId, Integer status) {
        if (id == null || userId == null || status == null) {
            return false;
        }

        // 处理完成时间
        LocalDateTime finishTime = null;
        if (status == 1) { // 已完成
            finishTime = LocalDateTime.now();
        }

        // 执行更新
        int rows = todoTaskMapper.updateTodoStatus(
                id, userId, status, finishTime, LocalDateTime.now()
        );
        return rows > 0;
    }

    /**
     * 删除待办
     */
    @Override
    public boolean deleteTodo(Integer id, Integer userId) {
        if (id == null || userId == null) {
            return false;
        }
        int rows = todoTaskMapper.deleteTodo(id, userId);
        return rows > 0;
    }

    /**
     * 获取待办统计信息
     */
    @Override
    public TodoStatistics getTodoStatistics(Integer userId) {
        if (userId == null) {
            return null;
        }

        TodoStatistics statistics = new TodoStatistics();
        // 状态统计
        statistics.setStatusStats(todoTaskMapper.selectStatusStats(userId));
        // 优先级统计
        statistics.setPriorityStats(todoTaskMapper.selectPriorityStats(userId));
        // 分类统计
        statistics.setCategoryStats(todoTaskMapper.selectCategoryStats(userId));

        return statistics;
    }

    /**
     * 获取临期提醒待办（3天内到期）
     */
    @Override
    public List<TodoTask> getExpireReminder(Integer userId) {
        if (userId == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = now.plusDays(3);

        List<TodoTask> reminderList = todoTaskMapper.selectExpireReminder(userId, now, expireTime);
        // 初始化优先级文本
        reminderList.forEach(todo -> {
            if (todo.getPriority() == 0) {
                todo.setPriorityText("低");
            } else if (todo.getPriority() == 1) {
                todo.setPriorityText("中");
            } else if (todo.getPriority() == 2) {
                todo.setPriorityText("高");
            }
        });

        return reminderList;
    }
}