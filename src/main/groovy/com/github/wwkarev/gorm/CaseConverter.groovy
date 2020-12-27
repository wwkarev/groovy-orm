package com.github.wwkarev.gorm

import groovy.transform.PackageScope

@PackageScope
class CaseConverter {
    static String convertFromPascalToSnake(String pascalCaseStr) {
        return convertFromCamelToSnake(pascalCaseStr)
    }

    static String convertFromCamelToSnake(String camelCaseStr) {
        camelCaseStr.findAll('[A-Z]').each{
            def m = camelCaseStr =~ '([A-Z])'
            if (m.size() > 0) {
                String newLetter = ((String)m[0][1]).toLowerCase()
                camelCaseStr = camelCaseStr.replaceFirst('[A-Z]', "_$newLetter")
            }
        }
        if (camelCaseStr.size() > 1 && camelCaseStr.getAt(0) == '_') {
            camelCaseStr = camelCaseStr.substring(1, camelCaseStr.size())
        }

        return camelCaseStr
    }

    static String convertFromSnakeToCamel(String snakeCaseStr) {
        snakeCaseStr.findAll('_[a-z]').each{
            def m = snakeCaseStr =~ '_([a-z])'
            if (m.size() > 0) {
                String newLetter = m[0][1].toUpperCase()
                snakeCaseStr = snakeCaseStr.replaceFirst('_[a-z]', newLetter)
            }
        }
        return snakeCaseStr
    }

    static String convertFromSnakeToPascal(String snakeCaseStr) {
        String camelCaseStr = convertFromSnakeToCamel(snakeCaseStr)
        return convertFromCamelToPascal(camelCaseStr)
    }

    static String convertFromCamelToPascal(String camelCaseStr) {
        String pascalCaseStr = ""
        if (camelCaseStr.size() > 0) {
            pascalCaseStr = camelCaseStr.charAt(0).toUpperCase().toString()
            pascalCaseStr = camelCaseStr.size() > 1 ? pascalCaseStr + camelCaseStr.substring(1) : pascalCaseStr
        }
        return pascalCaseStr
    }
}
