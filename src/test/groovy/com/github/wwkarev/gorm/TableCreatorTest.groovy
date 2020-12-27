package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.config.Config
import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.TestModel
import com.github.wwkarev.gorm.test.models.TestModelWithCustomTableName
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
        new TableCreator(sql, TestModel).create()
        sql.rows("select * from test_model")
        new TableDropper(sql, TestModel).drop()
        then:
        notThrown Exception
    }

    def "test TableCreator. Custom table name"() {
        when:
        new TableCreator(sql, TestModelWithCustomTableName).create()
        sql.rows("select * from test_model_custom")
        new TableDropper(sql, TestModelWithCustomTableName).drop()
        then:
        notThrown Exception
    }

    def "test TableCreator. Statement"() {
        when:
        TableCreator tableCreator = new TableCreator(sql, TestModel)
        Method method = tableCreator.getClass().getDeclaredMethod('buildCreateStatement')
        method.setAccessible(true)
        String statement = method.invoke(tableCreator)
        then:
        statement == 'create table test_model (id serial primary key, first_name text, family_name text, age integer, birthday timestamptz)'
    }

    def cleanup() {
        sql.close()
    }
}
