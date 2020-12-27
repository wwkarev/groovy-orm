package com.github.wwkarev.gorm

import com.github.wwkarev.gorm.config.ColumnConfig
import com.github.wwkarev.gorm.config.Config
import com.github.wwkarev.gorm.config.ForeignKey
import com.github.wwkarev.gorm.util.CaseConverter
import groovy.sql.Sql
import groovy.transform.MapConstructor
import groovy.transform.PackageScope

import java.lang.reflect.Field

@MapConstructor
abstract class Model {
    Sql sql
    Long id

    Model(Sql sql) {
        this.sql = sql
    }

    abstract Config config()

    Model insert() {
        List<UpdateStatementColumnInfo> updateStatementColumnInfoList = getStatementColumnInfoListWithoutId()
        String statement = buildInsertStatement(updateStatementColumnInfoList)
        List<Object> valueList = getValueList(updateStatementColumnInfoList)
        this.id = sql.executeInsert(statement, valueList)[0][0].longValue()
        initServiceMethods()
        return this
    }

    Model update() {
        if (!id) {
            throw new IllegalArgumentException('id is not initialized')
        }

        List<UpdateStatementColumnInfo> updateStatementColumnInfoList = getStatementColumnInfoList()
                .findAll{it.fieldName != 'id'}
        String statement = buildUpdateStatement(updateStatementColumnInfoList)
        List<Object> valueList = getValueList(updateStatementColumnInfoList)
        sql.execute(statement, valueList)
        initServiceMethods()
        return this
    }

    @PackageScope
    String getTableName() {
        return config().tableName ?: CaseConverter.convertFromPascalToSnake(this.getClass().getSimpleName())
    }

    @PackageScope
    String getFieldColumnName(String fieldName) {
        return config().getColumns()?.getAt(fieldName)?.columnName ?: CaseConverter.convertFromCamelToSnake(fieldName)
    }

    @PackageScope
    String getFieldColumnParam(String fieldName) {
        return config().getColumns()?.getAt(fieldName)?.lo ? 'lo_from_bytea(0, ?::bytea)' : '?'
    }

    @PackageScope
    Object getFieldColumnValue(Field field) {
        field.setAccessible(true)
        def value = field.get(this)
        if (field.getType().getName() == Date.getName()) {
            value = value ? ((Date)value).toTimestamp() : value
        }
        return value
    }

    private List<UpdateStatementColumnInfo> getStatementColumnInfoListWithoutId() {
        return getStatementColumnInfoList()
                .findAll{it.fieldName != 'id'}
    }

    private List<UpdateStatementColumnInfo> getStatementColumnInfoList() {
        return ModelPropertiesUtil.getFullFieldList(this.getClass()).collect { field ->
            String fieldName = field.getName()
            return new UpdateStatementColumnInfo(
                    fieldName: field.getName(),
                    name: getFieldColumnName(fieldName),
                    param: getFieldColumnParam(fieldName),
                    value: getFieldColumnValue(field)
            )
        }
    }

    private String buildInsertStatement(List<UpdateStatementColumnInfo> updateStatementColumnInfoList) {
        String tableName = getTableName()
        String insertStatementColumnNamePart = updateStatementColumnInfoList.collect{it.name}.join(', ')
        String insertStatementColumnParamPart = updateStatementColumnInfoList.collect{it.param}.join(', ')

        return "insert into $tableName ($insertStatementColumnNamePart) values($insertStatementColumnParamPart)"
    }

    private String buildUpdateStatement(List<UpdateStatementColumnInfo> updateStatementColumnInfoList) {
        String tableName = getTableName()
        String updateStatementColumnParamPart = updateStatementColumnInfoList.collect{it.name + " = " + it.param}.join(', ')

        return "update $tableName set $updateStatementColumnParamPart where id = $id"
    }

    private List<Object> getValueList(List<UpdateStatementColumnInfo> updateStatementColumnInfoList) {
        return updateStatementColumnInfoList.collect{it.value}
    }

    @PackageScope
    void initServiceMethods() {
        initForeignKeyGetters()
    }

    private void initForeignKeyGetters() {
        Config config = config()
        ModelPropertiesUtil.getFullFieldList(getClass()).each{Field field ->
            String fieldName = field.getName()
            ForeignKey foreignKey = config?.columns?.getAt(fieldName)?.foreignKey
            if (foreignKey) {
                field.setAccessible(true)
                Object destRecordId = field.get(this)
                if (destRecordId) {
                    this.metaClass."get${CaseConverter.convertFromCamelToPascal(fieldName)}Model" = {->
                        return new Selector(sql, foreignKey.dest).get(foreignKey.destColumnName, destRecordId)
                    }
                }
            }
        }
    }
}
