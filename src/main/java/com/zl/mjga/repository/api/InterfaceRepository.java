package com.zl.mjga.repository.api;

import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author roc
 * @since 2025/12/25 19:59
 */
@Repository
public class InterfaceRepository extends ApiInterfaceDao {
    @Autowired
    public InterfaceRepository(Configuration configuration) {
        super(configuration);
    }
}