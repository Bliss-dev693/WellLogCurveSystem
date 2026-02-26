package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.dto.TodoQuery;
import com.example.WellLogCurveSystem.entity.Result;

import com.example.WellLogCurveSystem.entity.TodoStatistics;
import com.example.WellLogCurveSystem.entity.TodoTask;
import com.example.WellLogCurveSystem.service.TodoTaskService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

/**
 * 待办事项控制器
 * 基础路径：/todo
 */
@RestController
@RequestMapping("/todo")
public class TodoTaskController {

    @Resource
    private TodoTaskService todoTaskService;

    /**
     * 1. 获取待办事项列表（新增userId参数）
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getTodoList(
            @RequestParam Integer userId, // 前端传递用户ID（必传）
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize) {

        // 校验用户ID
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }

        // 构建查询参数
        TodoQuery query = new TodoQuery();
        query.setUserId(userId); // 前端传递的用户ID
        query.setStatus(status);
        query.setPriority(priority);
        query.setCategory(category);
        if (pageNum != null) {
            query.setPageNum(pageNum);
        }
        if (pageSize != null) {
            query.setPageSize(pageSize);
        }

        // 查询数据
        Map<String, Object> result = todoTaskService.getTodoList(query);
        return Result.success(result);
    }

    /**
     * 2. 获取待办事项详情（新增userId参数）
     */
    @GetMapping("/{id}")
    public Result<TodoTask> getTodoById(
            @PathVariable Integer id,
            @RequestParam Integer userId) { // 前端传递用户ID

        // 校验参数
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        if (id == null || id <= 0) {
            return Result.error("待办ID不能为空且必须为正整数");
        }

        TodoTask todoTask = todoTaskService.getTodoById(id, userId);
        if (todoTask == null) {
            return Result.error(Result.NOT_FOUND_CODE, "待办事项不存在或不属于该用户");
        }
        return Result.success(todoTask);
    }

    /**
     * 3. 新增待办事项（userId由前端传递到Body）
     */
    @PostMapping("/create")
    public Result<Integer> createTodo(@RequestBody TodoTask todoTask) {
        // 校验参数
        if (todoTask.getUserId() == null || todoTask.getUserId() <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        if (!org.springframework.util.StringUtils.hasText(todoTask.getTitle())) {
            return Result.error("待办标题不能为空");
        }

        // 新增待办（直接使用前端传递的userId）
        Integer id = todoTaskService.createTodo(todoTask);
        return Result.success("新增成功", id);
    }

    /**
     * 4. 修改待办事项（新增userId参数）
     */
    @PutMapping("/update/{id}")
    public Result<String> updateTodo(
            @PathVariable Integer id,
            @RequestParam Integer userId, // 前端传递用户ID
            @RequestBody TodoTask todoTask) {

        // 校验参数
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        if (id == null || id <= 0) {
            return Result.error("待办ID不能为空且必须为正整数");
        }

        boolean success = todoTaskService.updateTodo(id, userId, todoTask);
        if (!success) {
            return Result.error("修改失败，待办事项不存在或不属于该用户");
        }
        return Result.success("修改成功");
    }

    /**
     * 5. 修改待办事项状态（新增userId参数）
     */
    @PutMapping("/updateStatus/{id}")
    public Result<String> updateTodoStatus(
            @PathVariable Integer id,
            @RequestParam Integer userId, // 前端传递用户ID
            @RequestBody Map<String, Integer> params) {

        // 校验参数
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        if (id == null || id <= 0) {
            return Result.error("待办ID不能为空且必须为正整数");
        }

        Integer status = params.get("status");
        if (status == null || (status != 0 && status != 1 && status != 2)) {
            return Result.error("状态值无效，仅支持0-待完成，1-已完成，2-已取消");
        }

        boolean success = todoTaskService.updateTodoStatus(id, userId, status);
        if (!success) {
            return Result.error("状态修改失败，待办事项不存在或不属于该用户");
        }
        return Result.success("状态修改成功");
    }

    /**
     * 6. 删除待办事项（新增userId参数）
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteTodo(
            @PathVariable Integer id,
            @RequestParam Integer userId) { // 前端传递用户ID

        // 校验参数
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        if (id == null || id <= 0) {
            return Result.error("待办ID不能为空且必须为正整数");
        }

        boolean success = todoTaskService.deleteTodo(id, userId);
        if (!success) {
            return Result.error("删除失败，待办事项不存在或不属于该用户");
        }
        return Result.success("删除成功");
    }

    /**
     * 7. 获取待办事项统计（新增userId参数）
     */
    @GetMapping("/statistics")
    public Result<TodoStatistics> getTodoStatistics(@RequestParam Integer userId) {
        // 校验用户ID
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }

        TodoStatistics statistics = todoTaskService.getTodoStatistics(userId);
        return Result.success(statistics);
    }

    /**
     * 8. 获取用户临期待办提醒（新增userId参数）
     */
    @GetMapping("/expireReminder")
    public Result<List<TodoTask>> getExpireReminder(@RequestParam Integer userId) {
        // 校验用户ID
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }

        List<TodoTask> reminderList = todoTaskService.getExpireReminder(userId);
        return Result.success(reminderList);
    }
}