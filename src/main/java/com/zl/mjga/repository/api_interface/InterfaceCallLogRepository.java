package com.zl.mjga.repository.api_interface;

import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceCallLogDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author roc
 * @since 2025/12/25 20:09
 */
public class InterfaceCallLogRepository extends ApiInterfaceCallLogDao {
    @Autowired
    public InterfaceCallLogRepository(Configuration configuration) {
        super(configuration);
    }
}
