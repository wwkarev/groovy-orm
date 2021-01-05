package com.github.wwkarev.gorm


import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.Worker
import com.github.wwkarev.gorm.test.models.WorkerNullConfig
import com.github.wwkarev.gorm.test.models.WorkerWithCustomTableName
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class TableCreatorTest extends Specification {
    @Shared
    Sql sql

    def setup() {
        DBConfig dbConfig = DBConfig.load(C.DB_CONFIG)
        sql = Sql.newInstance(dbConfig.host, dbConfig.user, dbConfig.password, dbConfig.driver)
    }

    def "test TableCreator"() {
        when:
        new TableCreator(sql, WorkerNullConfig).create()
        sql.rows("select * from worker_null_config")
        new TableDropper(sql, WorkerNullConfig).drop()
        then:
        notThrown Exception
    }

    def "test TableCreator. Custom table name"() {
        when:
        new TableCreator(sql, WorkerWithCustomTableName).create()
        sql.rows("select * from worker_custom")
        new TableDropper(sql, WorkerWithCustomTableName).drop()
        then:
        notThrown Exception
    }

    def "test TableCreator. Statement"() {
        when:
        TableCreator tableCreator = new TableCreator(sql, Worker)
        Method method = tableCreator.getClass().getDeclaredMethod('buildStatement')
        method.setAccessible(true)
        String statement = method.invoke(tableCreator)
        then:
        statement == 'create table worker (id serial primary key, first_name text, family_name text, age integer, birthday timestamptz)'
    }

    def "test TableDropper. Statement."() {
        when:
        TableDropper tableDropper = new TableDropper(sql, Worker)
        Method method = tableDropper.getClass().getDeclaredMethod('buildStatement', Boolean)
        method.setAccessible(true)
        String statement = method.invoke(tableDropper, false)
        then:
        statement == 'drop table worker'
    }

    def "test TableDropper. Statement cascade"() {
        when:
        TableDropper tableDropper = new TableDropper(sql, Worker)
        Method method = tableDropper.getClass().getDeclaredMethod('buildStatement', Boolean)
        method.setAccessible(true)
        String statement = method.invoke(tableDropper, true)
        then:
        statement == 'drop table worker cascade'
    }

    def cleanup() {
        sql.close()
    }
}
