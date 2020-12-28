package com.github.wwkarev.gorm.test.models

import com.github.wwkarev.gorm.Model
import com.github.wwkarev.gorm.config.Config
import groovy.sql.Sql
import groovy.transform.InheritConstructors

@InheritConstructors
class Address extends Model {
    String country
    String city
    String street
    Integer houseNumber

    @Override
    Config config() {
        return new Config()
    }
}
