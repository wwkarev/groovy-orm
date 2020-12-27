package com.github.wwkarev.gorm


import com.github.wwkarev.gorm.config.Config
import groovy.sql.Sql

import java.lang.reflect.Field

final class TableCreator extends Statement {
    TableCreator(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    void create() {
        String statement = buildCreateStatement()
        sql.execute(statement)
    }

    private String buildCreateStatement() {
        Model protoModel = getInstanceOfModel()
        Config config = protoModel.config()
        List<Field> fields =  ModelPropertiesUtil.getFullFieldList(modelClass)

        String tableName = protoModel.getTableName()
        String fieldsStatementRepresentation =
                fields.collect{Field field ->
                    return protoModel.getFieldColumnName(field) + ' ' + getFieldColumnTypeRepresentation(field)
                }.join(', ')
        return "create table $tableName ($fieldsStatementRepresentation)"
    }

    private String getFieldColumnTypeRepresentation(Field field) {
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
            //TODO add to tests
            throw new IllegalArgumentException("Illegal class of attribute $fieldName: $fieldClass")
        }
        return representation
    }
}
