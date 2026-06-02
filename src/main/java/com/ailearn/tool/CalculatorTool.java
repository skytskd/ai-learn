package com.ailearn.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * <h1>🧮 数学计算器工具</h1>
 *
 * <p>演示一个更简单的工具——执行数学运算。
 * 这个工具很简单，但很好地展示了：AI 如何识别"需要计算"的场景
 * 并自动调用正确的工具。</p>
 *
 * <h2>学习要点</h2>
 * <p>尝试问这些问题，观察 AI 是否会调用 CalculatorTool：</p>
 * <ol>
 *   <li>"123 + 456 等于多少？" → 应该调用</li>
 *   <li>"告诉我一个关于数学的笑话" → 不应该调用</li>
 *   <li>"帮我算一下 100*99 + 98/2" → 表达式拆分</li>
 * </ol>
 *
 * <h2>面试题：为什么大模型自己不会算数还要用工具？</h2>
 * <p>LLM 是语言模型，不是计算器。它的"计算"本质上是基于语言模式的预测，
 * 对于复杂计算（大数、多步骤），容易出错。通过 Tool 调用真正的计算引擎，
 * 可以确保 100% 的计算精度。</p>
 */
@Component
public class CalculatorTool {

    private static final Logger log = LoggerFactory.getLogger(CalculatorTool.class);

    /**
     * 执行数学运算表达式
     *
     * <p><b>@ToolParam 中的 require=True：</b></p>
     * <p>表示这个参数是<b>必填</b>的。如果 AI 无法提供，
     * 它会向用户追问。如果设为 false，AI 可能传 null。</p>
     *
     * <h3>支持的运算：</h3>
     * <ul>
     *   <li>加法：a + b</li>
     *   <li>减法：a - b</li>
     *   <li>乘法：a * b</li>
     *   <li>除法：a / b（保留 4 位小数）</li>
     * </ul>
     *
     * @param expression 数学表达式，如 "123 * 456"
     * @return 计算结果字符串
     */
    @Tool(description = """
            执行数学表达式计算。
            适用场景：用户要求进行数学计算、算术运算、数值求解时调用。
            支持的运算：加(+)、减(-)、乘(*)、除(/)。
            示例：用户问"123乘以456是多少"→ 传入"123 * 456"
            """)
    public String calculate(
            @ToolParam(description = "数学表达式，使用 + - * / 运算符，如 123*456", required = true)
            String expression) {
        log.info("🧮 计算表达式：{}", expression);

        try {
            // 解析表达式中的运算符
            double result;
            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                result = Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
            } else if (expression.contains("-")) {
                String[] parts = expression.split("-");
                result = Double.parseDouble(parts[0].trim()) - Double.parseDouble(parts[1].trim());
            } else if (expression.contains("*")) {
                String[] parts = expression.split("\\*");
                result = Double.parseDouble(parts[0].trim()) * Double.parseDouble(parts[1].trim());
            } else if (expression.contains("/")) {
                String[] parts = expression.split("/");
                double divisor = Double.parseDouble(parts[1].trim());
                if (divisor == 0) {
                    return "错误：除数不能为零！";
                }
                result = Double.parseDouble(parts[0].trim()) / divisor;
            } else {
                return "错误：不支持的表达式格式。请使用 + - * / 运算符。";
            }

            // 格式化结果（整数不显示小数，非整数保留 4 位小数）
            String resultStr;
            if (result == (long) result) {
                resultStr = String.format("%d", (long) result);
            } else {
                resultStr = String.format("%.4f", result);
            }

            log.info("✅ 计算结果：{} = {}", expression, resultStr);
            return String.format("%s = %s", expression, resultStr);
        } catch (NumberFormatException e) {
            log.error("❌ 表达式解析失败：{}", expression);
            return "错误：无法解析表达式中的数字。请确保格式正确，如：123 * 456";
        }
    }
}
