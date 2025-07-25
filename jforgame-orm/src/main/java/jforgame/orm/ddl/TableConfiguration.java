package jforgame.orm.ddl;

import jforgame.commons.StringUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class TableConfiguration {

    private Map<String, TableDefinition> tables = new HashMap<>();

    public void register(Set<Class<?>> codeTables) {
        for (Class<?> entity : codeTables) {
            String tableName = entity.getSimpleName();
            Entity annotation = entity.getAnnotation(Entity.class);
            if (StringUtil.isNotEmpty(annotation.name())) {
                tableName = annotation.name();
            }
            TableDefinition tableDefinition = new TableDefinition();
            tableDefinition.setTableName(tableName);
            tables.put(tableName, tableDefinition);
            fillColumns(entity, tableDefinition);
        }
    }

    private void fillColumns(Class<?> entity, TableDefinition tableDefinition) {
        Arrays.stream(entity.getDeclaredFields()).filter(e -> e.getAnnotation(Column.class) != null)
                .forEach(f -> {
                    Column column = f.getAnnotation(Column.class);
                    ColumnDefinition columnDef = new ColumnDefinition();
                    if (StringUtil.isNotEmpty(column.name())) {
                        columnDef.setName(column.name());
                    } else {
                        columnDef.setName(f.getName());
                    }
                    columnDef.setPrimary(f.getAnnotation(Id.class) != null);
                    // 主键，值不能为空
                    if (f.isAnnotationPresent(Id.class)) {
                        columnDef.setNullable(false);
                    } else {
                        columnDef.setNullable(column.nullable());
                    }
                    columnDef.setJdbcType(f.getType(), column.columnDefinition());
                    tableDefinition.addColumn(columnDef);
                });
    }

    public Map<String, TableDefinition> getTables() {
        return tables;
    }

}
