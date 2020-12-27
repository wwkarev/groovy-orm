package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.test.models.TestModel
import spock.lang.Specification

import java.lang.reflect.Field

class ModelPropertiesUtilTest extends Specification {
    def 'test ModelPropertiesUtil.getFullFieldList()'() {
        when:
        Model testModel = new TestModel(null, null, null, null, null)
        List<Field> fields = ModelPropertiesUtil.getFullFieldList(testModel.getClass())
        List<String> destFieldNames = ['id', 'firstName', 'lastName', 'age', 'birthday']
        then:
        assert fields.size() == destFieldNames.size()
        fields.each{Field field ->
            assert destFieldNames.contains(field.getName()) == true
        }
    }
}
