package com.github.wwkarev.gorm.test.models

import com.github.wwkarev.gorm.Model
import com.github.wwkarev.gorm.config.ColumnConfig
import com.github.wwkarev.gorm.config.Config
import groovy.sql.Sql

class WorkerWithLo extends Model {
    String firstName
    String lastName
    Integer age
    Date birthday
    String biography

    WorkerWithLo(Sql sql, String firstName, String lastName, Integer age, Date birthday, String biography) {
        super(sql)
        this.firstName = firstName
        this.lastName = lastName
        this.age = age
        this.birthday = birthday
        this.biography = biography
    }

    static WorkerWithLo getInstance(Sql sql) {
        String firstName = UUID.randomUUID().toString()
        String lastName = null
        Integer age = (Math.random() * 100).toLong()
        Date birthDay = new Date()
        String biography = UUID.randomUUID().toString()
        return new WorkerWithLo(sql, firstName, lastName, age, birthDay, biography)
    }

    @Override
    Config config() {
        return new Config(
                columns: [
                        biography: new ColumnConfig(lo: true, columnName: 'bio'),
                        lastName: new ColumnConfig(columnName: 'family_name')
                ]
        )
    }
}
