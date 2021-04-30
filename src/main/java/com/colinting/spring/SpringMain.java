package com.colinting.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringMain {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        User user = (User) context.getBean("user");
        System.out.println(user);

        Person person = (Person) context.getBean("person");
        System.out.println(person);

        context.close();

    }
}
