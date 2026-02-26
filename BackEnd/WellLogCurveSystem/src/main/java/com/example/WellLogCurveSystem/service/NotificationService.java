package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.dto.NotificationQuery;
import com.example.WellLogCurveSystem.entity.Notification;
import com.example.WellLogCurveSystem.mapper.NotificationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


import java.util.List;

/**
 * 通知服务类
 */
@Service
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    /**
     * 分页查询通知列表
     */
    public PageInfo<Notification> getNotificationList(NotificationQuery queryDTO) {
        // 构建查询条件
        Notification notification = new Notification();
        notification.setUserId(queryDTO.getUserId());
        notification.setType(queryDTO.getType());
        notification.setStatus(queryDTO.getStatus());

        // 分页查询
        PageHelper.startPage(queryDTO.getPageNum(), queryDTO.getPageSize());
        List<Notification> list = notificationMapper.selectByCondition(notification);
        return new PageInfo<>(list);
    }

    /**
     * 根据ID查询通知详情
     */
    public Notification getNotificationDetail(Long id, Integer userId) {
        return notificationMapper.selectByIdAndUserId(id, userId);
    }

    /**
     * 标记通知为已读
     */
    public boolean markAsRead(Long id, Integer userId) {
        int affected = notificationMapper.updateStatus(id, "read", userId);
        return affected > 0;
    }

    /**
     * 批量标记已读
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(List<Long> ids, Integer userId) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return notificationMapper.batchUpdateStatus(ids, "read", userId);
    }

    /**
     * 删除通知
     */
    public boolean deleteNotification(Long id, Integer userId) {
        int affected = notificationMapper.deleteByIdAndUserId(id, userId);
        return affected > 0;
    }

    /**
     * 批量删除通知
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteNotifications(List<Long> ids, Integer userId) {
        if (CollectionUtils.isEmpty(ids)) {
            return 0;
        }
        return notificationMapper.batchDelete(ids, userId);
    }

    /**
     * 清空所有通知
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean clearAllNotifications(Integer userId) {
        int affected = notificationMapper.deleteAllByUserId(userId);
        return affected >= 0; // 即使没有通知也返回成功
    }

    /**
     * 获取未读通知数量
     */
    public int getUnreadCount(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
}