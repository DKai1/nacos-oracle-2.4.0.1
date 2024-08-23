package com.alibaba.nacos.plugin.datasource.impl.oracle;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.ConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The mysql implementation of ConfigInfoAggrMapper.
 *
 * @author hyx
 **/
public class ConfigInfoMapperByOracle extends AbstractMapperByOracle implements ConfigInfoMapper {

    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }

    @Override
    public MapperResult findConfigInfoByAppFetchRows(MapperContext context) {
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        String sql = "SELECT * FROM (SELECT id,data_id,group_id,tenant_id,app_name,content, ROWNUM as rnum FROM config_info"
                + " WHERE tenant_id LIKE ? AND app_name= ?)" + " WHERE  rnum >= "
                + context.getStartRow() + " and " + context.getPageSize() + " >= rnum";

        return new MapperResult(sql, CollectionUtils.list(tenantId,
                appName));
    }

    @Override
    public MapperResult getTenantIdList(MapperContext context) {
        String sql = "SELECT * FROM (SELECT tenant_id, ROWNUM as rnum FROM"
                + " config_info WHERE tenant_id != '' GROUP BY tenant_id)"
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum";
        return new MapperResult(sql, Collections.emptyList());
    }

    @Override
    public MapperResult getGroupIdList(MapperContext context) {
        String sql = "SELECT * FROM (SELECT group_id, ROWNUM as rnum FROM config_info WHERE tenant_id ='' GROUP BY group_id) "
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum";
        return new MapperResult(sql, Collections.emptyList());
    }

    @Override
    public MapperResult findAllConfigKey(MapperContext context) {
        String sql = " SELECT data_id,group_id,app_name  FROM ( "
                + " SELECT * FROM (SELECT id, ROWNUM as rnum FROM config_info WHERE tenant_id LIKE ? ORDER BY id)"
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum"
                + " )" + " g, config_info t WHERE g.id = t.id  ";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID)));
    }

    @Override
    public MapperResult findAllConfigInfoBaseFetchRows(MapperContext context) {
        String sql = "SELECT t.id,data_id,group_id,content,md5"
                + " FROM (SELECT * FROM ( SELECT id, ROWNUM as rnum FROM config_info ORDER BY id)"
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum) "
                + " g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql, Collections.emptyList());
    }

    @Override
    public MapperResult findAllConfigInfoFragment(MapperContext context) {
        String sql = "SELECT * FROM (SELECT id,ROWNUM as rnum,data_id,group_id,tenant_id,app_name,content,md5,gmt_modified,type,encrypted_data_key "
                + "FROM config_info WHERE id > ? ORDER BY id ASC) " + " WHERE  rnum >= "
                + context.getStartRow() + " and " + context.getPageSize() + " >= rnum ";;
        return new MapperResult(sql, CollectionUtils
                .list(context.getWhereParameter(FieldConstant.ID)));
    }

    @Override
    public MapperResult findChangeConfigFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantTmp = StringUtils.isBlank(tenant) ? StringUtils.EMPTY : tenant;
        final Timestamp startTime = (Timestamp) context.getWhereParameter(FieldConstant.START_TIME);
        final Timestamp endTime = (Timestamp) context.getWhereParameter(FieldConstant.END_TIME);
        List<Object> paramList = new ArrayList<>();
        final String sqlFetchRows = "SELECT * FROM (SELECT id,data_id,group_id,tenant_id,app_name,content,type,md5,gmt_modified"
                + ", ROWNUM as rnum FROM config_info WHERE ";
        String where = " 1=1 ";
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ? ";
            paramList.add(group);
        }

        if (!StringUtils.isBlank(tenantTmp)) {
            where += " AND tenant_id = ? ";
            paramList.add(tenantTmp);
        }

        if (!StringUtils.isBlank(appName)) {
            where += " AND app_name = ? ";
            paramList.add(appName);
        }
        if (startTime != null) {
            where += " AND gmt_modified >=? ";
            paramList.add(startTime);
        }
        if (endTime != null) {
            where += " AND gmt_modified <=? ";
            paramList.add(endTime);
        }
        return new MapperResult(
                sqlFetchRows + where + " AND id > "
                        + context.getWhereParameter(FieldConstant.LAST_MAX_ID)
                        + " ORDER BY id ASC)" + " WHERE  rnum >= " + 0 + " and "
                        + context.getPageSize() + " >= rnum", paramList);
    }

    @Override
    public MapperResult listGroupKeyMd5ByPageFetchRows(MapperContext context) {
        String sql = "SELECT t.id,data_id,group_id,tenant_id,app_name,md5,type,gmt_modified,encrypted_data_key FROM "
                + "(SELECT * FROM ( SELECT id, ROWNUM as rnum FROM config_info ORDER BY id) "
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum "
                + " ) g, config_info t WHERE g.id = t.id";
        return new MapperResult(sql, Collections.emptyList());
    }

    @Override
    public MapperResult findConfigInfoBaseLikeFetchRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String sqlFetchRows = "SELECT * FROM (SELECT id,data_id,group_id,tenant_id,content, ROWNUM as rnum FROM config_info WHERE ";
        String where = " 1=1 AND tenant_id='' ";
        List<Object> paramList = new ArrayList<>();
        if (!StringUtils.isBlank(dataId)) {
            where += " AND data_id LIKE ? ";
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where += " AND group_id LIKE ";
            paramList.add(group);
        }
        if (!StringUtils.isBlank(content)) {
            where += " AND content LIKE ? ";
            paramList.add(content);
        }
        return new MapperResult(sqlFetchRows + where + ") "
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum ",
                paramList);
    }

    @Override
    public MapperResult findConfigInfo4PageFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        List<Object> paramList = new ArrayList<>();
        final String sql = "SELECT * FROM (SELECT id,data_id,group_id,tenant_id,app_name,content,type,encrypted_data_key,"
                + " ROWNUM as rnum FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE ");

        if (StringUtils.isNotBlank(tenant)) {
            where.append(" tenant_id=? ");
            paramList.add(tenant);
        }
        if (StringUtils.isNotBlank(dataId)) {
            where.append(" AND data_id=? ");
            paramList.add(dataId);
        }
        if (StringUtils.isNotBlank(group)) {
            where.append(" AND group_id=? ");
            paramList.add(group);
        }
        if (StringUtils.isNotBlank(appName)) {
            where.append(" AND app_name=? ");
            paramList.add(appName);
        }
        return new MapperResult(sql + where + ")  " + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum ",
                paramList);
    }

    @Override
    public MapperResult findConfigInfoBaseByGroupFetchRows(MapperContext context) {
        String sql = "SELECT * FROM (SELECT id,data_id,group_id,content, ROWNUM as rnum FROM config_info WHERE group_id=? AND tenant_id=?" + ")  "
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum ";;
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.GROUP_ID),
                context.getWhereParameter(FieldConstant.TENANT_ID)));
    }

    @Override
    public MapperResult findConfigInfoLike4PageFetchRows(MapperContext context) {
        final String tenant = (String) context.getWhereParameter(FieldConstant.TENANT_ID);
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);

        List<Object> paramList = new ArrayList<>();
        final String sqlFetchRows = "SELECT * FROM (SELECT id,data_id,group_id,tenant_id,app_name,content,encrypted_data_key,"
                + " ROWNUM as rnum FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE 1=1");

        if (!StringUtils.isBlank(tenant)) {
            where.append(" AND tenant_id LIKE ? ");
            paramList.add(tenant);
        }
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
            paramList.add(group);
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        return new MapperResult(sqlFetchRows + where + " ) "
                + " WHERE  rnum >= " + context.getStartRow() + " and "
                + context.getPageSize() + " >= rnum ",
                paramList);
    }

    @Override
    public MapperResult findAllConfigInfoFetchRows(MapperContext context) {
        String sql = "SELECT t.id,data_id,group_id,tenant_id,app_name,content,md5 "
                + " FROM (SELECT * FROM (  SELECT id, ROWNUM as rnum FROM config_info WHERE tenant_id LIKE ? ORDER BY id)"
                + " WHERE  rnum >= " + context.getStartRow() + " and " + context.getPageSize() + " >= rnum)"
                + " g, config_info t  WHERE g.id = t.id ";
        return new MapperResult(sql,
                CollectionUtils.list(context.getWhereParameter(FieldConstant.TENANT_ID), context.getStartRow(),
                        context.getPageSize()));
    }


    /**
     * Query config info count. The default sql: SELECT count(*) FROM config_info ...
     *
     * @param context The map of dataId, group, appName, content
     * @return The sql of querying config info count
     */
    @Override
    public MapperResult findConfigInfoLike4PageCountRows(MapperContext context) {
        final String dataId = (String) context.getWhereParameter(FieldConstant.DATA_ID);
        final String group = (String) context.getWhereParameter(FieldConstant.GROUP_ID);
        final String content = (String) context.getWhereParameter(FieldConstant.CONTENT);
        final String appName = (String) context.getWhereParameter(FieldConstant.APP_NAME);
        final String tenantId = (String) context.getWhereParameter(FieldConstant.TENANT_ID);

        final List<Object> paramList = new ArrayList<>();

        final String sqlCountRows = "SELECT count(*) FROM config_info";
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        if (!StringUtils.isBlank(tenantId)) {
            where.append("AND tenant_id LIKE ? ");
            paramList.add(tenantId);
        }
        if (!StringUtils.isBlank(dataId)) {
            where.append(" AND data_id LIKE ? ");
            paramList.add(dataId);
        }
        if (!StringUtils.isBlank(group)) {
            where.append(" AND group_id LIKE ? ");
            paramList.add(group);
        }
        if (!StringUtils.isBlank(appName)) {
            where.append(" AND app_name = ? ");
            paramList.add(appName);
        }
        if (!StringUtils.isBlank(content)) {
            where.append(" AND content LIKE ? ");
            paramList.add(content);
        }
        return new MapperResult(sqlCountRows + where, paramList);
    }

    @Override
    public String select(List<String> columns, List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "SELECT ";
        sql.append(method);
        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i));
            if (i == columns.size() - 1) {
                sql.append(" ");
            } else {
                sql.append(",");
            }
        }
        sql.append("FROM ");
        sql.append(getTableName());
        sql.append(" ");

        if (CollectionUtils.isEmpty(where)) {
            return sql.toString();
        }

        appendWhereClause(where, sql);
        return sql.toString();
    }

    @Override
    public String update(List<String> columns, List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "UPDATE ";
        sql.append(method);
        sql.append(getTableName()).append(" ").append("SET ");

        for (int i = 0; i < columns.size(); i++) {
            sql.append(columns.get(i)).append(" = ").append("?");
            if (i != columns.size() - 1) {
                sql.append(",");
            }
        }

        if (CollectionUtils.isEmpty(where)) {
            return sql.toString();
        }

        sql.append(" ");
        appendWhereClause(where, sql);

        return sql.toString();
    }

    private void appendWhereClause(List<String> where, StringBuilder sql) {
        sql.append(" WHERE 1=1 ");
        for (int i = 0; i < where.size(); i++) {
            // deal oracle tenant_id is null or ""
            if (!where.get(i).equals("tenant_id")) {
                sql.append(" AND ");
                sql.append(where.get(i)).append(" = ").append("? ");
            }
        }
    }

    public MapperResult updateConfigInfoAtomicCas(MapperContext context) {
        List<Object> paramList = new ArrayList<>();

        paramList.add(context.getUpdateParameter(FieldConstant.CONTENT));
        paramList.add(context.getUpdateParameter(FieldConstant.MD5));
        paramList.add(context.getUpdateParameter(FieldConstant.SRC_IP));
        paramList.add(context.getUpdateParameter(FieldConstant.SRC_USER));
        paramList.add(context.getUpdateParameter(FieldConstant.APP_NAME));
        paramList.add(context.getUpdateParameter(FieldConstant.C_DESC));
        paramList.add(context.getUpdateParameter(FieldConstant.C_USE));
        paramList.add(context.getUpdateParameter(FieldConstant.EFFECT));
        paramList.add(context.getUpdateParameter(FieldConstant.TYPE));
        paramList.add(context.getUpdateParameter(FieldConstant.C_SCHEMA));
        paramList.add(context.getUpdateParameter(FieldConstant.ENCRYPTED_DATA_KEY));
        paramList.add(context.getWhereParameter(FieldConstant.DATA_ID));
        paramList.add(context.getWhereParameter(FieldConstant.GROUP_ID));
//        paramList.add(context.getWhereParameter(FieldConstant.TENANT_ID));
        paramList.add(context.getWhereParameter(FieldConstant.MD5));
        String sql = "UPDATE config_info SET " + "content=?, md5=?, src_ip=?, src_user=?, gmt_modified="
                + getFunction("NOW()")
                + ", app_name=?, c_desc=?, c_use=?, effect=?, type=?, c_schema=?, encrypted_data_key=? "
                + "WHERE data_id=? AND group_id=? AND (md5=? OR md5 IS NULL OR md5='')";
        return new MapperResult(sql, paramList);
    }
}
