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

    void drop(Boolean cascade) {
        String statement = buildStatement(cascade)
        sql.execute(statement)
    }

    private String buildStatement(Boolean cascade) {
        String tableName = protoModel.getTableName()
        String statement = "drop table $tableName"
        if (cascade) {
            statement += " cascade"
        }
        return  statement
    }
}
