package com.zzg.mybatis.generator.controller;

import com.zzg.mybatis.generator.model.UITableColumnVO;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;

import java.util.List;

/**
 * Created by benjamin on 2018/5/16.
 */
public interface JavaTypeConverter {

    public void convert(List<UITableColumnVO> tableColumns, String tableName);

    public List<IgnoredColumn> getIgnoredColumns();

    public List<ColumnOverride> getColumnOverrides();

    default void setIgnoreBlobColumn(boolean ignoreBlobColumn){};
}
