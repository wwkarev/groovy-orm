package com.github.wwkarev.gorm.config

import com.sun.org.apache.xpath.internal.operations.Bool
/**
 * Column config
 * @author Vitalii Karev (wwkarev)
 */
class ColumnConfig {
    String columnName
    Boolean lo = false
    ForeignKey foreignKey
}
