package com.thorn.bbsmain.utils;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Optional;

public class MysqlJdbc {
    static JdbcTemplate jdbcTemplate = getTemplate();

    public static JdbcTemplate getTemplate() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/thorn?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        dataSource.setUsername("root");
        dataSource.setPassword("");
        return new JdbcTemplate(dataSource);
    }

    public static String getPassword(String email) {
        return jdbcTemplate.queryForObject("select password from user where email='" + email + "'",
                String.class);
    }

    public static int getUncheckedMessage(int uid) {
        return Optional.ofNullable(jdbcTemplate.queryForObject("select count(*) from message where owner=" + uid +
                " and isCheck=false ", Integer.class)).orElse(0);
    }

    public static Integer getUID(String email) {
        return jdbcTemplate.queryForObject("select uid from user where email='" + email +
                "'", Integer.class);
    }
}
