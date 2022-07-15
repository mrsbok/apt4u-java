package kr.co.thefc.bbl.controller;

import kr.co.thefc.bbl.controller.api.admin.AdminController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Slf4j
@Controller
public class MainController {
    private AdminController adminController;
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

    @GetMapping("/admin/index")
    public String admin_index() {
        log.info("admin index test");
        return "admin/index";
    }

    @GetMapping("/admin/login")
    public String admin_login() {
        log.info("admin login test");
        return "admin/login";
    }

    @GetMapping("/admin/manage_affiliated_stores")
    public String manage_affiliated_stores(HttpServletRequest request, Model model) {
        log.info("manage affiliated stores test");

        return "admin/manage_affiliated_stores";
    }

    @GetMapping("/store/index")
    public String store_index() {
        log.info("store index test");
        return "store/index";
    }

    @GetMapping("/store/register")
    public String store_register() {
        log.info("store register test");
        return "store/register";
    }

    @GetMapping("/store/login")
    public String store_login() {
        log.info("store login test");
        return "store/login";
    }

    @GetMapping("/store/manage_affiliated_trainers")
    public String manage_affiliated_trainers() {
        log.info("manage affiliated trainers test");
        return "store/manage_affiliated_trainers";
    }
 }
