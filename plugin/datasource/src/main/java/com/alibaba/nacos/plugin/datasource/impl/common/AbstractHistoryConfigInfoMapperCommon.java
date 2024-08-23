package com.alibaba.nacos.plugin.datasource.impl.common;

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.HistoryConfigInfoMapper;


public abstract class AbstractHistoryConfigInfoMapperCommon extends AbstractMapper implements HistoryConfigInfoMapper {


    public String removeConfigHistory() {
        return "DELETE FROM his_config_info WHERE gmt_modified < ? LIMIT ?";
    }


    public String findConfigHistoryCountByTime() {
        return "SELECT count(*) FROM his_config_info WHERE gmt_modified < ?";
    }


    public String findDeletedConfig() {
        return "SELECT DISTINCT data_id, group_id, tenant_id FROM his_config_info WHERE op_type = 'D' AND gmt_modified >= ? AND gmt_modified <= ?";
    }


    public String findConfigHistoryFetchRows() {
        return "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC";
    }


    public String detailPreviousConfigHistory() {
        return "SELECT nid,data_id,group_id,tenant_id,app_name,content,md5,src_user,src_ip,op_type,gmt_create,gmt_modified "
                + "FROM his_config_info WHERE nid = (SELECT max(nid) FROM his_config_info WHERE id = ?) ";
    }

    public String pageFindConfigHistoryFetchRows(int pageNo, int pageSize) {
        final int offset = (pageNo - 1) * pageSize;
        final int limit = pageSize;
        return  "SELECT nid,data_id,group_id,tenant_id,app_name,src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info "
                + "WHERE data_id = ? AND group_id = ? AND tenant_id = ? ORDER BY nid DESC  LIMIT " + offset + "," + limit;
    }

    public String getTableName() {
        return TableConstant.HIS_CONFIG_INFO;
    }

    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
