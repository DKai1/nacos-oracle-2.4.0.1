package com.alibaba.nacos.plugin.datasource.impl.common;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.TableConstant;
import com.alibaba.nacos.plugin.datasource.enums.oracle.TrustedOracleFunctionEnum;
import com.alibaba.nacos.plugin.datasource.mapper.AbstractMapper;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public abstract class AbstractConfigInfoMapperCommon extends AbstractMapper implements ConfigInfoMapper {

    private static final String DATA_ID = "dataId";

    private static final String GROUP = "group";

    private static final String APP_NAME = "appName";

    private static final String CONTENT = "content";

    private static final String TENANT = "tenant";

    public String findConfigMaxId() {
        return "SELECT MAX(id) FROM config_info";
    }

    public String findAllDataIdAndGroup() {
        return "SELECT DISTINCT data_id, group_id FROM config_info";
    }

    public String findConfigInfoByAppCountRows() {
        return "SELECT count(*) FROM config_info WHERE tenant_id LIKE ? AND app_name= ?";
    }

    public String findConfigInfoByAppFetchRows(int startRow, int pageSize) {
        return "SELECT id,data_id,group_id,tenant_id,app_name,content FROM config_info"
                + " WHERE tenant_id LIKE ? AND app_name= ?" + " LIMIT " + pageSize + " OFFSET " + startRow;
    }

    public String configInfoLikeTenantCount() {
        return "SELECT count(*) FROM config_info WHERE tenant_id LIKE ?";
    }

    public String getTenantIdList(int startRow, int pageSize) {
        return "SELECT tenant_id FROM config_info WHERE tenant_id != '' GROUP BY tenant_id LIMIT " + pageSize
                + " OFFSET " + startRow;
    }

    public String getGroupIdList(int startRow, int pageSize) {
        return "SELECT group_id FROM config_info WHERE tenant_id ='' GROUP BY group_id LIMIT " + pageSize + " OFFSET "
                + startRow;
    }

    public String findAllConfigKey(int startRow, int pageSize) {
        return " SELECT data_id,group_id,app_name  FROM ( "
                + " SELECT id FROM config_info WHERE tenant_id LIKE ? ORDER BY id LIMIT " + pageSize + " OFFSET "
                + startRow + " )" + " g, config_info t WHERE g.id = t.id  ";
    }

    public String findAllConfigInfoBaseFetchRows(int startRow, int pageSize) {
        return "SELECT t.id,data_id,group_id,content,md5"
                + " FROM ( SELECT id FROM config_info ORDER BY id LIMIT ?,?  ) "
                + " g, config_info t  WHERE g.id = t.id ";
    }

    public String findAllConfigInfoFragment(int startRow, int pageSize) {
        return "SELECT id,data_id,group_id,tenant_id,app_name,content,md5,gmt_modified,type,encrypted_data_key "
                + "FROM config_info WHERE id > ? ORDER BY id ASC LIMIT " + pageSize + " OFFSET " + startRow;
    }

    public String findChangeConfig() {
        return "SELECT data_id, group_id, tenant_id, app_name, content, gmt_modified,encrypted_data_key "
                + "FROM config_info WHERE gmt_modified >= ? AND gmt_modified <= ?";
    }

    public String findChangeConfigCountRows(Map<String, String> params, final Timestamp startTime,
                                            final Timestamp endTime) {
        final String tenant = params.get(TENANT);
        final String dataId = params.get(DATA_ID);
        final String group = params.get(GROUP);
        final String appName = params.get(APP_NAME);
        final String tenantTmp = StringUtils.isBlank(tenant) ? StringUtils.EMPTY : tenant;
        final String sqlCountRows = "SELECT count(*) FROM config_info WHERE ";
        String where = " 1=1 ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
        }

        if (!StringUtils.isBlank(tenantTmp)) {
            where += " AND tenant_id = ? ";
        }

        if (!StringUtils.isBlank(appName)) {
            where += " AND app_name = ? ";
        }
        if (startTime != null) {
            where += " AND gmt_modified >=? ";
        }
        if (endTime != null) {
            where += " AND gmt_modified <=? ";
        }
        return sqlCountRows + where;
    }


    public String findChangeConfigFetchRows(Map<String, String> params, final Timestamp startTime,
                                            final Timestamp endTime, int startRow, int pageSize, long lastMaxId) {
        final String tenant = params.get(TENANT);
        final String dataId = params.get(DATA_ID);
        final String group = params.get(GROUP);
        final String appName = params.get(APP_NAME);
        final String tenantTmp = StringUtils.isBlank(tenant) ? StringUtils.EMPTY : tenant;
        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_modified FROM config_info WHERE ";
        String where = " 1=1 ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
        }

        if (!StringUtils.isBlank(tenantTmp)) {
            where += " AND tenant_id = ? ";
        }

        if (!StringUtils.isBlank(appName)) {
            where += " AND app_name = ? ";
        }
        if (startTime != null) {
            where += " AND gmt_modified >=? ";
        }
        if (endTime != null) {
            where += " AND gmt_modified <=? ";
        }
        return sqlFetchRows + where + " AND id > " + lastMaxId + " ORDER BY id ASC" + " LIMIT " + 0 + " OFFSET "
                + pageSize;
    }


    public String listGroupKeyMd5ByPageFetchRows(int startRow, int pageSize) {
        return "SELECT t.id,data_id,group_id,tenant_id,app_name,md5,type,gmt_modified,encrypted_data_key FROM "
                + "( SELECT id FROM config_info ORDER BY id LIMIT " + pageSize + " OFFSET " + startRow
                + " ) g, config_info t WHERE g.id = t.id";
    }


    public String findAllConfigInfo4Export(List<Long> ids, Map<String, String> params) {
        String tenant = params.get("tenant");
        String tenantTmp = StringUtils.isBlank(tenant) ? StringUtils.EMPTY : tenant;
        String sql = "SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_create,gmt_modified,src_user,src_ip,"
                + "c_desc,c_use,effect,c_schema,encrypted_data_key FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        List<Object> paramList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(ids)) {
            where.append(" id IN (");
            for (int i = 0; i < ids.size(); i++) {
                if (i != 0) {
                    where.append(", ");
                }
                where.append('?');
                paramList.add(ids.get(i));
            }
            where.append(") ");
        } else {
            where.append(" tenant_id= ? ");
            paramList.add(tenantTmp);
            if (!StringUtils.isBlank(params.get(DATA_ID))) {
                where.append(" AND data_id LIKE ? ");
            }
            if (StringUtils.isNotBlank(params.get(GROUP))) {
                where.append(" AND group_id= ? ");
            }
            if (StringUtils.isNotBlank(params.get(APP_NAME))) {
                where.append(" AND app_name= ? ");
            }
        }
        return sql + where;
    }


    public String findConfigInfoBaseLikeCountRows(Map<String, String> params) {
        final String sqlCountRows = "SELECT count(*) FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='' ";

        if (!StringUtils.isBlank(params.get(DATA_ID))) {
            where += " AND data_id LIKE ? ";
        }
        if (!StringUtils.isBlank(params.get(GROUP))) {
            where += " AND group_id LIKE ";
        }
        if (!StringUtils.isBlank(params.get(CONTENT))) {
            where += " AND content LIKE ? ";
        }
        return sqlCountRows + where;
    }


    public String findConfigInfoBaseLikeFetchRows(Map<String, String> params, int startRow, int pageSize) {
        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,content FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='' ";
        if (!StringUtils.isBlank(params.get(DATA_ID))) {
            where += " AND data_id LIKE ? ";
        }
        if (!StringUtils.isBlank(params.get(GROUP))) {
            where += " AND group_id LIKE ";
        }
        if (!StringUtils.isBlank(params.get(CONTENT))) {
            where += " AND content LIKE ? ";
        }
        return sqlFetchRows + where + " LIMIT " + startRow + "," + pageSize;
    }


    public String findConfigInfo4PageCountRows(Map<String, String> params) {
        final String appName = params.get(APP_NAME);
        final String dataId = params.get(DATA_ID);
        final String group = params.get(GROUP);
        final String sqlCount = "SELECT count(*) FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id=? ");
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
        }
        return sqlCount + where;
    }


    public String findConfigInfo4PageFetchRows(Map<String, String> params, int startRow, int pageSize) {
        final String appName = params.get(APP_NAME);
        final String dataId = params.get(DATA_ID);
        final String group = params.get(GROUP);
        final String sql = "SELECT id,data_id,group_id,tenant_id,app_name,content,type,encrypted_data_key FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id=? ");
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
        }
        return sql + where + " LIMIT " + pageSize + " OFFSET " + startRow;
    }


    public String findConfigInfoBaseByGroupFetchRows(int startRow, int pageSize) {
        return "SELECT id,data_id,group_id,content FROM config_info WHERE group_id=? AND tenant_id=?" + " LIMIT "
                + pageSize + " OFFSET " + startRow;
    }


    public String findConfigInfoLike4PageCountRows(Map<String, String> params) {
        String dataId = params.get(DATA_ID);
        String group = params.get(GROUP);
        final String appName = params.get(APP_NAME);
        final String content = params.get(CONTENT);
        final String sqlCountRows = "SELECT count(*) FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id LIKE ? ");
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
        }
        return sqlCountRows + where;
    }


    public String findConfigInfoLike4PageFetchRows(Map<String, String> params, int startRow, int pageSize) {
        String dataId = params.get(DATA_ID);
        String group = params.get(GROUP);
        final String appName = params.get(APP_NAME);
        final String content = params.get(CONTENT);
        final String sqlFetchRows = "SELECT id,data_id,group_id,tenant_id,app_name,content,encrypted_data_key FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");
        where.append(" tenant_id LIKE ? ");
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
        }
        return sqlFetchRows + where + " LIMIT " + pageSize + " OFFSET " + startRow;
    }


    public String findAllConfigInfoFetchRows(int startRow, int pageSize) {
        return "SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5 "
                + " FROM (  SELECT id FROM config_info WHERE tenant_id LIKE ? ORDER BY id LIMIT " + pageSize
                + " OFFSET " + startRow + " )" + " g, config_info t  WHERE g.id = t.id ";
    }


    public String findConfigInfosByIds(int idSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT ID,data_id,group_id,tenant_id,app_name,content,md5 FROM config_info WHERE ");
        sql.append("id IN (");
        for (int i = 0; i < idSize; i++) {
            if (i != 0) {
                sql.append(", ");
            }
            sql.append('?');
        }
        sql.append(") ");
        return sql.toString();
    }


    public String removeConfigInfoByIdsAtomic(int size) {
        StringBuilder sql = new StringBuilder("DELETE FROM config_info WHERE ");
        sql.append("id IN (");
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                sql.append(", ");
            }
            sql.append('?');
        }
        sql.append(") ");
        return sql.toString();
    }


    public String updateConfigInfoAtomicCas() {
        return "UPDATE config_info SET "
                + "content=?, md5 = ?, src_ip=?,src_user=?,gmt_modified=?, app_name=?,c_desc=?,c_use=?,effect=?,type=?,c_schema=? "
                + "WHERE data_id=? AND group_id=? AND tenant_id=? AND (md5=? OR md5 IS NULL OR md5='')";
    }


    public String getTableName() {
        return TableConstant.CONFIG_INFO;
    }


    @Override
    public String getFunction(String functionName) {
        return TrustedOracleFunctionEnum.getFunctionByName(functionName);
    }
}
