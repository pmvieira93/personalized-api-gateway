package com.github.pmvieira93.gateway.infrastructure.batch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanPrinterRunner implements CommandLineRunner {

    private final ApplicationContext ctx;

    private final Boolean shouldPrintBeans;

    public BeanPrinterRunner(ApplicationContext ctx,
            @Value("${beans.printable:false}") Boolean shouldPrintBeans) {
        this.ctx = ctx;
        this.shouldPrintBeans = shouldPrintBeans;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!shouldPrintBeans) {
            return;
        }
        String[] beanNames = ctx.getBeanDefinitionNames();
        System.out.println("============================================================");
        System.out.println("============================================================");
        System.out.println("List of beans provided by Spring Boot [" + beanNames.length + "] : ");
        System.out.println("============================================================");
        for (String beanName : beanNames) {
            Object bean = ctx.getBean(beanName);
            System.out.println(beanName + " : " + bean.getClass().toString());
        }
        System.out.println("============================================================");
        System.out.println("============================================================");
    }
}