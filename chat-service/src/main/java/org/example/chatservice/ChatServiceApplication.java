package org.example.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class ChatServiceApplication {
    public static void main(String[] args) {
        System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
        SpringApplication.run(ChatServiceApplication.class, args);
    }

}
