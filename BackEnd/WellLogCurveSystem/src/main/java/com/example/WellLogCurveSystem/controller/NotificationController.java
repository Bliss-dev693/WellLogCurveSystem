package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.dto.NotificationQuery;
import com.example.WellLogCurveSystem.entity.Notification;
import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.NotificationService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知管理控制器（修改后：接收前端传递的userId）
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    /**
     * 获取通知列表（新增接收userId参数）
     */
    @GetMapping
    public Result<Map<String, Object>> getNotificationList(
            NotificationQuery queryDTO,
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId // 前端传递的用户ID
    ) {
        // 设置前端传递的用户ID
        queryDTO.setUserId(userId);

        // 查询分页数据
        PageInfo<Notification> pageInfo = notificationService.getNotificationList(queryDTO);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageInfo.getList());
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());

        return Result.success(data);
    }

    /**
     * 获取通知详情（新增userId参数）
     */
    @GetMapping("/{id}")
    public Result<Notification> getNotificationDetail(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId // 前端传递的用户ID
    ) {
        Notification notification = notificationService.getNotificationDetail(id, userId);

        if (notification == null) {
            return Result.error(Result.NOT_FOUND_CODE, "通知不存在");
        }
        return Result.success(notification);
    }

    /**
     * 标记通知为已读（新增userId参数）
     */
    @PatchMapping("/{id}/read")
    public Result<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId, // 前端传递的用户ID
            @RequestBody Map<String, String> request
    ) {
        boolean success = notificationService.markAsRead(id, userId);

        if (!success) {
            return Result.error("标记已读失败，通知不存在");
        }

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("status", "read");
        data.put("updateTime", System.currentTimeMillis()); // 实际应从数据库查询

        return Result.success("标记已读成功", data);
    }

    /**
     * 批量标记已读（userId放在请求体中）
     */
    @PatchMapping("/batch-read")
    public Result<Map<String, Object>> batchMarkAsRead(@RequestBody Map<String, Object> request) {
        // 从请求体获取参数
        List<Long> ids = (List<Long>) request.get("ids");
        Integer userId = (Integer) request.get("userId");

        // 参数校验
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要标记的通知");
        }

        int successCount = notificationService.batchMarkAsRead(ids, userId);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("failedIds", List.of()); // 简化处理，实际可返回失败的ID

        return Result.success("批量标记已读成功", data);
    }

    /**
     * 删除通知（新增userId参数）
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteNotification(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId // 前端传递的用户ID
    ) {
        boolean success = notificationService.deleteNotification(id, userId);

        if (!success) {
            return Result.error("删除失败，通知不存在");
        }
        return Result.success("删除成功");
    }

    /**
     * 批量删除通知（userId放在请求体中）
     */
    @DeleteMapping("/batch-delete")
    public Result<Map<String, Object>> batchDeleteNotifications(@RequestBody Map<String, Object> request) {
        // 从请求体获取参数
        List<Long> ids = (List<Long>) request.get("ids");
        Integer userId = (Integer) request.get("userId");

        // 参数校验
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        if (ids == null || ids.isEmpty()) {
            return Result.error("请选择要删除的通知");
        }

        int successCount = notificationService.batchDeleteNotifications(ids, userId);

        // 构建响应数据
        Map<String, Object> data = new HashMap<>();
        data.put("successCount", successCount);
        data.put("failedIds", List.of());

        return Result.success("批量删除成功", data);
    }

    /**
     * 清空所有通知（新增userId参数）
     */
    @DeleteMapping("/clear-all")
    public Result<String> clearAllNotifications(
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId // 前端传递的用户ID
    ) {
        boolean success = notificationService.clearAllNotifications(userId);

        if (!success) {
            return Result.error("清空通知失败");
        }
        return Result.success("清空成功");
    }

    /**
     * 获取未读通知数量（新增userId参数）
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount(
            @RequestParam @NotNull(message = "用户ID不能为空") Integer userId // 前端传递的用户ID
    ) {
        int unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("unreadCount", unreadCount);

        return Result.success(data);
    }
}