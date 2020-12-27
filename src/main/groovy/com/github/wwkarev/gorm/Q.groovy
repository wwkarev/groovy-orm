package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.util.CaseConverter

class Q {
    private String param
    private List<Object> values
    private def GREATER_SUFIX = /(.*)__g$/
    private def GREATER_OR_EQUALS_SUFIX = /(.*)__ge$/
    private def LESS_SUFIX = /(.*)__l$/
    private def LESS_OR_EQUALS_SUFIX = /(.*)__le$/
    private def IS_NULL_SUFIX = /(.*)__is_null$/
    private def IN_SUFIX = /(.*)__in$/


    Q(Map<String, Object> map) {
        values = []
        List<String> fields = []
        map.each{fieldName, value ->
            fields.add(getWhereStatementByField(fieldName, value))
            if ((fieldName =~ IN_SUFIX).size() > 0) {
                values.addAll((List)value)
            } else if ((fieldName =~ IS_NULL_SUFIX).size() == 0) {
                values.add(value)
            }
        }
        param = fields.join(' and ')
    }

    String getParam() {
        return param
    }

    List<Object> getValues() {
        return values
    }

    private String getWhereStatementByField(String fieldName, Object value) {
        String operator = "= ?"
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

        return  CaseConverter.convertFromCamelToSnake(fieldName) + " $operator"
    }

    Q and(Q q) {
        param = "($param) and (${q.getParam()})"
        values.addAll(q.getValues())
        return this
    }

    Q or(Q q) {
        param = "($param) or (${q.getParam()})"
        values.addAll(q.getValues())
        return this
    }

    Q bitwiseNegate() {
        param = "not ($param)"
        return this
    }
}
