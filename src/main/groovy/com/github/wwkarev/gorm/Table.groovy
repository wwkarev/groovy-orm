package com.github.wwkarev.gorm

import groovy.sql.Sql

final class Table {
    static void create(Sql sql, Class modelClass) {
        new TableCreator(sql, modelClass).create()
    }
    static void drop(Sql sql, Class modelClass) {
        new TableDropper(sql, modelClass).drop()
    }
}
