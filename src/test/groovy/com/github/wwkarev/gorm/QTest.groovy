package com.github.wwkarev.gorm

import spock.lang.Specification

class QTest extends Specification {
    def 'test QTest'() {
        when:
        Integer i1 = 4
        Double d1 = 0
        String s1 = '3'
        Boolean b_false = false
        Boolean b_true = true
        Date date_1 = new Date()
        List expressionResultList = [
                [new Q(a: i1, b__g: s1) | ~ new Q(c__le: d1), '(a = ? and b > ?) or (not (c <= ?))', [i1, s1, d1]],
                [new Q(aBC: b_false) & new Q(deF: i1, g__is_null: false) | new Q(f__le: date_1), '((a_b_c = ?) and (de_f = ? and g is not null)) or (f <= ?)', [b_false, i1, date_1]],
                [new Q(a: i1, b__g: s1) | ~ new Q(c__in: [d1, i1]), '(a = ? and b > ?) or (not (c in (?, ?)))', [i1, s1, d1, i1]],
        ]


        then:
        expressionResultList.each{
            assert ((Q)it[0]).getParam() == it[1]
            ((Q)it[0]).getValues().eachWithIndex{ def entry, int i ->
                if (entry.getClass() == Date) {
                    assert ((Date)it[2][i]).getTime() == ((Date)entry).getTime()
                } else {
                    assert it[2][i] == entry
                }
            }
        }
    }
}
