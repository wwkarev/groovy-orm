package com.github.wwkarev.gorm.test.models

import com.github.wwkarev.gorm.Model
import com.github.wwkarev.gorm.config.Config
import groovy.sql.Sql

class WorkerWithCustomTableName extends Model {
    String firstName
    String lastName
    Integer age
    Date birthday

    WorkerWithCustomTableName(Sql sql, String firstName, String lastName, Integer age, Date birthday) {
        super(sql)
        this.firstName = firstName
        this.lastName = lastName
        this.age = age
        this.birthday = birthday
    }

    @Override
    Config config() {
        return new Config(tableName: 'worker_custom')
    }
}
