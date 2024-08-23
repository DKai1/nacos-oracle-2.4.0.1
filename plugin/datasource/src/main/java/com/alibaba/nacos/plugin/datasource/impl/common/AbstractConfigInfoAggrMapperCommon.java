package com.alibaba.nacos.plugin.datasource.impl.common;

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoAggrMapper;


public abstract class AbstractConfigInfoAggrMapperCommon extends AbstractMapper implements ConfigInfoAggrMapper {


    public String batchRemoveAggr(int datumSize) {
        final StringBuilder placeholderString = new StringBuilder();
        for (int i = 0; i < datumSize; i++) {
            if (i != 0) {
                placeholderString.append(", ");
            }
            placeholderString.append('?');
        }
        return "DELETE FROM config_info_aggr WHERE data_id = ? AND group_id = ? AND tenant_id = ? AND datum_id IN ("
                + placeholderString + ")";
    }

    public String aggrConfigInfoCount(int size, boolean isIn) {
        StringBuilder sql = new StringBuilder(
                "SELECT count(*) FROM config_info_aggr WHERE data_id = ? AND group_id = ? AND tenant_id = ? AND datum_id");
        if (isIn) {
            sql.append(" IN (");
        } else {
            sql.append(" NOT IN (");
        }
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append('?');
        }
        sql.append(')');

        return sql.toString();
    }

    public String findConfigInfoAggrIsOrdered() {
        return "SELECT data_id,group_id,tenant_id,datum_id,app_name,content FROM "
                + "config_info_aggr WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY datum_id";
    }

    public String findConfigInfoAggrByPageFetchRows(int startRow, int pageSize) {
        return "SELECT data_id,group_id,tenant_id,datum_id,app_name,content FROM config_info_aggr WHERE data_id= ? AND "
                + "group_id= ? AND tenant_id= ? ORDER BY datum_id LIMIT " + pageSize + " OFFSET " + startRow;
    }

    public String findAllAggrGroupByDistinct() {
        return "SELECT DISTINCT data_id, group_id, tenant_id FROM config_info_aggr";
    }

    public String getTableName() {
        return TableConstant.CONFIG_INFO_AGGR;
    }

    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }

}