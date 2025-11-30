package com.bookstore.controller;

import com.bookstore.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "http://localhost:5173")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取销售统计数据
     * @param period 时间周期：day, week, month, year
     * @return 销售统计数据
     */
    @GetMapping("/sales")
    public ResponseEntity<?> getSalesStatistics(@RequestParam(defaultValue = "day") String period) {
        if (!period.equals("day") && !period.equals("week") && !period.equals("month") && !period.equals("year")) {
            return ResponseEntity.badRequest().body("无效的时间周期");
        }
        return ResponseEntity.ok(statisticsService.getSalesStatistics(period));
    }

    /**
     * 获取畅销书籍排行
     * @param limit 返回的数量限制
     * @return 畅销书籍排行数据
     */
    @GetMapping("/top-books")
    public ResponseEntity<?> getTopBooks(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statisticsService.getTopBooks(limit));
    }

    /**
     * 获取分类销售比例（按书籍数量）
     * @return 分类销售比例数据（按书籍数量）
     */
    @GetMapping("/category-sales")
    public ResponseEntity<?> getCategorySales() {
        return ResponseEntity.ok(statisticsService.getCategorySales());
    }

    /**
     * 获取订单状态分布
     * @return 订单状态分布数据
     */
    @GetMapping("/order-status")
    public ResponseEntity<?> getOrderStatusDistribution() {
        return ResponseEntity.ok(statisticsService.getOrderStatusDistribution());
    }
}
