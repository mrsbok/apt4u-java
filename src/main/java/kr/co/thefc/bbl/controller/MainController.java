package kr.co.thefc.bbl.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class MainController {
    @GetMapping("/")
    public String root() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index() {
        log.info("index");
        return "index";
    }

    @GetMapping("/test_guide")
    public String test_guide() {
        log.info("test_guide");
        return "test_guide";
    }
}
