package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.CaseConverter
import spock.lang.Specification

class CaseConverterTest extends Specification {
    def "test camel to snake"() {
        expect:
        CaseConverter.convertFromCamelToSnake(camel) == snake
        where:
        camel|snake
        'abcDeFG'|'abc_de_f_g'
        'a_bcDeFG'|'a_bc_de_f_g'
        ''|''
        'a'|'a'
    }

    def "test pascal to snake"() {
        expect:
        CaseConverter.convertFromCamelToSnake(camel) == snake
        where:
        camel|snake
        'AbcDeFG'|'abc_de_f_g'
        'A_bcDeFG'|'a_bc_de_f_g'
        ''|''
        'A'|'a'
    }

    def "test snake to camel"() {
        expect:
        CaseConverter.convertFromSnakeToCamel(snake) == camel
        where:
        snake|camel
        'a_b_cd_e'|'aBCdE'
        'a_b_cd__e'|'aBCd_E'
        ''|''
        'a'|'a'
    }

    def "test snake to pascal"() {
        expect:
        CaseConverter.convertFromSnakeToPascal(snake) == camel
        where:
        snake|camel
        'a_b_cd_e'|'ABCdE'
        'ab_b_cd__e'|'AbBCd_E'
        ''|''
        'a'|'A'
    }

    def "test camel to pascal"() {
        expect:
        CaseConverter.convertFromCamelToPascal(snake) == camel
        where:
        snake|camel
        'aBCdE'|'ABCdE'
        'abBCd_E'|'AbBCd_E'
        ''|''
        'a'|'A'
    }
}
