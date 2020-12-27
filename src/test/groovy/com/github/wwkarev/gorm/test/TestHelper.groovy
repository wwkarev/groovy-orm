package com.github.wwkarev.gorm.test

import com.github.wwkarev.gorm.Model

class TestHelper {
    static Class getModelClass(Class cls) {
        if (cls == Model) {
            return cls
        } else {
            return getModelClass(cls.getSuperclass())
        }
    }

    static convertLargeObject(sql, loObjectNumber) {
        def result = null
        def selectStatement = "SELECT convert_from(lo_get($loObjectNumber), 'UTF-8')".toString()
        def selectResult = sql.rows(selectStatement)
        if (selectResult.size() != 0) {
            result = selectResult[0]["convert_from"]
        }
        return result
    }
}
