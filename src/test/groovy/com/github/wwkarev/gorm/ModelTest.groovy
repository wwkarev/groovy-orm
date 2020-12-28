package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.test.db.C
import com.github.wwkarev.gorm.test.db.DBConfig
import com.github.wwkarev.gorm.test.models.Address
import com.github.wwkarev.gorm.test.models.Worker
import com.github.wwkarev.gorm.test.models.WorkerWithForeignKey
import com.github.wwkarev.gorm.test.models.WorkerWithLo
import com.github.wwkarev.gorm.test.TestHelper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Field
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
        new TableCreator(sql, WorkerWithLo).create()
        WorkerWithLo model = WorkerWithLo.getInstance(sql)
        model.insert()

        GroovyRowResult rowResult = sql.firstRow("select * from worker_with_lo where id = ${model.id}")

        new TableDropper(sql, WorkerWithLo).drop()
        then:
        model.firstName == rowResult.first_name
        model.lastName == rowResult.family_name
        model.age == rowResult.age
        model.birthday.getTime() == rowResult.birthday.getTime()
        model.biography == TestHelper.convertLargeObject(sql, rowResult.bio)
    }

    def "test insert statement"() {
        when:
        Model model = WorkerWithLo.getInstance(sql)

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
        statement == 'insert into worker_with_lo (first_name, family_name, age, birthday, bio) values(?, ?, ?, ?, lo_from_bytea(0, ?::bytea))'
        valueList[0] == model.firstName
        valueList[1] == model.lastName
        valueList[2] == model.age
        valueList[3].getTime() == model.birthday.toTimestamp().getTime()
        valueList[4] == model.biography
    }

    def "test update"() {
        when:
        new TableCreator(sql, WorkerWithLo).create()

        Long id = sql.executeInsert("insert into worker_with_lo (first_name) values(null)")[0][0].longValue()

        WorkerWithLo model = WorkerWithLo.getInstance(sql)
        model.id = id
        model.update()

        GroovyRowResult rowResult = sql.firstRow("select * from worker_with_lo where id = ${model.id}")

        new TableDropper(sql, WorkerWithLo).drop()
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
        Model model = WorkerWithLo.getInstance(sql)
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
        statement == "update worker_with_lo set first_name = ?, family_name = ?, age = ?, birthday = ?, bio = lo_from_bytea(0, ?::bytea) where id = $modelId"
        valueList[0] == model.firstName
        valueList[1] == model.lastName
        valueList[2] == model.age
        valueList[3].getTime() == model.birthday.toTimestamp().getTime()
        valueList[4] == model.biography
    }

    def "test insert foreignKey"() {
        setup:
        new TableCreator(sql, Address).create()
        new TableCreator(sql, WorkerWithForeignKey).create()

        when:
        Long addressId = sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        WorkerWithForeignKey testModelWithForeignKey = new WorkerWithForeignKey(sql, "John", "Doe", null, null, "Germany").insert()
        Address testAddress = testModelWithForeignKey.getAddressModel()
        then:
        testAddress.id == addressId
        testAddress.country == "Germany"
        testAddress.city == "Berlin"
        testAddress.street == "Street #1"

        cleanup:
        new TableDropper(sql, WorkerWithForeignKey).drop()
        new TableDropper(sql, Address).drop()
    }

    def "test update foreignKey"() {
        setup:
        new TableCreator(sql, Address).create()
        new TableCreator(sql, WorkerWithForeignKey).create()

        when:
        Long addressId = sql.executeInsert("insert into address (country, city, street) values('Germany', 'Berlin', 'Street #1')")[0][0].longValue()
        sql.executeInsert("insert into test_fk (first_name, age) values('John', 10)")[0][0].longValue()
        WorkerWithForeignKey testModelWithForeignKey = new Selector(sql, WorkerWithForeignKey).get("first_name", "John")
        testModelWithForeignKey.address = "Germany"
        testModelWithForeignKey.update()
        Address testAddress = testModelWithForeignKey.getAddressModel()
        then:
        testAddress.id == addressId
        testAddress.country == "Germany"
        testAddress.city == "Berlin"
        testAddress.street == "Street #1"

        cleanup:
        new TableDropper(sql, WorkerWithForeignKey).drop()
        new TableDropper(sql, Address).drop()
    }
    def 'test ModelPropertiesUtil.getFullFieldList()'() {
        when:
        Model testModel = new Worker(null, null, null, null, null)
        List<Field> fields = Model.getFullFieldList(testModel.getClass())
        List<String> destFieldNames = ['id', 'firstName', 'lastName', 'age', 'birthday']
        then:
        assert fields.size() == destFieldNames.size()
        fields.each{Field field ->
            assert destFieldNames.contains(field.getName()) == true
        }
    }

    def cleanup() {
        sql.close()
    }
}
