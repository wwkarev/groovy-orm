package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.TestAddress
import com.github.wwkarev.gorm.test.models.TestModel
import com.github.wwkarev.gorm.test.models.TestModelWithForeignKey
import com.github.wwkarev.gorm.test.models.TestModelWithLo
import com.github.wwkarev.gorm.util.TestHelper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method

class ModelTest extends Specification {
    @Shared
    Sql sql

    def setup() {
        DBConfig dbConfig = DBConfig.load(C.DB_CONFIG)
        sql = Sql.newInstance(dbConfig.host, dbConfig.user, dbConfig.password, dbConfig.driver)
    }

    def "test insert"() {
        when:
        new TableCreator(sql, TestModelWithLo).create()
        TestModelWithLo model = TestModelWithLo.getInstance(sql)
        model.insert()

        GroovyRowResult rowResult = sql.firstRow("select * from test_model_with_lo where id = ${model.id}")

        new TableDropper(sql, TestModelWithLo).drop()
        then:
        model.firstName == rowResult.first_name
        model.lastName == rowResult.family_name
        model.age == rowResult.age
        model.birthday.getTime() == rowResult.birthday.getTime()
        model.biography == TestHelper.convertLargeObject(sql, rowResult.bio)
    }

    def "test insert statement"() {
        when:
        Model model = TestModelWithLo.getInstance(sql)

        Class modelClass = TestHelper.getModelClass(model.getClass())

        Method method = modelClass.getDeclaredMethod('getStatementColumnInfoListWithoutId')
        method.setAccessible(true)
        List<UpdateStatementColumnInfo> updateStatementColumnInfoList = method.invoke(model)

        method = modelClass.getDeclaredMethod('buildInsertStatement', List.class)
        method.setAccessible(true)
        String statement = method.invoke(model, updateStatementColumnInfoList)

        method = modelClass.getDeclaredMethod('getValueList', List.class)
        method.setAccessible(true)
        List<Object> valueList =  method.invoke(model, updateStatementColumnInfoList)
        then:
        statement == 'insert into test_model_with_lo (first_name, family_name, age, birthday, bio) values(?, ?, ?, ?, lo_from_bytea(0, ?::bytea))'
        valueList[0] == model.firstName
        valueList[1] == model.lastName
        valueList[2] == model.age
        valueList[3].getTime() == model.birthday.toTimestamp().getTime()
        valueList[4] == model.biography
    }

    def "test update"() {
        when:
        new TableCreator(sql, TestModelWithLo).create()

        Long id = sql.executeInsert("insert into test_model_with_lo (first_name) values(null)")[0][0].longValue()

        TestModelWithLo model = TestModelWithLo.getInstance(sql)
        model.id = id
        model.update()

        GroovyRowResult rowResult = sql.firstRow("select * from test_model_with_lo where id = ${model.id}")

        new TableDropper(sql, TestModelWithLo).drop()
        then:
        model.firstName == rowResult.first_name
        model.lastName == rowResult.family_name
        model.age == rowResult.age
        model.birthday.getTime() == rowResult.birthday.getTime()
        model.biography == TestHelper.convertLargeObject(sql, rowResult.bio)
    }

    def "test update statement"() {
        when:
        Long modelId = 0
        Model model = TestModelWithLo.getInstance(sql)
        model.id = modelId

        Class modelClass = TestHelper.getModelClass(model.getClass())

        Method method = modelClass.getDeclaredMethod('getStatementColumnInfoListWithoutId')
        method.setAccessible(true)
        List<UpdateStatementColumnInfo> updateStatementColumnInfoList = method.invoke(model)

        method = modelClass.getDeclaredMethod('buildUpdateStatement', List.class)
        method.setAccessible(true)
        String statement = method.invoke(model, updateStatementColumnInfoList)

        method = modelClass.getDeclaredMethod('getValueList', List.class)
        method.setAccessible(true)
        List<Object> valueList =  method.invoke(model, updateStatementColumnInfoList)
        then:
        statement == "update test_model_with_lo set first_name = ?, family_name = ?, age = ?, birthday = ?, bio = lo_from_bytea(0, ?::bytea) where id = $modelId"
        valueList[0] == model.firstName
        valueList[1] == model.lastName
        valueList[2] == model.age
        valueList[3].getTime() == model.birthday.toTimestamp().getTime()
        valueList[4] == model.biography
    }

    def "test insert foreignKey"() {
        setup:
        new TableCreator(sql, TestAddress).create()
        new TableCreator(sql, TestModelWithForeignKey).create()

        when:
        Long addressId = sql.executeInsert("insert into test_address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        TestModelWithForeignKey testModelWithForeignKey = new TestModelWithForeignKey(sql, "John", "Doe", null, null, "Germany").insert()
        TestAddress testAddress = testModelWithForeignKey.getAddressModel()
        then:
        testAddress.id == addressId
        testAddress.country == "Germany"
        testAddress.city == "Berlin"
        testAddress.street == "Street #1"

        cleanup:
        new TableDropper(sql, TestModelWithForeignKey).drop()
        new TableDropper(sql, TestAddress).drop()
    }

    def "test update foreignKey"() {
        setup:
        new TableCreator(sql, TestAddress).create()
        new TableCreator(sql, TestModelWithForeignKey).create()

        when:
        Long addressId = sql.executeInsert("insert into test_address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        sql.executeInsert("insert into test_fk (first_name, age) values('John', 10)")[0][0].longValue()
        TestModelWithForeignKey testModelWithForeignKey = new Selector(sql, TestModelWithForeignKey).get("first_name", "John")
        testModelWithForeignKey.address = "Germany"
        testModelWithForeignKey.update()
        TestAddress testAddress = testModelWithForeignKey.getAddressModel()
        then:
        testAddress.id == addressId
        testAddress.country == "Germany"
        testAddress.city == "Berlin"
        testAddress.street == "Street #1"

        cleanup:
        new TableDropper(sql, TestModelWithForeignKey).drop()
        new TableDropper(sql, TestAddress).drop()
    }

    def cleanup() {
        sql.close()
    }
}
