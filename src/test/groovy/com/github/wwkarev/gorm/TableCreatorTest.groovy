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

    def "test TableTruncater"() {
        setup:
        new TableCreator(sql, Worker).create()
        when:
        Worker worker = new Worker(sql, UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0, new Date())
        worker.insert()
        worker.id = null
        worker.insert()
        assert sql.rows("select * from worker").size() == 2
        new TableTruncater(sql, Worker).truncate()
        Long size = sql.rows("select * from worker").size()
        then:
        assert size == 0
        notThrown Exception
        cleanup:
        new TableDropper(sql, Worker).drop()
    }

    def "test TableTruncater. Statement."() {
        when:
        TableTruncater tableTruncater = new TableTruncater(sql, Worker)
        Method method = tableTruncater.getClass().getDeclaredMethod('buildStatement', Boolean)
        method.setAccessible(true)
        String statement = method.invoke(tableTruncater, false)
        then:
        statement == 'truncate worker'
    }

    def "test TableTruncater. Statement cascade"() {
        when:
        TableTruncater tableTruncater = new TableTruncater(sql, Worker)
        Method method = tableTruncater.getClass().getDeclaredMethod('buildStatement', Boolean)
        method.setAccessible(true)
        String statement = method.invoke(tableTruncater, true)
        then:
        statement == 'truncate worker cascade'
    }

    def cleanup() {
        sql.close()
    }
}
