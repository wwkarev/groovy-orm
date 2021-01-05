package com.github.wwkarev.gorm

import groovy.sql.Sql
import groovy.transform.PackageScope

/**
 * Provides DROP TABLE functionality
 * @author Vitalii Karev (wwkarev)
 */
@PackageScope
final class TableTruncater extends Statement {
    TableTruncater(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    void truncate(Boolean cascade) {
        String statement = buildStatement()
        sql.execute(statement)
    }

    private String buildStatement(Boolean cascade) {
        String tableName = protoModel.getTableName()
        String statement = "truncate $tableName"
        if (cascade) {
            statement += " cascade"
        }
        return statement
    }
}
