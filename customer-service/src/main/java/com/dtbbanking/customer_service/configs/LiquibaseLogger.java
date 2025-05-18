package com.dtbbanking.customer_service.configs;

import jakarta.annotation.PostConstruct;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LiquibaseLogger {

    @Autowired(required = false)
    private SpringLiquibase liquibase;

    @PostConstruct
    public void logLiquibaseStatus() {
        if (liquibase != null) {
            System.out.println(" Liquibase is configured and should run.");
        } else {
            System.err.println(" Liquibase bean is NOT loaded.");
        }
    }
}
