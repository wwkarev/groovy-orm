package com.github.wwkarev.gorm

import groovy.sql.Sql
import groovy.transform.PackageScope

/**
 * Provides DROP TABLE functionality
 * @author Vitalii Karev (wwkarev)
 */
@PackageScope
final class TableDropper extends Statement {
    TableDropper(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    void drop() {
        String tableName = protoModel.getTableName()
        String statement = "drop table $tableName"
        sql.execute(statement)
    }
}
