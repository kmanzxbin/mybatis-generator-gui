package com.service.ltms;

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

public class LtmsTypeConverter implements JavaTypeConverter {
    private static final Logger LOG = LoggerFactory.getLogger(LtmsTypeConverter.class);
    List<String> tableNameWithoutBlob = Arrays.asList(new String[]{"lt_retailer", "lt_distributor", "lt_sub_retailer", "lt_sub_distributor"});
    List<String> blobColumn = Arrays.asList(new String[]{"profile_photo", "store_photo1", "store_photo1_thumb", "store_photo2", "store_photo2_thumb", "retailer_photo", "retailer_photo_thumb"});
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

        LOG.info("autoConvertJavaType");

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
                        columnOverride.setJavaType("String");
                        columnOverride.setJdbcType("NVARCHAR");
                        columnOverrides.add(columnOverride);
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    } else if (jdbcType.equals("number")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        // 这个为ltms定制化修改 所有涉及到统计的都使用Long
                        String className = tableNameLowerCase.replace("_", "");
                        if (columnName.endsWith("_id") || columnName.equals("id")) {
                            columnOverride.setJavaType("Long");
                        } else if (columnName.endsWith("timestamp")) {
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
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());
                    } else if (item.getJdbcType().toLowerCase().equals("float")) {
                        ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
                        columnOverride.setJavaType("Double");
                        columnOverrides.add(columnOverride);
                        LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());
//                            } else if (item.getJdbcType().toLowerCase().equals("blob")) {
//                                ColumnOverride columnOverride = new ColumnOverride(item.getColumnName());
//                                columnOverride.setJavaType("byte[]");
//                                columnOverrides.add(columnOverride);
//                                LOG.info("convert {} {} java Type to {}", columnOverride.getColumnName(), item.getJdbcType(), columnOverride.getJavaType());

                    }
                }
            });

            this.ignoredColumns = ignoredColumns;
            this.columnOverrides = columnOverrides;

        }


    }
}
