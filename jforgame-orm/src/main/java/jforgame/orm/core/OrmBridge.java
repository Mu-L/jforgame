package jforgame.orm.core;

import java.util.*;

/**
 * 对象与数据库表记录之间的桥梁
 */
public class OrmBridge {
    /**
     * 对应的数据库表名称
     */
    private String tableName;
    /**
     * 缓存所有表字段及其对应的元数据
     */
    private final Map<String, FieldMetaData> fieldMetadataMap = new HashMap<>();
    /**
     * 被覆写的property与表column的映射
     */
    private Map<String, String> propertyToColumnOverride = new HashMap<>();
    /**
     * 被覆写的表column与property的映射
     */
    private Map<String, String> columnToPropertyOverride = new HashMap<>();
    /**
     * 实体所有的主键字段
     */
    private final Set<String> uniqueProperties = new HashSet<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void addFieldMetadata(String field, FieldMetaData metadata) {
        fieldMetadataMap.put(field, metadata);
    }

    public Map<String, FieldMetaData> getFieldMetadataMap() {
        return fieldMetadataMap;
    }

    /**
     * 查询实体的语主键组合
     *
     * @return 实体所有的主键字段
     */
    public List<String> getPrimaryKeyProperties() {
        return new ArrayList<>(this.uniqueProperties);
    }

    public void addUniqueKey(String id) {
        this.uniqueProperties.add(id);
    }

    public void addPropertyColumnOverride(String property, String column) {
        this.propertyToColumnOverride.put(property, column);
        this.columnToPropertyOverride.put(column, property);
    }

    public String getOverrideProperty(String property) {
        return this.propertyToColumnOverride.get(property);
    }

    public Map<String, String> getPropertyToColumnOverride() {
        return propertyToColumnOverride;
    }

    public Map<String, String> getColumnToPropertyOverride() {
        return columnToPropertyOverride;
    }

    public void setColumnToPropertyOverride(Map<String, String> columnToPropertyOverride) {
        this.columnToPropertyOverride = columnToPropertyOverride;
    }

    public void setPropertyToColumnOverride(Map<String, String> propertyToColumnOverride) {
        this.propertyToColumnOverride = propertyToColumnOverride;
    }

    public List<String> listAllProperties() {
        return new ArrayList<>(this.fieldMetadataMap.keySet());
    }

}
