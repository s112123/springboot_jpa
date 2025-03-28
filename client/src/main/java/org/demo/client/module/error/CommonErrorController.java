package org.demo.client.module.error;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class CommonErrorController {

    @GetMapping("/404")
    public String handleNotFound() {
        return "error/4xx";
    }

    @GetMapping("/500")
    public String handleServerError() {
        return "error/5xx";
    }
}
