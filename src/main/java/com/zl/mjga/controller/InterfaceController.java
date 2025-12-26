package com.zl.mjga.controller;

/**
 * @author roc
 * @since 2025/12/25 20:14
 */
import com.zl.mjga.service.InterfaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/interface")
@RequiredArgsConstructor
public class InterfaceController {
    private final InterfaceService interfaceService;


}
