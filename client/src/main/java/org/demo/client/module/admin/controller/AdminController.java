package org.demo.client.module.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * 회원관리 페이지
     *
     * @return
     */
    @GetMapping("/member")
    public String adminPage(Model model) {
        model.addAttribute("menuOption", 0);
        return "admin/member";
    }

    /**
     * 공지하기 페이지
     *
     * @return
     */
    @GetMapping("/notice")
    public String noticePage(Model model) {
        model.addAttribute("menuOption", 1);
        return "admin/notice";
    }
}
