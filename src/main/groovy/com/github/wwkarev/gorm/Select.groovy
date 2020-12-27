package com.github.wwkarev.gorm

class Select {
    Model get(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).get(fieldName, value)
    }

    Model get(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).get(q)
    }

    List<Model> filter(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).filter(fieldName, value)
    }

    List<Model> filter(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).filter(q)
    }

    Selector where(sql, Class modelClass, String fieldName, value) {
        return new Selector(sql, modelClass).where(fieldName, value)
    }

    Selector where(sql, Class modelClass, Q q) {
        return new Selector(sql, modelClass).where(q)
    }

    Selector orderBy(sql, Class modelClass, List<String> fieldNames) {
        return new Selector(sql, modelClass).orderBy(fieldNames)
    }

    Selector orderBy(sql, Class modelClass, String fieldName) {
        return new Selector(sql, modelClass).orderBy(fieldName)
    }

    List<Model> select(sql, Class modelClass) {
        return new Selector(sql, modelClass).select()
    }
}
