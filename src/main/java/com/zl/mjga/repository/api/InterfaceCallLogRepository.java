package com.zl.mjga.repository.api;

import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceCallLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author roc
 * @since 2025/12/25 20:09
 */
@Repository
public class InterfaceCallLogRepository extends ApiInterfaceCallLogDao {
    @Autowired
    public InterfaceCallLogRepository(Configuration configuration) {
        super(configuration);
    }
}
