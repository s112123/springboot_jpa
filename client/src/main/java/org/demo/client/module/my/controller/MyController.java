package org.demo.client.module.my.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
public class MyController {

    /**
     * 내 정보 페이지
     *
     * @return
     */
    @GetMapping("/profile")
    public String myProfilePage(Model model) {
        model.addAttribute("menuOption", 0);
        return "my/profile";
    }

    /**
     * 내가 쓴 리뷰 페이지
     *
     * @return
     */
    @GetMapping("/review")
    public String myReviewPage(Model model) {
        model.addAttribute("menuOption", 1);
        return "my/review";
    }

    /**
     * 내가 찜한 리뷰 페이지
     *
     * @return
     */
    @GetMapping("/good")
    public String myGoodPage(Model model) {
        model.addAttribute("menuOption", 2);
        return "my/good";
    }

    /**
     * 알림 내역 페이지
     *
     * @return
     */
    @GetMapping("/notification")
    public String myNotificationPage(Model model) {
        model.addAttribute("menuOption", 3);
        return "my/notification";
    }

    /**
     * 1:1 채팅 페이지
     *
     * @return
     */
    @GetMapping("/chat")
    public String myChatPage(Model model) {
        model.addAttribute("menuOption", 4);
        return "my/chat";
    }
}
