package com.github.wwkarev.gorm

import java.lang.reflect.Field

final class ModelPropertiesUtil {
    static List<Field> getFullFieldList(Class cls) {
        List<Field> fields = getFullFieldListByClass(cls, [])
        return fields.findAll{!it.isSynthetic()}
    }

    private static List<Field> getFullFieldListByClass(Class cls, List<Field> fields) {
        if (cls == Model) {
            fields = cls.declaredFields.findAll{it.getName() == 'id'} + fields
        } else {
            fields = cls.declaredFields + fields
            fields = getFullFieldListByClass(cls.getSuperclass(), fields)
        }
        return fields
    }
}
