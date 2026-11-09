package com.nautiflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DemoDataSeederTest {

    @Test
    void noCargaDatosSiYaHayLancheros() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        DataSource dataSource = mock(DataSource.class);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM lanchero", Long.class)).thenReturn(3L);

        new DemoDataSeeder(jdbcTemplate, dataSource).run(null);

        verifyNoInteractions(dataSource);
    }

    @Test
    void elScriptDeDemoEstaEnElClasspath() throws Exception {
        ClassPathResource script = new ClassPathResource(DemoDataSeeder.SCRIPT);

        assertTrue(script.exists());
        String contenido = script.getContentAsString(StandardCharsets.UTF_8);
        assertTrue(contenido.contains("INSERT INTO lanchero"));
        assertTrue(contenido.contains("INSERT INTO viaje"));
    }
}
