package com.alibaba.nacos.plugin.datasource.impl.common;

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.GroupCapacityMapper;


public abstract class AbstractGroupCapacityMapperCommon extends AbstractMapper implements GroupCapacityMapper {


    public String insertIntoSelect() {
        return "INSERT INTO group_capacity (group_id, quota, `usage`, `max_size`, max_aggr_count, max_aggr_size,gmt_create,"
                + " gmt_modified) SELECT ?, ?, count(*), ?, ?, ?, ?, ? FROM config_info";
    }


    public String insertIntoSelectByWhere() {
        return "INSERT INTO group_capacity (group_id, quota,`usage`, `max_size`, max_aggr_count, max_aggr_size, gmt_create,"
                + " gmt_modified) SELECT ?, ?, count(*), ?, ?, ?, ?, ? FROM config_info WHERE group_id=? AND tenant_id = ''";
    }


    public String incrementUsageByWhereQuotaEqualZero() {
        return "UPDATE group_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE group_id = ? AND `usage` < ? AND quota = 0";
    }


    public String incrementUsageByWhereQuotaNotEqualZero() {
        return "UPDATE group_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE group_id = ? AND `usage` < quota AND quota != 0";
    }


    public String incrementUsageByWhere() {
        return "UPDATE group_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE group_id = ?";
    }


    public String decrementUsageByWhere() {
        return "UPDATE group_capacity SET `usage` = `usage` - 1, gmt_modified = ? WHERE group_id = ? AND `usage` > 0";
    }


    public String updateUsage() {
        return "UPDATE group_capacity SET `usage` = (SELECT count(*) FROM config_info), gmt_modified = ? WHERE group_id = ?";
    }


    public String updateUsageByWhere() {
        return "UPDATE group_capacity SET `usage` = (SELECT count(*) FROM config_info WHERE group_id=? AND tenant_id = ''),"
                + " gmt_modified = ? WHERE group_id= ?";
    }


    public String selectGroupInfoBySize() {
        return "SELECT id, group_id FROM group_capacity WHERE id > ? LIMIT ?";
    }


    public String getTableName() {
        return TableConstant.GROUP_CAPACITY;
    }


    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
