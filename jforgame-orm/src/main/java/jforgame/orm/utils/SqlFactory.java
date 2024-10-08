package jforgame.orm.utils;

import jforgame.orm.FieldMetadata;
import jforgame.orm.OrmBridge;
import jforgame.orm.StatefulEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SqlFactory {

    private static Logger logger = LoggerFactory.getLogger(SqlFactory.class);

    public static String createInsertSql(StatefulEntity entity, OrmBridge bridge) {
        StringBuilder sb = new StringBuilder();

        sb.append(" INSERT INTO ")
                .append(bridge.getTableName()).append(" (");

        List<String> properties = bridge.listProperties();
        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            String column = property;
            if (bridge.getOverrideProperty(property) != null) {
                column = bridge.getOverrideProperty(property);
            }
            sb.append("`").append(column).append("`");
            if (i < properties.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(") VALUES (");

        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            FieldMetadata fieldMetadata = bridge.getFieldMetadataMap().get(property);
            try {
                Object value = fieldMetadata.getField().get(entity);
                if (fieldMetadata.getConverter() != null) {
                    // 进行转换
                    value = fieldMetadata.getConverter().convertToDatabaseColumn(value);
                }
                sb.append("'").append(value).append("'");
                if (i < properties.size() - 1) {
                    sb.append(",");
                }
            } catch (Exception e) {
                logger.error("createInsertSql failed", e);
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String createPreparedInsertSql(StatefulEntity entity, OrmBridge bridge) {
        StringBuilder sb = new StringBuilder();

        sb.append(" INSERT INTO ")
                .append(bridge.getTableName()).append(" (");

        List<String> properties = bridge.listProperties();
        for (int i = 0; i < properties.size(); i++) {
            String property = properties.get(i);
            String column = property;
            if (bridge.getOverrideProperty(property) != null) {
                column = bridge.getOverrideProperty(property);
            }
            sb.append("`").append(column).append("`");
            if (i < properties.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(") VALUES (");

        for (int i = 0; i < properties.size(); i++) {
            sb.append("?");
            if (i < properties.size() - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String createPreparedUpdateSql(StatefulEntity entity, OrmBridge bridge, Object[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append(" UPDATE ").append(bridge.getTableName())
                .append(" SET ");
        sb.append(columnSetterSql(columns));
        sb.append(createWhereClauseSql(entity, bridge));

        return sb.toString();
    }

    private static String columnSetterSql(Object[] columns) {
        StringBuilder sb = new StringBuilder();
        for (Object column : columns) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("`").append(column).append("` = ? ");
        }

        return sb.toString();
    }

    public static String createUpdateSql(StatefulEntity entity, OrmBridge bridge) {
        StringBuilder sb = new StringBuilder();
        sb.append(" UPDATE ").append(bridge.getTableName())
                .append(" SET ");
        sb.append(object2SetterSql(entity, bridge));
        sb.append(createWhereClauseSql(entity, bridge));

        return sb.toString();
    }

    private static String object2SetterSql(StatefulEntity entity, OrmBridge bridge) {
        Set<String> columns = entity.savingColumns();
        StringBuilder sb = new StringBuilder();
        boolean saveAll = entity.isSaveAll() || columns == null || columns.size() <= 0;
        for (Map.Entry<String, FieldMetadata> entry : bridge.getFieldMetadataMap().entrySet()) {
            String property = entry.getKey();
            // 仅持久化部分字段
            if (!saveAll && !columns.contains(property)) {
                continue;
            }
            FieldMetadata metadata = entry.getValue();
            try {
                Object value = metadata.getField().get(entity);
                if (metadata.getConverter() != null) {
                    // 进行转换
                    value = metadata.getConverter().convertToDatabaseColumn(value);
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                String column = entry.getKey();
                if (bridge.getOverrideProperty(property) != null) {
                    column = bridge.getOverrideProperty(property);
                }
                sb.append("`").append(column).append("` = '").append(value).append("'");
            } catch (Exception e) {
                logger.error("object2SetterSql failed", e);
            }
        }

        return sb.toString();
    }

    public static String createDeleteSql(StatefulEntity entity, OrmBridge bridge) {
        StringBuilder sb = new StringBuilder();
        sb.append(" DELETE FROM ")
                .append(bridge.getTableName())
                .append(createWhereClauseSql(entity, bridge));

        return sb.toString();
    }

    private static String createWhereClauseSql(StatefulEntity entity, OrmBridge bridge) {
        StringBuilder sb = new StringBuilder();
        //占位申明，避免拼接sql需要考虑and
        sb.append(" WHERE 1=1");
        List<String> properties = bridge.getQueryProperties();
        for (String property : properties) {
            Object colValue;
            try {
                colValue = ReflectUtils.getMethodValue(entity, property);
                String column = property;
                if (bridge.getOverrideProperty(property) != null) {
                    column = bridge.getOverrideProperty(property);
                }
                sb.append(" AND `").append(column).append("` = '").append(colValue).append("'");
            } catch (Exception e) {
                logger.error("createWhereClauseSql failed", e);
            }
        }
        return sb.toString();
    }

}
