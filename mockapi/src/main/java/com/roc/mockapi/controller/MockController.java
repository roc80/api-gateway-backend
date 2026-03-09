package com.roc.mockapi.controller;

import com.roc.apiclientsdk.module.ApiResponse;
import com.roc.apiclientsdk.module.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author roc
 * @since 2026/2/25 10:56
 */
@RestController
@RequestMapping("/name")
public class MockController {

    @PostMapping("")
    public ApiResponse name(@RequestBody User user) {
        return ApiResponse.success(user.getUsername());
    }
}
