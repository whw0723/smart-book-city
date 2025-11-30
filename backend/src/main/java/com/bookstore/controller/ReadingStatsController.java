package com.bookstore.controller;


import com.bookstore.service.ReadingStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reading-stats")
@CrossOrigin(origins = "http://localhost:5173")
public class ReadingStatsController {

    @Autowired
    private ReadingStatsService readingStatsService;

    /**
     * 获取用户的阅读统计数据
     * @param userId 用户ID
     * @return 阅读统计数据
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReadingStats(@PathVariable Long userId) {
        Map<String, Object> statsData = readingStatsService.getUserReadingStats(userId);
        return ResponseEntity.ok(statsData);
    }
}
