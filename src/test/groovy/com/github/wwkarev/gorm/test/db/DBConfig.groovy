package com.github.wwkarev.gorm.test.db

import groovy.json.JsonSlurper

class DBConfig {
    private static DBConfig dbConfig
    private String host
    private String user
    private String password
    private String driver

    private DBConfig(String host, String user, String password, String driver) {
        this.host = host
        this.user = user
        this.password = password
        this.driver = driver
    }

    static DBConfig load(String configFileName) {
        if (!dbConfig) {
            def config = new JsonSlurper().parse(new File(configFileName))
            dbConfig = new DBConfig(config.host, config.user, config.password, config.driver)
        }
        return dbConfig
    }

    String getHost() {
        return host
    }

    String getUser() {
        return user
    }

    String getPassword() {
        return password
    }

    String getDriver() {
        return driver
    }
}
