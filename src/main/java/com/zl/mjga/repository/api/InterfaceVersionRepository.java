package com.zl.mjga.repository.api;

import org.jooq.Configuration;
import org.jooq.generated.api_gateway.tables.daos.ApiInterfaceVersionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author roc
 * @since 2025/12/25 20:08
 */
@Repository
public class InterfaceVersionRepository extends ApiInterfaceVersionDao {
    @Autowired
    public InterfaceVersionRepository(Configuration configuration) {
        super(configuration);
    }
}
