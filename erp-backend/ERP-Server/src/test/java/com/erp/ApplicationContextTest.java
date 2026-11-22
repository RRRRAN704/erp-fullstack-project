package com.erp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/** Smoke test: the app starts against the containers and sees the seed data. */
class ApplicationContextTest extends IntegrationTestBase {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextLoadsAndSeedDataIsPresent() {
        Integer employees = jdbcTemplate.queryForObject("select count(*) from employee", Integer.class);
        assertThat(employees).isPositive();
    }
}
