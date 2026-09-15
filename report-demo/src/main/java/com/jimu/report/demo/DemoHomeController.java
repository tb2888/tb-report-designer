package com.jimu.report.demo;

import com.jimu.report.starter.ReportProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 演示应用首页：根路径重定向到报表列表页，避免直接访问 http://localhost:8080 时看到错误页。
 * 报表 UI 由 starter 提供，挂在 SPA 基路径（默认 /report）下，列表页是 {base}/list。
 * 生产宿主可参考本类自行决定根路径行为（starter 不会抢占宿主的 "/"）。
 */
@Controller
public class DemoHomeController {

    private final ReportProperties props;

    public DemoHomeController(ReportProperties props) {
        this.props = props;
    }

    @GetMapping("/")
    public String home() {
        // ui-path 形如 /report/designer，取其父路径作为 SPA 基路径
        String uiPath = props.getUiPath();
        int idx = uiPath.lastIndexOf('/');
        String base = idx > 0 ? uiPath.substring(0, idx) : uiPath;
        return "redirect:" + base + "/list";
    }
}
