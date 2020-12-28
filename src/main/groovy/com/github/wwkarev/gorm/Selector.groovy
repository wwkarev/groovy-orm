package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.exceptions.MultipleRecordsFoundException
import com.github.wwkarev.gorm.exceptions.RecordNotFoundException
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.PackageScope

import java.lang.reflect.Field


/**
 * Provides SELECT functionality
 * @author Vitalii Karev (wwkarev)
 */
@PackageScope
final class Selector extends Statement {
    Q filterQ
    List<OrderByStatement> orderByStatementList

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
        orderByStatementList = fieldNames.collect{String fieldName ->
            String mode = "asc"
            def ascRegex = fieldName =~ ASC_SUFIX
            def descRegex = fieldName =~ DESC_SUFIX
            if (ascRegex.size() > 0) {
                fieldName = ascRegex[0][1]
            }
            if (descRegex.size() > 0) {
                fieldName = descRegex[0][1]
                mode = "desc"
            }
            return new OrderByStatement(fieldName: fieldName, mode: mode)
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
        List<Field> fields =  Model.getFullFieldList(modelClass)

        String tableName = protoModel.getTableName()
        String valuesPart = generateSelectValuesPart(fields)
        String wherePart = generateSelectWherePart()
        String orderByPart = generateSelectOrderByPart()

        String statement = "select $valuesPart from ${tableName}${wherePart}${orderByPart}"
        return statement
    }

    private String generateSelectValuesPart(List<Field> fields) {
        return fields.collect{Field field ->
            String fieldName = field.getName()
            return protoModel.getFieldColumnName(fieldName) + ' as ' + CaseConverter.convertFromPascalToSnake(fieldName)
        }.join(', ')
    }

    private String generateSelectWherePart() {
        String wherePart = ""
        if (filterQ) {
            wherePart = " where ${filterQ.getStatement()}"
            filterQ.getSubStatementInfoList().each{ Q.WhereInfo whereInfo ->
                wherePart = wherePart.replace(whereInfo.id, protoModel.getFieldColumnName(whereInfo.fieldName) + ' ' + whereInfo.operator)
            }
        }

        return wherePart
    }

    private String generateSelectOrderByPart() {
        String orderByPart = ""
        if (orderByStatementList?.size() > 0) {
            String orderBySubStatement = orderByStatementList
                    .collect{OrderByStatement orderByStatement ->
                        return "${protoModel.getFieldColumnName(orderByStatement.fieldName)} ${orderByStatement.mode}"
                    }
                    .join(', ')
            orderByPart = " order by ${orderBySubStatement}"
        }

        return orderByPart
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
            }
            model.initServiceMethods()
            return model
        }
    }

    @PackageScope
    static class OrderByStatement {
        String fieldName
        String mode
    }
}
