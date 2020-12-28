package com.github.wwkarev.gorm.test.models

import com.github.wwkarev.gorm.Model
import com.github.wwkarev.gorm.config.ColumnConfig
import com.github.wwkarev.gorm.config.Config
import com.github.wwkarev.gorm.config.ForeignKey
import groovy.sql.Sql
import groovy.transform.InheritConstructors

@InheritConstructors
class WorkerWithForeignKey extends Model {
    String firstName
    String lastName
    Integer age
    Date birthday
    String address

    WorkerWithForeignKey(Sql sql, String firstName, String lastName, Integer age, Date birthday, String address) {
        super(sql)
        this.firstName = firstName
        this.lastName = lastName
        this.age = age
        this.birthday = birthday
        this.address = address
    }

    @Override
    Config config() {
        return new Config(
                tableName: 'test_fk',
                columns: [
                        lastName: new ColumnConfig(columnName: 'family_name'),
                        address: new ColumnConfig(columnName: 'address_id', foreignKey: new ForeignKey(dest: Address, destColumnName: 'country'))
                ]
        )
    }
}
