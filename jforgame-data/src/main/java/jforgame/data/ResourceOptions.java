package jforgame.data;

/**
 * Data resource loading configuration.
 * This is a plain configuration object, not dependent on Spring Boot's configuration binding capability.
 */
public class ResourceOptions {

    /**
     * Resource root directory
     */
    private String location = "csv/";

    /**
     * File suffix (default)
     */
    private String suffix = ".csv";

    /**
     * Scan path for configuration entity {@link jforgame.data.annotation.DataTable}
     */
    private String tableScanPath;

    /**
     * Scan path for configuration container {@link Container} subclasses
     */
    private String containerScanPath;

    /**
     * Common constant table name, default is common.csv/common.xlsx
     */
    private String commonTableName = "common";

    /**
     * decide whether to report an {@link IllegalStateException} when the configuration file corresponding to the Config Table does not exist
     * @since 4.1.0
     */
    private boolean ignoreConfig = true;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getTableScanPath() {
        return tableScanPath;
    }

    public void setTableScanPath(String tableScanPath) {
        this.tableScanPath = tableScanPath;
    }

    public String getContainerScanPath() {
        return containerScanPath;
    }

    public void setContainerScanPath(String containerScanPath) {
        this.containerScanPath = containerScanPath;
    }

    public String getCommonTableName() {
        return commonTableName;
    }

    public void setCommonTableName(String commonTableName) {
        this.commonTableName = commonTableName;
    }

    public boolean isIgnoreConfig() {
        return ignoreConfig;
    }

    public void setIgnoreConfig(boolean ignoreConfig) {
        this.ignoreConfig = ignoreConfig;
    }
}
