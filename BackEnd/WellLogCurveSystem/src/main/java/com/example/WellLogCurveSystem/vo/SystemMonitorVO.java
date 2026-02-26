package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.util.List;

/**
 * 系统监控数据VO（前端可视化专用）
 */
@Data
public class SystemMonitorVO {
    /** 系统基础信息 */
    private SystemInfo systemInfo;
    /** JVM运行信息 */
    private JvmInfo jvmInfo;
    /** CPU使用信息 */
    private CpuInfo cpuInfo;
    /** 内存使用信息 */
    private MemoryInfo memoryInfo;
    /** 磁盘使用信息 */
    private List<DiskInfo> diskInfoList;

    // 系统基础信息内部类
    @Data
    public static class SystemInfo {
        private String osName;        // 操作系统名称（如Windows 10/Linux）
        private String osArch;        // 系统架构（如amd64）
        private String javaVersion;   // Java版本（如17.0.8）
        private long startTime;       // 应用启动时间戳
        private String upTime;        // 应用运行时长（如1天2小时）
    }

    // JVM信息内部类
    @Data
    public static class JvmInfo {
        private int threadCount;      // 当前线程数
        private int daemonThreadCount;// 守护线程数
        private int gcCount;          // GC总次数
        private long gcTime;          // GC总耗时(ms)
    }

    // CPU信息内部类
    @Data
    public static class CpuInfo {
        private int cpuCoreNum;       // CPU核心数
        private double cpuUsage;      // CPU使用率(%)
    }

    // 内存信息内部类
    @Data
    public static class MemoryInfo {
        private double heapUsedMB;    // 堆内存已用(MB)
        private double heapMaxMB;     // 堆内存最大值(MB)
        private double nonHeapUsedMB; // 非堆内存已用(MB)
        private double heapUsage;     // 堆内存使用率(%)
    }

    // 磁盘信息内部类
    @Data
    public static class DiskInfo {
        private String diskPath;      // 磁盘路径（如C:/、/）
        private double totalSpaceGB;  // 总空间(GB)
        private double usedSpaceGB;   // 已用空间(GB)
        private double usage;         // 磁盘使用率(%)
    }
}