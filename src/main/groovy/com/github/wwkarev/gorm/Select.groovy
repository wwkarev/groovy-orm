package com.github.wwkarev.gorm

/**
 * Provides SELECT functionality
 * @author Vitalii Karev (wwkarev)
 */
class Select {
    static Model get(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).get(fieldName, value)
    }

    static Model get(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).get(q)
    }

    static List<Model> filter(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).filter(fieldName, value)
    }

    static List<Model> filter(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).filter(q)
    }

    static Selector where(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).where(fieldName, value)
    }

    static Selector where(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).where(q)
    }

    static Selector orderBy(sql, Class modelClass, List<String> fieldNames) {
        return new Selector(sql, modelClass).orderBy(fieldNames)
    }

    static Selector orderBy(sql, Class modelClass, String fieldName) {
        return new Selector(sql, modelClass).orderBy(fieldName)
    }

    static List<Model> select(sql, Class modelClass) {
        return new Selector(sql, modelClass).select()
    }
}
