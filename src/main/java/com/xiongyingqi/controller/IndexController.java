package com.xiongyingqi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiongyingqi
 * @since 16-10-24 上午11:34
 */
@RestController
public class IndexController {
    @GetMapping("/")
    public Object index() {
        return "Hello world!";
    }
}
