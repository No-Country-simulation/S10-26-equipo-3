package com.nautiflow.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

@Component
@ConditionalOnProperty(name = "nautiflow.seed.demo", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {
    static final String SCRIPT = "db/demo-data.sql";

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public DemoDataSeeder(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Long lancheros = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lanchero", Long.class);
        if (lancheros != null && lancheros > 0) {
            log.info("Datos de demo omitidos: la base ya tiene {} lancheros.", lancheros);
            return;
        }
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource(SCRIPT));
        populator.setSqlScriptEncoding("UTF-8");
        populator.execute(dataSource);
        log.info("Datos de demo cargados desde {}.", SCRIPT);
    }
}
