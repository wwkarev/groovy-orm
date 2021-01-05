package com.github.wwkarev.gorm

import groovy.sql.Sql

/**
 * Provides table management functionality
 * @author Vitalii Karev (wwkarev)
 */
final class Table {
    static void create(Sql sql, Class modelClass) {
        new TableCreator(sql, modelClass).create()
    }
    static void drop(Sql sql, Class modelClass, Boolean cascade = true) {
        new TableDropper(sql, modelClass).drop(cascade)
    }
}
