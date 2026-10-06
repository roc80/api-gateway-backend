package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.roc.api.dto.InterfaceCallReport;
import com.zl.mjga.repository.api.InterfaceCallLogRepository;
import com.zl.mjga.service.InterfaceCallReportServiceImpl;
import org.jooq.JSONB;
import org.jooq.generated.api_gateway.tables.pojos.ApiInterfaceCallLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 网关调用埋点上报 RPC 实现测试：字段映射、非法上报拦截与非法 JSON 请求/响应体剔除
 *
 * @author roc
 * @since 2026/10/6
 */
@ExtendWith(MockitoExtension.class)
class InterfaceCallReportServiceImplTest {

    @Mock private InterfaceCallLogRepository interfaceCallLogRepository;

    @InjectMocks private InterfaceCallReportServiceImpl interfaceCallReportService;

    @Test
    void reportCall_givenValidReport_shouldPersistMappedCallLog() {
        interfaceCallReportService.reportCall(
                new InterfaceCallReport(
                        3L, 7L, "dave", "{\"username\":\"dave\"}", "\"dave\"", 200, true, 12));

        ArgumentCaptor<ApiInterfaceCallLog> captor =
                ArgumentCaptor.forClass(ApiInterfaceCallLog.class);
        verify(interfaceCallLogRepository).insert(captor.capture());
        ApiInterfaceCallLog callLog = captor.getValue();
        assertThat(callLog.getApiId()).isEqualTo(3L);
        assertThat(callLog.getVersionId()).isEqualTo(7L);
        assertThat(callLog.getCaller()).isEqualTo("dave");
        assertThat(jsonbText(callLog.getRequestData())).isEqualTo("{\"username\":\"dave\"}");
        assertThat(jsonbText(callLog.getResponseData())).isEqualTo("\"dave\"");
        assertThat(callLog.getStatusCode()).isEqualTo(200);
        assertThat(callLog.getSuccess()).isTrue();
        assertThat(callLog.getDurationMs()).isEqualTo(12);
    }

    @Test
    void reportCall_givenBlankOrInvalidJsonBodies_shouldStoreNullInsteadOfFailing() {
        interfaceCallReportService.reportCall(
                new InterfaceCallReport(
                        3L, 7L, "dave", "", "plain-text-not-json", 500, false, null));

        ArgumentCaptor<ApiInterfaceCallLog> captor =
                ArgumentCaptor.forClass(ApiInterfaceCallLog.class);
        verify(interfaceCallLogRepository).insert(captor.capture());
        // 空白请求体与纯文本响应体都不能写入 JSONB 列，必须剔除而非让埋点落库失败
        assertThat(captor.getValue().getRequestData()).isNull();
        assertThat(captor.getValue().getResponseData()).isNull();
        assertThat(captor.getValue().getDurationMs()).isZero();
    }

    @Test
    void reportCall_givenBlankCaller_shouldFallbackToAnonymous() {
        interfaceCallReportService.reportCall(
                new InterfaceCallReport(3L, 7L, "  ", null, null, 200, true, 5));

        ArgumentCaptor<ApiInterfaceCallLog> captor =
                ArgumentCaptor.forClass(ApiInterfaceCallLog.class);
        verify(interfaceCallLogRepository).insert(captor.capture());
        assertThat(captor.getValue().getCaller()).isEqualTo("anonymous");
    }

    @Test
    void reportCall_givenMissingApiOrVersion_shouldSkipPersist() {
        interfaceCallReportService.reportCall(
                new InterfaceCallReport(null, 7L, "dave", null, null, 200, true, 5));
        interfaceCallReportService.reportCall(null);

        verify(interfaceCallLogRepository, never()).insert(any(ApiInterfaceCallLog.class));
    }

    private static String jsonbText(JSONB jsonb) {
        return jsonb != null && jsonb.data() != null ? jsonb.data().toString() : null;
    }
}
