package com.jimu.report.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 演示用 mock 业务接口：模拟宿主/第三方返回「主子表」嵌套 JSON，
 * 供 API 类型数据集演示 dataPath / detailPath 解析（真实宿主应指向自己的业务 API）。
 */
@RestController
public class DemoMockController {

    @GetMapping("/mock/orders")
    public Map<String, Object> orders(@RequestParam(required = false) String status) {
        List<Map<String, Object>> orders = List.of(
                order("SO-2026-0801", "华南医药集团", "2026-08-01", "已发货", List.of(
                        item("阿莫西林胶囊 0.25g*24", 200, 8.5),
                        item("布洛芬缓释胶囊 0.3g*20", 300, 12.0))),
                order("SO-2026-0802", "华东器械贸易", "2026-08-02", "待发货", List.of(
                        item("一次性医用外科口罩", 1000, 0.35),
                        item("医用防护服 L", 120, 68.0),
                        item("红外额温枪", 60, 89.9))),
                order("SO-2026-0803", "华北连锁药房", "2026-08-03", "已签收", List.of(
                        item("维生素C咀嚼片 100片", 500, 22.5))));
        List<Map<String, Object>> filtered = status == null || status.isBlank()
                ? orders
                : orders.stream().filter(o -> status.equals(o.get("status"))).toList();
        return Map.of("code", 0, "msg", "ok", "data", Map.of("orders", filtered));
    }

    private Map<String, Object> order(String no, String customer, String date, String status,
                                      List<Map<String, Object>> items) {
        double total = items.stream().mapToDouble(i -> (double) i.get("amount")).sum();
        return Map.of("orderNo", no, "customerName", customer, "orderDate", date, "status", status,
                "totalAmount", Math.round(total * 100) / 100.0, "items", items);
    }

    private Map<String, Object> item(String name, int qty, double price) {
        double amount = Math.round(qty * price * 100) / 100.0;
        return Map.of("productName", name, "qty", qty, "price", price, "amount", amount);
    }
}
