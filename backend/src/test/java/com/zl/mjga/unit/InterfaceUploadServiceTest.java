package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zl.mjga.dto.api.InterfaceUploadDto;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.repository.api.InterfaceRepository;
import com.zl.mjga.repository.api.InterfaceVersionRepository;
import com.zl.mjga.service.InterfaceService;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterface;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 用户上传API的校验规则与初始状态测试
 *
 * @author roc
 * @since 2026/10/3
 */
@ExtendWith(MockitoExtension.class)
class InterfaceUploadServiceTest {

    @Mock private InterfaceRepository interfaceRepository;

    @Mock private InterfaceVersionRepository interfaceVersionRepository;

    @InjectMocks private InterfaceService interfaceService;

    private static InterfaceUploadDto uploadDto() {
        return new InterfaceUploadDto(
                "user-echo-api",
                "用户回显接口",
                "示例描述",
                "user",
                "v1",
                "POST",
                "/api/user-echo",
                "{\"q\":\"...\"}",
                "{\"a\":\"...\"}");
    }

    @Test
    void uploadInterface_givenValidDto_shouldCreatePendingInterfaceWithCurrentVersion() {
        when(interfaceRepository.fetchOneByCode("user-echo-api")).thenReturn(null);
        when(interfaceVersionRepository.fetchCurrentByMethodPath("POST", "/api/user-echo"))
                .thenReturn(null);

        interfaceService.uploadInterface(uploadDto(), "uploader");

        ArgumentCaptor<ApiInterface> interfaceCaptor = ArgumentCaptor.forClass(ApiInterface.class);
        verify(interfaceRepository).insert(interfaceCaptor.capture());
        assertThat(interfaceCaptor.getValue().getOwner()).isEqualTo("uploader");
        assertThat(interfaceCaptor.getValue().getEnabled()).isFalse();
        assertThat(interfaceCaptor.getValue().getCode()).isEqualTo("user-echo-api");
        ArgumentCaptor<ApiInterfaceVersion> versionCaptor =
                ArgumentCaptor.forClass(ApiInterfaceVersion.class);
        verify(interfaceVersionRepository).insert(versionCaptor.capture());
        assertThat(versionCaptor.getValue().getIsCurrent()).isTrue();
        assertThat(versionCaptor.getValue().getAllowInvoke()).isTrue();
        assertThat(versionCaptor.getValue().getPath()).isEqualTo("/api/user-echo");
    }

    @Test
    void uploadInterface_givenDuplicateCode_shouldThrow() {
        when(interfaceRepository.fetchOneByCode("user-echo-api")).thenReturn(new ApiInterface());

        assertThatThrownBy(() -> interfaceService.uploadInterface(uploadDto(), "uploader"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void uploadInterface_givenOccupiedMethodPath_shouldThrow() {
        when(interfaceRepository.fetchOneByCode("user-echo-api")).thenReturn(null);
        when(interfaceVersionRepository.fetchCurrentByMethodPath("POST", "/api/user-echo"))
                .thenReturn(new ApiInterfaceVersion());

        assertThatThrownBy(() -> interfaceService.uploadInterface(uploadDto(), "uploader"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void uploadInterface_givenValidDto_shouldReturnPendingInterfaceDto() {
        when(interfaceRepository.fetchOneByCode(any())).thenReturn(null);
        when(interfaceVersionRepository.fetchCurrentByMethodPath(any(), any())).thenReturn(null);

        assertThat(interfaceService.uploadInterface(uploadDto(), "uploader").enabled()).isFalse();
    }
}
