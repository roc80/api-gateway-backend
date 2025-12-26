package com.zl.mjga.repository.api_interface;

import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author roc
 * @since 2025/12/25 19:59
 */
public class InterfaceRepository extends ApiInterfaceDao {
    @Autowired
    public InterfaceRepository(Configuration configuration) {
        super(configuration);
    }
}