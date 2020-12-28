package com.github.wwkarev.gorm

import groovy.transform.PackageScope

/**
 * Q objects is used to build complex where query
 * @author Vitalii Karev (wwkarev)
 */
class Q {
    private String statement
    private List<WhereInfo> whereInfoList
    private List<Object> values
    private def GREATER_SUFIX = /(.*)__g$/
    private def GREATER_OR_EQUALS_SUFIX = /(.*)__ge$/
    private def LESS_SUFIX = /(.*)__l$/
    private def LESS_OR_EQUALS_SUFIX = /(.*)__le$/
    private def IS_NULL_SUFIX = /(.*)__is_null$/
    private def IN_SUFIX = /(.*)__in$/


    Q(Map<String, Object> map) {
        values = []
        whereInfoList = []
        map.each{fieldName, value ->
            whereInfoList.add(getSubStatementInfo(fieldName, value))
            if ((fieldName =~ IN_SUFIX).size() > 0) {
                values.addAll((List)value)
            } else if ((fieldName =~ IS_NULL_SUFIX).size() == 0) {
                values.add(value)
            }
        }
        statement = whereInfoList.collect{it.id}.join(' and ')
    }

    String getStatement() {
        return statement
    }

    List<WhereInfo> getSubStatementInfoList() {
        return whereInfoList
    }

    List<Object> getValues() {
        return values
    }

    private WhereInfo getSubStatementInfo(String fieldName, Object value) {
        String id = UUID.randomUUID().toString()
        String operator = '= ?'

        def greaterRegex = fieldName =~ GREATER_SUFIX
        def greaterOrEqualsRegex = fieldName =~ GREATER_OR_EQUALS_SUFIX
        def lessRegex = fieldName =~ LESS_SUFIX
        def lessOrEqualsRegex = fieldName =~ LESS_OR_EQUALS_SUFIX
        def isNullRegex = fieldName =~ IS_NULL_SUFIX
        def inRegex = fieldName =~ IN_SUFIX
        if (greaterRegex.size() > 0) {
            operator = "> ?"
            fieldName = greaterRegex[0][1]
        } else if (greaterOrEqualsRegex.size() > 0) {
            operator = ">= ?"
            fieldName = greaterOrEqualsRegex[0][1]
        } else if (lessRegex.size() > 0) {
            operator = "< ?"
            fieldName = lessRegex[0][1]
        } else if (lessOrEqualsRegex.size() > 0) {
            operator = "<= ?"
            fieldName = lessOrEqualsRegex[0][1]
        } else if (isNullRegex.size() > 0) {
            if (value) {
                operator = "is null"
            } else {
                operator = "is not null"
            }
            fieldName = isNullRegex[0][1]
        } else if (inRegex.size() > 0) {
            operator = "in (${((List)value).collect{'?'}.join(', ')})"
            fieldName = inRegex[0][1]
        }

        return new WhereInfo(id: id, fieldName: fieldName, operator: operator)
    }

    Q and(Q q) {
        statement = "($statement) and (${q.getStatement()})"
        whereInfoList.addAll(q.getSubStatementInfoList())
        values.addAll(q.getValues())
        return this
    }

    Q or(Q q) {
        statement = "($statement) or (${q.getStatement()})"
        whereInfoList.addAll(q.getSubStatementInfoList())
        values.addAll(q.getValues())
        return this
    }

    Q bitwiseNegate() {
        statement = "not ($statement)"
        return this
    }

    @PackageScope
    static class WhereInfo {
        String id
        String fieldName
        String operator
    }
}
