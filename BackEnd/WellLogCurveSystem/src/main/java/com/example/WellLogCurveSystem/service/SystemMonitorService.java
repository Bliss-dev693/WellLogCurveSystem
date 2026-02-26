package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.vo.SystemMonitorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统监控核心服务
 */
@Slf4j
@Service
public class SystemMonitorService {

    // 应用启动时间（初始化后固定）
    private final long startTime = System.currentTimeMillis();

    /**
     * 获取完整的系统监控数据
     */
    public Result<SystemMonitorVO> getSystemMonitorData() {
        try {
            SystemMonitorVO vo = new SystemMonitorVO();
            vo.setSystemInfo(getSystemInfo());
            vo.setJvmInfo(getJvmInfo());
            vo.setCpuInfo(getCpuInfo());
            vo.setMemoryInfo(getMemoryInfo());
            vo.setDiskInfoList(getDiskInfoList());
            
            log.info("系统监控数据采集完成");
            return Result.success("获取系统监控数据成功", vo);
        } catch (Exception e) {
            log.error("获取系统监控数据失败", e);
            return Result.error("获取系统监控数据失败: " + e.getMessage());
        }
    }

    // 获取系统基础信息
    private SystemMonitorVO.SystemInfo getSystemInfo() {
        SystemMonitorVO.SystemInfo info = new SystemMonitorVO.SystemInfo();
        info.setOsName(System.getProperty("os.name"));
        info.setOsArch(System.getProperty("os.arch"));
        info.setJavaVersion(System.getProperty("java.version"));
        info.setStartTime(startTime);
        
        // 计算应用运行时长
        long upTimeMs = System.currentTimeMillis() - startTime;
        long days = TimeUnit.MILLISECONDS.toDays(upTimeMs);
        long hours = TimeUnit.MILLISECONDS.toHours(upTimeMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTimeMs) % 60;
        info.setUpTime(String.format("%d天 %d小时 %d分钟", days, hours, minutes));
        return info;
    }

    // 获取JVM信息
    private SystemMonitorVO.JvmInfo getJvmInfo() {
        SystemMonitorVO.JvmInfo info = new SystemMonitorVO.JvmInfo();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        info.setThreadCount(threadBean.getThreadCount());
        info.setDaemonThreadCount(threadBean.getDaemonThreadCount());

        // 统计GC信息
        long gcCount = 0;
        long gcTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcCount += gcBean.getCollectionCount();
            gcTime += gcBean.getCollectionTime();
        }
        info.setGcCount((int) gcCount);
        info.setGcTime(gcTime);
        return info;
    }

    // 获取CPU信息
    private SystemMonitorVO.CpuInfo getCpuInfo() {
        SystemMonitorVO.CpuInfo info = new SystemMonitorVO.CpuInfo();
        // CPU核心数
        info.setCpuCoreNum(Runtime.getRuntime().availableProcessors());
        // CPU使用率（Sun/Oracle/OpenJDK专用）
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        // 保留2位小数
        double cpuUsage = new BigDecimal(osBean.getSystemCpuLoad() * 100)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
        info.setCpuUsage(cpuUsage);
        return info;
    }

    // 获取内存信息
    private SystemMonitorVO.MemoryInfo getMemoryInfo() {
        SystemMonitorVO.MemoryInfo info = new SystemMonitorVO.MemoryInfo();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        
        // 堆内存计算（转换为MB）
        info.setHeapUsedMB(convertToMB(heapUsage.getUsed()));
        info.setHeapMaxMB(convertToMB(heapUsage.getMax()));
        // 堆内存使用率
        double heapUsageRate = heapUsage.getMax() == 0 ? 0 : 
                new BigDecimal((double) heapUsage.getUsed() / heapUsage.getMax() * 100)
                        .setScale(2, RoundingMode.HALF_UP).doubleValue();
        info.setHeapUsage(heapUsageRate);
        
        // 非堆内存
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        info.setNonHeapUsedMB(convertToMB(nonHeapUsage.getUsed()));
        return info;
    }

    // 获取磁盘信息
    private List<SystemMonitorVO.DiskInfo> getDiskInfoList() {
        List<SystemMonitorVO.DiskInfo> diskList = new ArrayList<>();
        for (File root : File.listRoots()) {
            try {
                SystemMonitorVO.DiskInfo disk = new SystemMonitorVO.DiskInfo();
                disk.setDiskPath(root.getPath());
                disk.setTotalSpaceGB(convertToGB(root.getTotalSpace()));
                disk.setUsedSpaceGB(convertToGB(root.getTotalSpace() - root.getFreeSpace()));
                // 磁盘使用率
                double usage = root.getTotalSpace() == 0 ? 0 :
                        new BigDecimal((double) (root.getTotalSpace() - root.getFreeSpace()) / root.getTotalSpace() * 100)
                                .setScale(2, RoundingMode.HALF_UP).doubleValue();
                disk.setUsage(usage);
                diskList.add(disk);
            } catch (Exception e) {
                log.warn("获取磁盘{}信息失败", root.getPath(), e);
            }
        }
        return diskList;
    }

    // 字节转换为MB
    private double convertToMB(long bytes) {
        return new BigDecimal((double) bytes / (1024 * 1024))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // 字节转换为GB
    private double convertToGB(long bytes) {
        return new BigDecimal((double) bytes / (1024 * 1024 * 1024))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}