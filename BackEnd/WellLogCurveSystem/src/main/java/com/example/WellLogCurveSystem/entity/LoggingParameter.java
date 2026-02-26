package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 单时间步的测井参数模型
 */
@Data
public class LoggingParameter {
    @NotNull(message = "AC参数不能为空")
    private BigDecimal AC; // 声波时差

    @NotNull(message = "GR参数不能为空")
    private BigDecimal GR; // 自然伽马

    @NotNull(message = "RT参数不能为空")
    private BigDecimal RT; // 深电阻率

    @NotNull(message = "RXO参数不能为空")
    private BigDecimal RXO; // 浅电阻率
}