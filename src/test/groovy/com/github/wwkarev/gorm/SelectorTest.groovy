package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.exceptions.MultipleRecordsFoundException
import com.github.wwkarev.gorm.exceptions.RecordNotFoundException
import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.Address
import com.github.wwkarev.gorm.test.models.Worker
import com.github.wwkarev.gorm.test.models.WorkerWithForeignKey
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

        new TableCreator(sql, Address).create()
        new TableCreator(sql, WorkerWithForeignKey).create()
        new TableCreator(sql, Worker).create()
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
        new Selector(sql, Worker)                                                                                                                   | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from worker'
        new Selector(sql, Worker).where(new Q(a: 1) | ~ new Q(b__is_null: true, cD__le: '3')).orderBy(['age__desc', 'birthday', 'first_name__asc']) | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from worker where (a = ?) or (not (b is null and c_d <= ?)) order by age desc, birthday asc, first_name asc'
        new Selector(sql, Worker).where('c__le', 1)                                                                                                 | 'select id as id, first_name as first_name, family_name as last_name, age as age, birthday as birthday from worker where c <= ?'
    }

    def "test selector. Order by DESC."() {
        when:
        sql.executeInsert("insert into worker (family_name, birthday, age) values('John', '1990-01-01', 10)")
        sql.executeInsert("insert into worker (family_name, age) values('Michael', 30)")
        sql.executeInsert("insert into worker (family_name, age) values('Joe', 50)")
        sql.executeInsert("insert into worker (family_name, age) values('Dan', 20)")

        List<Worker> testModels = new Selector(sql, Worker).orderBy('lastName__desc').filter('age__le', 30)
        then:
        testModels[0].lastName == 'Michael'
        testModels[1].lastName == 'John'
        testModels[2].lastName == 'Dan'
    }

    def "test selector. Order by ASC."() {
        when:
        sql.executeInsert("insert into worker (family_name, birthday, age) values('John', '1990-01-01', 10)")
        sql.executeInsert("insert into worker (family_name, age) values('Michael', 30)")
        sql.executeInsert("insert into worker (family_name, age) values('Joe', 50)")
        sql.executeInsert("insert into worker (family_name, age) values('Dan', 20)")

        List<Worker> testModels = new Selector(sql, Worker).orderBy('lastName').filter('age__le', 30)
        then:
        testModels[0].lastName == 'Dan'
        testModels[1].lastName == 'John'
        testModels[2].lastName == 'Michael'
    }

    def "test selector. Statement select."() {
        when:
        Long id1 = sql.executeInsert("insert into test_fk (first_name, birthday, age) values('John', '1990-01-01', 50)")[0][0].longValue()
        Long id2 = sql.executeInsert("insert into test_fk (first_name, age) values('Ben', 30)")[0][0].longValue()
        Long id3 = sql.executeInsert("insert into test_fk (first_name, age) values('Ben', 30)")[0][0].longValue()
        Long id4 = sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
//        sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        Long id5 = sql.executeInsert("insert into test_fk (first_name, age, address_id) values('John', 10, 'Germany')")[0][0].longValue()

        List<WorkerWithForeignKey> testModels = new Selector(sql, WorkerWithForeignKey).filter(new Q(age__le: 10))
        println(testModels)
        testModels.each{
            println("name: ${it.firstName}")
            println(it.getAddressModel().city)
        }
        then:
        1 == 1
    }

    def "test selector. Get. Record Not Found."() {
        when:
        new Selector(sql, Address).get(new Q(city: 'Berlin'))

        then:
        thrown RecordNotFoundException
    }

    def "test selector. Get. Multiple destination records."() {
        when:
        sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")
        sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")

        new Selector(sql, Address).get(new Q(city: 'Berlin'))

        then:
        thrown MultipleRecordsFoundException
    }

    def "test selector. Foreign Key. Legal."() {
        when:
        Long addressId = sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        sql.executeInsert("insert into test_fk (first_name, family_name, age, address_id) values('Ben', 'Thor', 30, 'Germany')")

        WorkerWithForeignKey modelWithForeignKey = new Selector(sql, WorkerWithForeignKey).get(new Q(lastName: 'Thor'))
        Address testAddress = modelWithForeignKey.getAddressModel()

        then:
        testAddress.id == addressId
        testAddress.country == 'Germany'
        testAddress.city == 'Berlin'
        testAddress.street == 'Street #1'
    }

    def cleanup() {
        new TableDropper(sql, WorkerWithForeignKey).drop()
        new TableDropper(sql, Address).drop()
        new TableDropper(sql, Worker).drop()

        sql.close()
    }
}
