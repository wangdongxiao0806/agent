package com.wdx.middleware.service.impl;

import com.wdx.middleware.service.DemoService;

public class DemoServiceImpl implements DemoService {

    public String say(String str) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(str);
        return str;
    }
}
