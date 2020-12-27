package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.exceptions.MultipleRecordsFoundException
import com.github.wwkarev.gorm.exceptions.RecordNotFoundException
import com.github.wwkarev.gorm.util.CaseConverter
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

import java.lang.reflect.Field

final class Selector extends Statement {
    Q filterQ
    List<String> orderByColumns

    Selector(Sql sql, Class modelClass) {
        super(sql, modelClass)
    }

    Model get(String fieldName, value) {
        return get(new Q((fieldName): value))
    }

    Model get(Q q) {
        where(q)
        List<Model> models = select()
        if (models.size() == 0) {
            throw new RecordNotFoundException()
        } else if (models.size() > 1) {
            throw new MultipleRecordsFoundException()
        }
        return models[0]
    }

    List<Model> filter(String fieldName, value) {
        return filter(new Q((fieldName): value))
    }

    List<Model> filter(Q q) {
        where(q)
        return select()
    }

    Selector where(String fieldName, value) {
        return where(new Q((fieldName): value))
    }

    Selector where(Q q) {
        filterQ = q
        return this
    }

    Selector orderBy(List<String> fieldNames) {
        def ASC_SUFIX = /(.*)__asc$/
        def DESC_SUFIX = /(.*)__desc$/
        orderByColumns = fieldNames.collect{String fieldName ->
            String column = "$fieldName asc"
            def ascRegex = fieldName =~ ASC_SUFIX
            def descRegex = fieldName =~ DESC_SUFIX
            if (ascRegex.size() > 0) {
                column = "${ascRegex[0][1]} asc"
            }
            if (descRegex.size() > 0) {
                column = "${descRegex[0][1]} desc"
            }
            return column
        }
        return this
    }

    Selector orderBy(String fieldName) {
        return orderBy([fieldName])
    }

    List<Model> select() {
        String statement = buildStatement()
        List<Object> statementParams = getStatementParams()
        List<GroovyRowResult> rowResults = sql.rows(statement, statementParams)
        return buildModels(rowResults)
    }

    private String buildStatement() {
        Model protoModel = getInstanceOfModel()
        List<Field> fields =  ModelPropertiesUtil.getFullFieldList(modelClass)

        String tableName = protoModel.getTableName()
        String valuesPart = generateSelectValuesPart(protoModel, fields)

        String statement = "select $valuesPart from $tableName"
        if (filterQ) {
            statement += " where ${filterQ.getParam()}"
        }
        if (orderByColumns?.size() > 0) {
            statement += " order by ${orderByColumns.join(', ')}"
        }
        return statement
    }

    private generateSelectValuesPart(Model protoModel, List<Field> fields) {
        return fields.collect{Field field ->
            return protoModel.getFieldColumnName(field) + ' as ' + CaseConverter.convertFromPascalToSnake(field.getName())
        }.join(', ')
    }

    private List<Object> getStatementParams() {
        List<Object> parmas = []
        if (filterQ) {
            parmas = filterQ.getValues()
        }
        return parmas
    }

    private List<Model> buildModels(List<GroovyRowResult> rowResults) {
        return rowResults.collect{GroovyRowResult result ->
            Model model = getInstanceOfModel()
            model."setSql"(sql)
            result.each{String column_alias, Object value ->
                model."set${CaseConverter.convertFromSnakeToPascal(column_alias)}"(value)
                model.initServiceMethods()
            }
            return model
        }
    }
}
