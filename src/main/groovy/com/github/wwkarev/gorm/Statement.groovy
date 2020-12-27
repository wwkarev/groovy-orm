package com.github.wwkarev.gorm

import groovy.sql.Sql
import groovy.transform.PackageScope

@PackageScope
abstract class Statement {
    protected Sql sql
    protected Class modelClass

    Statement(Sql sql, Class modelClass) {
        this.sql = sql
        this.modelClass = modelClass
    }

    protected Model getInstanceOfModel() {
        List<Object> modelParams = (0..modelClass.getConstructors()[0].getParameterCount() - 1).collect{null}
        return modelClass.newInstance(modelParams  as Object[])
    }
}
