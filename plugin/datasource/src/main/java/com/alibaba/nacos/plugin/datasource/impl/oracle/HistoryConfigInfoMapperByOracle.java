package com.alibaba.nacos.plugin.datasource.impl.oracle;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.plugin.datasource.constants.DataSourceConstant;
import com.alibaba.nacos.plugin.datasource.constants.FieldConstant;
import com.alibaba.nacos.plugin.datasource.mapper.HistoryConfigInfoMapper;
import com.alibaba.nacos.plugin.datasource.model.MapperContext;
import com.alibaba.nacos.plugin.datasource.model.MapperResult;

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
public class HistoryConfigInfoMapperByOracle extends AbstractMapperByOracle implements HistoryConfigInfoMapper {

    @Override
    public String getDataSource() {
        return DataSourceConstant.ORACLE;
    }

    @Override
    public MapperResult removeConfigHistory(MapperContext context) {
        String sql = "DELETE FROM his_config_info WHERE gmt_modified < ? ROWNUM <= ?";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.START_TIME),
                context.getWhereParameter(FieldConstant.LIMIT_SIZE)));
    }

    @Override
    public MapperResult pageFindConfigHistoryFetchRows(MapperContext context) {
        final int offset = ((context.getStartRow() - 1) < 0 ? 0 : (context.getStartRow() - 1)) * context.getPageSize();
        final int limit = context.getPageSize();
        String sql =
                "SELECT * FROM (SELECT nid,ROWNUM as rnum,data_id,group_id,tenant_id,app_name,"
                        + "src_ip,src_user,op_type,gmt_create,gmt_modified FROM his_config_info "
                        + "WHERE data_id = ? AND group_id = ?  ORDER BY nid DESC ) where rnum >= "
                        + offset + " and " + limit + " >= rnum ";
        return new MapperResult(sql, CollectionUtils.list(context.getWhereParameter(FieldConstant.DATA_ID),
                context.getWhereParameter(FieldConstant.GROUP_ID)));
    }

    @Override
    public String count(List<String> where) {
        StringBuilder sql = new StringBuilder();
        String method = "SELECT ";
        sql.append(method);
        sql.append("COUNT(*) FROM ");
        sql.append(getTableName());
        sql.append(" ");

        if (null == where || where.size() == 0) {
            return sql.toString();
        }

        appendWhereClause(where, sql);

        return sql.toString();
    }

    private void appendWhereClause(List<String> where, StringBuilder sql) {
        sql.append("WHERE 1=1 ");
        for (int i = 0; i < where.size(); i++) {
            if (!where.get(i).equalsIgnoreCase("tenant_id")) {
                sql.append(" AND ");
                sql.append(where.get(i)).append(" = ").append("?");
            }
        }
    }

}
