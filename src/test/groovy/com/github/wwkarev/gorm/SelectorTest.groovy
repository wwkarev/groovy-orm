package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.TestAddress
import com.github.wwkarev.gorm.test.models.TestModel
import com.github.wwkarev.gorm.test.models.TestModelWithForeignKey
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class SelectorTest extends Specification {
    @Shared
    Sql sql

    def setup() {
        DBConfig dbConfig = DBConfig.load(C.DB_CONFIG)
        sql = Sql.newInstance(dbConfig.host, dbConfig.user, dbConfig.password, dbConfig.driver)
    }

    def "test selector. Statement."() {
        when:
        Method method = selector.getClass().getDeclaredMethod('buildStatement')
        method.setAccessible(true)
        String statement = method.invoke(selector)
        then:
        statement == result
        where:
        selector|result
        new Selector(sql, TestModel) | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from test_model'
        new Selector(sql, TestModel).where(new Q(a: 1) | ~ new Q(b__is_null: true, cD__le: '3')).orderBy(['age__desc', 'birthday', 'first_name__asc']) | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from test_model where (a = ?) or (not (b is null and c_d <= ?)) order by age desc, birthday asc, first_name asc'
        new Selector(sql, TestModel).where('c__le', 1)                                                                                                 | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from test_model where c <= ?'
    }

    def "test selector. Statement select."() {
        when:
        new TableCreator(sql, TestAddress).create()
        new TableCreator(sql, TestModelWithForeignKey).create()
        Long id1 = sql.executeInsert("insert into test_fk (first_name, birthday, age) values('John', '1990-01-01', 50)")[0][0].longValue()
        Long id2 = sql.executeInsert("insert into test_fk (first_name, age) values('Ben', 30)")[0][0].longValue()
        Long id3 = sql.executeInsert("insert into test_fk (first_name, age) values('Ben', 30)")[0][0].longValue()
        Long id4 = sql.executeInsert("insert into test_address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
//        sql.executeInsert("insert into test_address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        Long id5 = sql.executeInsert("insert into test_fk (first_name, age, address_id) values('John', 10, 'Germany')")[0][0].longValue()

        List<TestModelWithForeignKey> testModels = new Selector(sql, TestModelWithForeignKey).filter(new Q(age__le: 10))
        println(testModels)
        testModels.each{
            println("name: ${it.firstName}")
            println(it.getAddressModel().city)
        }

        new TableDropper(sql, TestModelWithForeignKey).drop()
        new TableDropper(sql, TestAddress).drop()
        then:
        1 == 2
    }

    def cleanup() {
        sql.close()
    }
}
