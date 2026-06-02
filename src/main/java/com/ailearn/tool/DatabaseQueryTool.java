package com.ailearn.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * <h1>🗄️ 数据库查询工具（模拟）</h1>
 *
 * <p>模拟一个简化的数据库查询工具，演示 Agent 如何与数据层交互。</p>
 *
 * <h2>面试要点</h2>
 * <p>在企业级 AI Agent 中，数据库查询工具是最常见的工具之一。
 * 面试常考：</p>
 * <ol>
 *   <li>如何防止 SQL 注入？（使用参数化查询）</li>
 *   <li>如何限制工具权限？（只读查询，禁止 DDL/DML）</li>
 *   <li>如何处理大数据量？（分页、流式返回）</li>
 * </ol>
 */
@Component
public class DatabaseQueryTool {

    private static final Logger log = LoggerFactory.getLogger(DatabaseQueryTool.class);

    // ============ 模拟数据库数据 ============
    // 生产环境替换为真正的 JdbcTemplate / MyBatis / JPA 查询
    private static final List<Map<String, Object>> EMPLOYEE_DB = List.of(
            Map.of("id", 1, "name", "张三", "department", "研发部", "salary", 15000),
            Map.of("id", 2, "name", "李四", "department", "产品部", "salary", 18000),
            Map.of("id", 3, "name", "王五", "department", "研发部", "salary", 20000),
            Map.of("id", 4, "name", "赵六", "department", "市场部", "salary", 13000),
            Map.of("id", 5, "name", "孙七", "department", "研发部", "salary", 22000)
    );

    /**
     * 查询员工信息
     *
     * <h3>安全设计：</h3>
     * <ol>
     *   <li>不接受原始 SQL，只接受查询条件（防止 SQL 注入）</li>
     *   <li>只读操作，不提供修改/删除能力</li>
     *   <li>如果数据量大，返回前 N 条（防止 Token 消耗过多）</li>
     * </ol>
     *
     * @param department 部门名称（可选，null 表示查全部）
     * @return 格式化的查询结果
     */
    @Tool(description = """
            查询公司员工信息。
            适用场景：用户询问员工信息、部门人员、薪资等问题时调用。
            仅支持只读查询，无法修改数据。
            可通过部门名称筛选，不传则查询所有员工。
            注意：每次最多返回 10 条记录。
            """)
    public String queryEmployees(
            @ToolParam(description = "部门名称，如 研发部、产品部、市场部。不传表示查询全部", required = false)
            String department) {

        log.info("🔍 查询员工信息，部门：{}", (department != null ? department : "全部"));

        StringBuilder result = new StringBuilder();
        result.append("员工信息查询结果：\n");
        result.append("──────────────────────────────────────\n");

        int count = 0;
        for (Map<String, Object> emp : EMPLOYEE_DB) {
            // 如果指定了部门，按部门筛选
            if (department != null && !department.equals(emp.get("department"))) {
                continue;
            }

            result.append(String.format("工号：%d | 姓名：%s | 部门：%s | 薪资：¥%,d\n",
                    emp.get("id"), emp.get("name"), emp.get("department"), emp.get("salary")));
            count++;

            // 最多 10 条（大数据量场景的防护措施）
            if (count >= 10) {
                result.append("\n（结果过多，已截断至前 10 条）");
                break;
            }
        }

        if (count == 0) {
            result.append("未找到匹配的员工记录。");
        } else {
            result.append("──────────────────────────────────────\n");
            result.append(String.format("共找到 %d 条记录。", count));
        }

        return result.toString();
    }

    /**
     * 统计各部门人数
     *
     * @return 部门人数统计
     */
    @Tool(description = """
            统计各部门员工人数。
            适用场景：用户询问公司人员结构、部门规模等问题时调用。
            """)
    public String countByDepartment() {
        log.info("📊 统计各部门人数");

        Map<String, Long> deptCount = Map.of(
                "研发部", EMPLOYEE_DB.stream().filter(e -> "研发部".equals(e.get("department"))).count(),
                "产品部", EMPLOYEE_DB.stream().filter(e -> "产品部".equals(e.get("department"))).count(),
                "市场部", EMPLOYEE_DB.stream().filter(e -> "市场部".equals(e.get("department"))).count()
        );

        StringBuilder result = new StringBuilder();
        result.append("各部门人数统计：\n");
        result.append("──────────────────\n");
        deptCount.forEach((dept, count) ->
                result.append(String.format("  %s：%d 人\n", dept, count))
        );
        result.append(String.format("──────────────────\n  总计：%d 人\n", EMPLOYEE_DB.size()));

        return result.toString();
    }
}
