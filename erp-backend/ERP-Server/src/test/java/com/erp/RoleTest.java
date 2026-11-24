package com.erp;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;

import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Role rules per endpoint: every protected endpoint must reject the wrong role.
 * Admin-only: departments, positions, employee management. Staff-only (everyone but admin): clients, contracts,
 * orders. Manager-only: assigning orders. Ids/bodies are dummies: the role check must stop the request first.
 */
class RoleTest extends IntegrationTestBase {

    private static final String NOT_ADMIN = "Current user is not admin";
    private static final String ADMIN_NO_ACCESS = "Admin does not have access to this resource";
    private static final String MANAGER_ONLY = "Only manager users can access this resource";

    /** method, url, body (null = none) */
    static Stream<String[]> adminOnlyEndpoints() {
        return Stream.of(
                new String[]{"POST", "/employee/department", "{}"},
                new String[]{"GET", "/employee/department/page?page=1&pageSize=10", null},
                new String[]{"PUT", "/employee/department", "{}"},
                new String[]{"DELETE", "/employee/department?ids=999999", null},
                new String[]{"POST", "/employee/position", "{}"},
                new String[]{"GET", "/employee/position/page?page=1&pageSize=10", null},
                new String[]{"PUT", "/employee/position", "{}"},
                new String[]{"DELETE", "/employee/position?ids=999999", null},
                new String[]{"POST", "/employee/info", "{}"},
                new String[]{"GET", "/employee/info/page?page=1&pageSize=10", null},
                new String[]{"POST", "/employee/info/status/1?id=999999", null},
                new String[]{"PUT", "/employee/info", "{}"},
                new String[]{"GET", "/employee/info/999999", null});
    }

    static Stream<String[]> staffOnlyEndpoints() {
        return Stream.of(
                new String[]{"POST", "/employee/client", "{}"},
                new String[]{"PUT", "/employee/client", "{}"},
                new String[]{"PUT", "/employee/client/archive", "[]"},
                new String[]{"PUT", "/employee/client/restore", "[]"},
                new String[]{"GET", "/employee/client/page?page=1&pageSize=10", null},
                new String[]{"GET", "/employee/client/page/archived?page=1&pageSize=10", null},
                new String[]{"POST", "/employee/contract", "{}"},
                new String[]{"PUT", "/employee/contract", "{}"},
                new String[]{"PUT", "/employee/contract/archive", "[]"},
                new String[]{"PUT", "/employee/contract/restore", "[]"},
                new String[]{"GET", "/employee/contract/page?page=1&pageSize=10", null},
                new String[]{"GET", "/employee/contract/page/archived?page=1&pageSize=10", null},
                new String[]{"PUT", "/employee/order/archive", "[]"},
                new String[]{"PUT", "/employee/order/restore", "[]"},
                new String[]{"GET", "/employee/order/page?page=1&pageSize=10", null},
                new String[]{"GET", "/employee/order/page/archived?page=1&pageSize=10", null});
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("adminOnlyEndpoints")
    void adminOnlyEndpointsRejectConsultantsAndManagers(String method, String url, String body) throws Exception {
        expectRejected("yrwang", method, url, body, NOT_ADMIN);
        expectRejected("manager", method, url, body, NOT_ADMIN);
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("staffOnlyEndpoints")
    void staffEndpointsRejectAdmin(String method, String url, String body) throws Exception {
        expectRejected("admin", method, url, body, ADMIN_NO_ACCESS);
    }

    @ParameterizedTest(name = "assign as {0}")
    @CsvSource({
            "yrwang, " + MANAGER_ONLY,
            "admin, " + ADMIN_NO_ACCESS})
    void onlyManagersAssignOrders(String user, String message) throws Exception {
        expectRejected(user, "PUT", "/employee/order/assign", "{\"id\":999999,\"employeeId\":2}", message);
    }

    private void expectRejected(String user, String method, String url, String body, String message) throws Exception {
        var request = request(HttpMethod.valueOf(method), url).header("token", loginAs(user));
        if (body != null) {
            request.contentType(APPLICATION_JSON).content(body);
        }
        mockMvc.perform(request)
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value(message));
    }
}
