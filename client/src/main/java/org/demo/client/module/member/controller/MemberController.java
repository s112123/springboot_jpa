package org.demo.client.module.member.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {

    /**
     * 회원가입 페이지
     * 
     * @return
     */
    @GetMapping("/add")
    public String home() {
        return "sign_up";
    }
}
