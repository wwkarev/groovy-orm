package com.github.wwkarev.gorm.config


/**
 * Foreign key property
 * @author Vitalii Karev (wwkarev)
 */
class ForeignKey {
    Class dest
    String destColumnName = 'id'
    String getterName
}
