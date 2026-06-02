package com.ailearn.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * <h1>🌤️ 天气查询工具</h1>
 *
 * <p>演示如何定义一个 AI Agent 可以调用的工具。</p>
 *
 * <h2>关键注解说明</h2>
 * <table border="1">
 *   <tr><th>注解</th><th>作用</th><th>面试要点</th></tr>
 *   <tr>
 *     <td>@Component</td>
 *     <td>注册为 Spring Bean，让 ChatClient 的 .tools() 方法能找到它</td>
 *     <td>工具必须是 Spring 管理的 Bean</td>
 *   </tr>
 *   <tr>
 *     <td>@Tool</td>
 *     <td>标记这是一个 AI 可调用的工具方法</td>
 *     <td>description 是 AI 判断是否调用的关键依据</td>
 *   </tr>
 *   <tr>
 *     <td>@ToolParam</td>
 *     <td>标记工具方法的参数</td>
 *     <td>description 告诉 AI 每个参数的含义</td>
 *   </tr>
 * </table>
 *
 * <h2>面试题：Tool 的 description 为什么这么重要？</h2>
 * <p>因为 AI 是通过 description 来判断"是否调用"以及"调用哪个工具"的。
 * 如果 description 写得不好，AI 可能会：</p>
 * <ul>
 *   <li>该调用时没调用（description 太模糊）</li>
 *   <li>不该调用时调用了（description 太宽泛）</li>
 *   <li>参数传错了（@ToolParam 的 description 不清楚）</li>
 * </ul>
 *
 * <p><b>最佳实践：</b>description 要写清楚"什么情况下应该调用这个工具"。</p>
 *
 * @see org.springframework.ai.tool.annotation.Tool
 */
@Component
public class WeatherTool {

    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);

    /**
     * 查询指定城市的天气
     *
     * <p><b>@Tool 注解的 description 编写原则：</b></p>
     * <ol>
     *   <li>说明功能：查询城市天气</li>
     *   <li>说明适用场景：用户问天气时</li>
     *   <li>说明局限性：仅支持中国主要城市（诚实）</li>
     *   <li>说明返回值格式：温度、天气状况、风力等</li>
     * </ol>
     *
     * <p><b>⚠️ 生产环境注意：</b>这里用模拟数据做演示。
     * 实际项目中应该对接真正的天气 API（如和风天气、心知天气等）。</p>
     *
     * @param city 城市名称（支持中文名，如"北京"、"杭州"）
     * @return 格式化的天气信息字符串
     */
    @Tool(description = """
            查询指定城市当前的天气情况。
            适用场景：用户询问某城市天气、气温、是否下雨、是否适合出行等问题时调用。
            仅支持中国主要城市。
            返回：温度、天气状况、风力、湿度等信息。
            """)
    public String getWeather(
            @ToolParam(description = "城市名称，使用中文，如：北京、上海、杭州") String city) {
        log.info("🔍 查询天气：{}", city);

        // ============ 模拟天气数据 ============
        // 生产环境替换为真实 API 调用，例如：
        //   String apiUrl = "https://api.weather.com/v1/current?city=" + city;
        //   return httpClient.get(apiUrl);
        String weather;
        switch (city) {
            case "北京":
                weather = """
                    城市：北京
                    天气：晴转多云
                    温度：8°C ~ 22°C
                    风力：北风 3-4 级
                    湿度：45%
                    建议：适合户外活动，早晚温差大，注意添衣
                    """;
                break;
            case "上海":
                weather = """
                    城市：上海
                    天气：小雨
                    温度：15°C ~ 20°C
                    风力：东南风 2-3 级
                    湿度：78%
                    建议：出门请带雨伞
                    """;
                break;
            case "杭州":
                weather = """
                    城市：杭州
                    天气：晴
                    温度：18°C ~ 25°C
                    风力：微风
                    湿度：55%
                    建议：天气晴好，非常适合出游！
                    """;
                break;
            case "深圳":
                weather = """
                    城市：深圳
                    天气：多云
                    温度：22°C ~ 28°C
                    风力：南风 2 级
                    湿度：70%
                    建议：温暖舒适，适合户外活动
                    """;
                break;
            default:
                weather = String.format("""
                    城市：%s
                    天气：暂无数据（仅支持北京、上海、杭州、深圳）
                    建议：请尝试查询支持的城市
                    """, city);
        }

        log.debug("📊 天气数据：\n{}", weather);
        return weather;
    }
}
