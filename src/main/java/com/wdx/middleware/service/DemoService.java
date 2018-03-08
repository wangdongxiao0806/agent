package com.wdx.middleware.service;

import com.wdx.middleware.annontation.Agent;

public interface DemoService {

    @Agent
    public String say(String str);
}
