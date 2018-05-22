package com.service.cap;

import com.zzg.mybatis.generator.controller.JavaTypeConverter;
import com.zzg.mybatis.generator.model.UITableColumnVO;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benjamin on 2018/1/9.
 */

public class CapTypeConverter implements JavaTypeConverter {
    private static final Logger LOG = LoggerFactory.getLogger(CapTypeConverter.class);
    List<String> tableNameWithoutBlob = Arrays.asList(new String[]{});
    List<String> blobColumn = Arrays.asList(new String[]{});
    private List<IgnoredColumn> ignoredColumns;
    private List<ColumnOverride> columnOverrides;

    public List<IgnoredColumn> getIgnoredColumns() {
        return ignoredColumns;
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public List<ColumnOverride> getColumnOverrides() {
        return columnOverrides;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }

    public void convert(List<UITableColumnVO> tableColumns, String tableName) {

        LOG.info("cap autoConvertJavaType");

        // If select same schema and another table, update table data
        List<UITableColumnVO> items = tableColumns;

        tableName = tableName.toLowerCase();
        final String tableNameLowerCase = tableName;

        if (items != null && items.size() > 0) {
            List<IgnoredColumn> ignoredColumns = new ArrayList<>();
            List<ColumnOverride> columnOverrides = new ArrayList<>();

            items.stream().forEach(item -> {
                String columnName = item.getColumnName().toLowerCase();
                String jdbcType = item.getJdbcType().toLowerCase();

                if (jdbcType.equals("blob")
                        && tableNameWithoutBlob.contains(tableNameLowerCase)
                        && !(blobColumn.contains(columnName))) {
                    IgnoredColumn ignoredColumn = new IgnoredColumn(item.getColumnName());
                    LOG.info("ignore blob column {} in {}", columnName, tableNameLowerCase);
                    ignoredColumns.add(ignoredColumn);
                } else if (item.getChecked() && item.getJavaType() == null && item.getTypeHandle() == null && item.getPropertyName() == null) {

                    if (jdbcType.startsWith("nvarchar")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        columnOverride.setJdbcType("NVARCHAR");
                        columnOverride.setJavaType("String");
                        columnOverrides.add(columnOverride);
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    } else if (jdbcType.equals("number")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());

                        if (columnName.contains("amount") || columnName.contains("price")) {
                            columnOverride.setJavaType("Double");
                        } else {
                            columnOverride.setJavaType("Long");
                        }
                        columnOverrides.add(columnOverride);
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());
                    } else if (item.getJdbcType().toLowerCase().equals("float")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        columnOverrides.add(columnOverride);
                        columnOverride.setJavaType("Double");
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    }
                }
            });

            this.ignoredColumns = ignoredColumns;
            this.columnOverrides = columnOverrides;

        }


    }
}
