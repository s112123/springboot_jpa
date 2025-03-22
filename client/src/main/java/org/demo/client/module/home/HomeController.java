package org.demo.client.module.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * 메인 페이지
     * 
     * @return
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
