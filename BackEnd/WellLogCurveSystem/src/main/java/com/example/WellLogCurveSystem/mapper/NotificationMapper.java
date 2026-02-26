package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 通知Mapper
 */
@Mapper
public interface NotificationMapper {

    /**
     * 分页查询通知列表
     */
    List<Notification> selectByCondition(Notification notification);

    /**
     * 根据ID查询通知
     */
    @Select("SELECT * FROM notification WHERE id = #{id} AND user_id = #{userId}")
    Notification selectByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);

    /**
     * 更新通知状态（标记已读）
     */
    @Update("UPDATE notification SET status = #{status}, update_time = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("userId") Integer userId);

    /**
     * 批量标记已读
     */
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") String status, @Param("userId") Integer userId);

    /**
     * 根据ID删除通知
     */
    @Delete("DELETE FROM notification WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Integer userId);

    /**
     * 批量删除通知
     */
    int batchDelete(@Param("ids") List<Long> ids, @Param("userId") Integer userId);

    /**
     * 清空用户所有通知
     */
    @Delete("DELETE FROM notification WHERE user_id = #{userId}")
    int deleteAllByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND status = 'unread'")
    int countUnreadByUserId(@Param("userId") Integer userId);
}