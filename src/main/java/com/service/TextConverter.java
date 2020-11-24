package com.service;

import com.zzg.mybatis.generator.controller.JavaTypeConverter;
import com.zzg.mybatis.generator.model.UITableColumnVO;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Benjamin Zheng
 * @Date: 2020/5/7 17:09
 */
public class TextConverter implements JavaTypeConverter {

    private static final Logger LOG = LoggerFactory.getLogger(TextConverter.class);
    List<String> blobColumn = new ArrayList<>();
    List<ColumnOverride> columnOverrides;
    private List<IgnoredColumn> ignoredColumns;

    boolean ignoreBlobColumn;
    @Override
    public void setIgnoreBlobColumn(boolean ignoreBlobColumn) {
        this.ignoreBlobColumn = ignoreBlobColumn;
    }

    @Override
    public void convert(List<UITableColumnVO> tableColumns, String tableName) {

        List<UITableColumnVO> items = tableColumns;

        tableName = tableName.toLowerCase();
        final String tableNameLowerCase = tableName;

        if (items != null && items.size() > 0) {
            List<IgnoredColumn> ignoredColumns = new ArrayList<>();
            List<ColumnOverride> columnOverrides = new ArrayList<>();

            items.stream().forEach(item -> {
                String columnName = item.getColumnName().toLowerCase();
                String jdbcType = item.getJdbcType().toLowerCase();

                if (jdbcType.contains("blob")
                        && !(blobColumn.contains(columnName))) {
                    IgnoredColumn ignoredColumn = new IgnoredColumn(item.getColumnName());
                    LOG.info("ignore blob column {} in {}", columnName, tableNameLowerCase);
                    ignoredColumns.add(ignoredColumn);
                }
            });
            this.ignoredColumns = ignoredColumns;
        }

        columnOverrides = tableColumns.stream().filter(t -> t.getJdbcType().toLowerCase().equals("text"))
                .map(t -> {
                    ColumnOverride columnOverride = new ColumnOverride(t.getColumnName());
                    columnOverride.setJdbcType("VARCHAR");
                    columnOverride.setJavaType("String");
                    LOG.info("convert {} {} java Type to {} in table {}", columnOverride.getColumnName(), t.getJdbcType(),
                            columnOverride.getJavaType(), tableNameLowerCase);
                    return columnOverride;
                }).collect(Collectors.toList());
    }

    @Override
    public List<IgnoredColumn> getIgnoredColumns() {
        return ignoredColumns;
    }

    @Override
    public List<ColumnOverride> getColumnOverrides() {
        return columnOverrides;
    }
}
