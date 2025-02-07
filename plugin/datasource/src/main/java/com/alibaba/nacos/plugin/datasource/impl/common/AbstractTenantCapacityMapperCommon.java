package com.alibaba.nacos.plugin.datasource.impl.common;

import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.TenantCapacityMapper;

public abstract class AbstractTenantCapacityMapperCommon extends AbstractMapper implements TenantCapacityMapper {


    public String incrementUsageWithDefaultQuotaLimit() {
        return "UPDATE tenant_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE tenant_id = ? AND `usage` <"
                + " ? AND quota = 0";
    }


    public String incrementUsageWithQuotaLimit() {
        return "UPDATE tenant_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE tenant_id = ? AND `usage` < "
                + "quota AND quota != 0";
    }


    public String incrementUsage() {
        return "UPDATE tenant_capacity SET `usage` = `usage` + 1, gmt_modified = ? WHERE tenant_id = ?";
    }


    public String decrementUsage() {
        return "UPDATE tenant_capacity SET `usage` = `usage` - 1, gmt_modified = ? WHERE tenant_id = ? AND `usage` > 0";
    }


    public String correctUsage() {
        return "UPDATE tenant_capacity SET `usage` = (SELECT count(*) FROM config_info WHERE tenant_id = ?), "
                + "gmt_modified = ? WHERE tenant_id = ?";
    }


    public String getCapacityList4CorrectUsage() {
        return "SELECT id, tenant_id FROM tenant_capacity WHERE id>? LIMIT ?";
    }


    public String insertTenantCapacity() {
        return "INSERT INTO tenant_capacity (tenant_id, quota, `usage`, `max_size`, max_aggr_count, max_aggr_size, "
                + "gmt_create, gmt_modified) SELECT ?, ?, count(*), ?, ?, ?, ?, ? FROM config_info WHERE tenant_id=?;";
    }


    public String getTableName() {
        return TableConstant.TENANT_CAPACITY;
    }


    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
