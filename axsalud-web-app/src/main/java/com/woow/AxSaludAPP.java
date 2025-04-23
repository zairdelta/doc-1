package com.woow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class AxSaludAPP {

    public static void main(String[] args) {
        SpringApplication.run(AxSaludAPP.class, args);
        log.info("AX SALUD application starts !");
    }
}