package com.zl.mjga.service;

import com.roc.api.service.DemoService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @author roc
 * @since 2026/3/10 13:33
 */
@DubboService
public class DemoServiceImpl implements DemoService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
