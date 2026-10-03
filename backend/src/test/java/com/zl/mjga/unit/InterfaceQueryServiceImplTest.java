package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.roc.api.dto.InvokeTargetInfo;
import com.zl.mjga.repository.api.InterfaceVersionRepository;
import com.zl.mjga.service.InterfaceQueryServiceImpl;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author roc
 * @since 2026/10/3
 */
@ExtendWith(MockitoExtension.class)
class InterfaceQueryServiceImplTest {

    @Mock private InterfaceVersionRepository interfaceVersionRepository;

    @InjectMocks private InterfaceQueryServiceImpl interfaceQueryService;

    @Test
    void getInvokeTarget_givenRegisteredVersion_shouldReturnApiAndVersionId() {
        ApiInterfaceVersion version = new ApiInterfaceVersion();
        version.setId(7L);
        version.setApiId(3L);
        when(interfaceVersionRepository.fetchInvokeTarget("POST", "/api/name")).thenReturn(version);

        InvokeTargetInfo target = interfaceQueryService.getInvokeTarget("POST", "/api/name");

        assertThat(target)
                .extracting(InvokeTargetInfo::getApiId, InvokeTargetInfo::getVersionId)
                .containsExactly(3L, 7L);
    }

    @Test
    void getInvokeTarget_givenUnknownTarget_shouldReturnNull() {
        when(interfaceVersionRepository.fetchInvokeTarget("GET", "/api/none")).thenReturn(null);

        assertThat(interfaceQueryService.getInvokeTarget("GET", "/api/none")).isNull();
    }

    @Test
    void getInvokeTarget_givenBlankInput_shouldReturnNullWithoutQuery() {
        assertThat(interfaceQueryService.getInvokeTarget(" ", "/api/name")).isNull();
    }
}
