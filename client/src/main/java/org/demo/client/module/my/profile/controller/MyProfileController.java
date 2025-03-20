package org.demo.client.module.my.profile.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/my")
public class MyProfileController {

    @GetMapping("/profile")
    public String myProfilePage() {
        return "my/profile";
    }
}
