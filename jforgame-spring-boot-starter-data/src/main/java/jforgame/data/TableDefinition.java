package jforgame.data;

import jforgame.data.annotation.DataTable;
import jforgame.data.annotation.Id;
import jforgame.data.annotation.Index;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TableDefinition {

    /**
     * 表格唯一主键
     */
    private IdMeta idMeta;

    /**
     * 表格索引列
     */
    private final Map<String, IndexMeta> indexMetaMap = new HashMap<>();

    private Class clazz;

    /**
     * 配置表名称（默认为类名小写）
     */
    private String resourceTable;

    public TableDefinition(Class clazz) {
        this.clazz = clazz;
        this.init();
    }

    private void init() {
        DataTable dataTable = (DataTable) clazz.getAnnotation(DataTable.class);
        if (StringUtils.isEmpty(dataTable.name())) {
            this.resourceTable = clazz.getSimpleName().toLowerCase();
        } else {
            this.resourceTable = dataTable.name();
        }

        Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(Id.class) != null)
                .forEach(f -> {
                    IdMeta indexMeta = new FieldIdMeta(f);
                    String key = indexMeta.getName();
                    if (idMeta != null) {
                        throw new RuntimeException(String.format("%s类主键重复-->%s", clazz.getName(), key));
                    }
                    idMeta = indexMeta;
                });

        if (idMeta == null) {
            throw new RuntimeException(String.format("%s类主键不存在", clazz.getName()));
        }
        Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.getAnnotation(Index.class) != null)
                .forEach(f -> {
                    IndexMeta indexMeta = new FieldIndexMeta(f);
                    String key = indexMeta.getName();
                    if (indexMetaMap.put(key, indexMeta) != null) {
                        throw new RuntimeException(String.format("%s类索引重复-->%s", clazz.getName(), key));
                    }
                    indexMetaMap.put(key, indexMeta);
                });

        Arrays.stream(clazz.getDeclaredMethods()).filter(m -> m.getAnnotation(Index.class) != null)
                .forEach(m -> {
                    Index index = m.getAnnotation(Index.class);
                    IndexMeta indexMeta = new MethodIndexMeta(index, m);
                    String key = indexMeta.getName();
                    if (indexMetaMap.put(key, indexMeta) != null) {
                        throw new RuntimeException(String.format("%s类索引重复-->%s", clazz.getName(), key));
                    }
                    indexMetaMap.put(key, indexMeta);
                });
    }

    IdMeta getIdMeta() {
        return idMeta;
    }

    Map<String, IndexMeta> getIndexMetaMap() {
        return indexMetaMap;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getResourceTable() {
        return resourceTable;
    }
}
