package com.github.wwkarev.gorm

import groovy.sql.Sql

final class TableDropper extends Statement {
    TableDropper(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    void drop() {
        Model protoModel = getInstanceOfModel()
        String tableName = protoModel.getTableName()
        String statement = "drop table $tableName"
        sql.execute(statement)
    }
}
