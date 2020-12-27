package com.github.wwkarev.gorm


import com.github.wwkarev.gorm.config.Config
import com.github.wwkarev.gorm.exceptions.UnsupportedFieldClassException
import groovy.sql.Sql
import groovy.transform.PackageScope

import java.lang.reflect.Field

@PackageScope
final class TableCreator extends Statement {
    TableCreator(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    void create() {
        String statement = buildCreateStatement()
        sql.execute(statement)
    }

    private String buildCreateStatement() {
        Config config = protoModel.config()
        List<Field> fields =  Model.getFullFieldList(modelClass)

        String tableName = protoModel.getTableName()
        String fieldsStatementRepresentation =
                fields.collect{Field field ->
                    return protoModel.getFieldColumnName(field.getName()) + ' ' + getColumnTypeRepresentation(field)
                }.join(', ')
        return "create table $tableName ($fieldsStatementRepresentation)"
    }

    private String getColumnTypeRepresentation(Field field) {
        String fieldName = field.getName()
        Class fieldClass = field.getType()

        String representation
        if ('id' == fieldName) {
            representation =  "serial primary key"
        } else {
            switch (fieldClass) {
                case String:
                    representation = "text"
                    break
                case [Integer]:
                    representation = "integer"
                    break
                case [Integer, Long]:
                    representation = "bigint"
                    break
                case [Double, Float]:
                    representation = "double precision"
                    break
                case Date:
                    representation = "timestamptz"
                    break
            }
        }
        if (!representation) {
            throw new UnsupportedFieldClassException("Illegal class of field $fieldName: $fieldClass")
        }
        return representation
    }
}
