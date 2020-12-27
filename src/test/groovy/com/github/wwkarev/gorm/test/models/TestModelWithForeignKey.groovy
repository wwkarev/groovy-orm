package com.github.wwkarev.gorm.test.models

import com.github.wwkarev.gorm.Model
import com.github.wwkarev.gorm.config.ColumnConfig
import com.github.wwkarev.gorm.config.Config
import com.github.wwkarev.gorm.config.ForeignKey
import groovy.sql.Sql
import groovy.transform.InheritConstructors

@InheritConstructors
class TestModelWithForeignKey extends Model {
    String firstName
    String lastName
    Integer age
    Date birthday
    String address

    @Override
    Config config() {
        return new Config(
                tableName: 'test_fk',
                columns: [
                        lastName: new ColumnConfig(columnName: 'family_name'),
                        address: new ColumnConfig(columnName: 'address_id', foreignKey: new ForeignKey(dest: TestAddress, destColumnName: 'country'))
                ]
        )
    }

    TestAddress getAddressModel() {}
}
