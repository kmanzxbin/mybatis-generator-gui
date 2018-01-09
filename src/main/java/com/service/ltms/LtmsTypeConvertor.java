package com.service.ltms;

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

public class LtmsTypeConvertor {
    private static final Logger _LOG = LoggerFactory.getLogger(LtmsTypeConvertor.class);
    List<String> tableNameWithoutBlob = Arrays.asList(new String[]{"lt_retailer", "lt_distributor", "lt_sub_retailer", "lt_sub_distributor"});
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

        _LOG.info("autoConvertJavaType");

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
                        && !(columnName.equals("profile_photo"))) {
                    IgnoredColumn ignoredColumn = new IgnoredColumn(item.getColumnName());
                    _LOG.info("ignore blob column {} in {}", columnName, tableNameLowerCase);
                    ignoredColumns.add(ignoredColumn);
                } else if (item.getChecked() && item.getJavaType() == null && item.getTypeHandle() == null && item.getPropertyName() == null) {

                    if (jdbcType.startsWith("nvarchar")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        columnOverride.setJavaType("String");
                        columnOverrides.add(columnOverride);
                        _LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    } else if (jdbcType.equals("number")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        // 这个为ltms定制化修改 所有涉及到统计的都使用Long
                        String className = tableNameLowerCase.replace("_", "");
                        if (columnName.endsWith("_id") || columnName.equals("id")) {
                            columnOverride.setJavaType("Long");
                        } else {
                            columnName = columnName.replace("_", "");
                            if (columnName.contains("amount")
                                    || columnName.contains("balance")
                                    || (columnName.endsWith("count") && !columnName.endsWith("account"))
                                    || columnName.endsWith("qnty")
                                    || columnName.contains("salesquantity")
                                    || (columnName.contains("commission") && !columnName.contains("commissionpaid"))
                                    || columnName.equals("refundfee")
                                    || columnName.endsWith("tax")) {
                                columnOverride.setJavaType("Long");
                            } else if (className.endsWith("stat") &&
                                    (columnName.equals("inventory")
                                            || columnName.equals("stockout")
                                            || columnName.equals("stockin"))
                                    ) {
                                columnOverride.setJavaType("Long");
                            } else {
                                columnOverride.setJavaType("Integer");
                            }
                        }
                        columnOverrides.add(columnOverride);
                        _LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());
                    } else if (item.getJdbcType().toLowerCase().equals("float")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        columnOverride.setJavaType("Double");
                        columnOverrides.add(columnOverride);
                        _LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());
//                            } else if (item.getJdbcType().toLowerCase().equals("blob")) {
//                                ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
//                                columnOverride.setJavaType("byte[]");
//                                columnOverrides.add(columnOverride);
//                                _LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    }
                }
            });

            this.ignoredColumns = ignoredColumns;
            this.columnOverrides = columnOverrides;

        }


    }
}
