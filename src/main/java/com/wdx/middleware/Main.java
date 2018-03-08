package com.wdx.middleware;

import com.wdx.middleware.aop.ProxyAopHandler;
import com.wdx.middleware.service.DemoService;
import com.wdx.middleware.service.impl.DemoServiceImpl;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        List<Thread> workers = new ArrayList<Thread>();
        for(int i = 0 ; i<10;i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    DemoService demoService = new DemoServiceImpl();
                    DemoService service = (DemoService) Proxy.newProxyInstance(
                            demoService.getClass().getClassLoader(),
                            demoService.getClass().getInterfaces(),
                            new ProxyAopHandler(demoService));
                    String result = service.say("hello world");

                    System.out.println(result);
                }
            });

            t.start();
            workers.add(t);
        }
        for(int i = 0; i < workers.size(); i++) {
            workers.get(i).join();
        }
    }
}
