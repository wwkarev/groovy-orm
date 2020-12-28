package com.github.wwkarev.gorm

import groovy.sql.Sql
import groovy.transform.PackageScope

/**
 * Base class for statement classes
 * @author Vitalii Karev (wwkarev)
 */
@PackageScope
abstract class Statement {
    protected Sql sql
    protected Class modelClass
    protected Model protoModel

    Statement(Sql sql, Class modelClass) {
        this.sql = sql
        this.modelClass = modelClass
        protoModel = getInstanceOfModel()
    }

    protected Model getInstanceOfModel() {
        List<Object> modelParams = (0..modelClass.getConstructors()[0].getParameterCount() - 1).collect{null}
        return modelClass.newInstance(modelParams  as Object[])
    }
}
