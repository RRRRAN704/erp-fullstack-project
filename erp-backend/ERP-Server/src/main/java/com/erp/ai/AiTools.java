package com.erp.ai;

import com.erp.context.BaseContext;
import com.erp.dto.ClientPageQueryDTO;
import com.erp.dto.ContractPageQueryDTO;
import com.erp.entity.Client;
import com.erp.entity.Contract;
import com.erp.entity.Employee;
import com.erp.entity.Order;
import com.erp.enumeration.Role;
import com.erp.exception.BaseException;
import com.erp.mapper.ClientMapper;
import com.erp.mapper.ContractMapper;
import com.erp.mapper.EmployeeMapper;
import com.erp.mapper.OrderMapper;
import com.erp.service.ReportService;
import com.erp.vo.ContractAmountVO;
import com.erp.vo.NumberOfContractVO;
import com.erp.vo.NumberOfOrderVO;
import com.erp.vo.TopEmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The tools the AI assistant may call. Each one is a read-only Java method: the model only chooses which
 * tool to call with which arguments, it never sees the database or SQL.
 * Client, contract and order tools apply the same visibility as the list pages
 * (consultant: own records, manager: own department), using the logged-in user from BaseContext.
 */
@Component
@RequiredArgsConstructor
public class AiTools {

    /** Shown records per search: enough to answer, small enough to keep the prompt cheap. */
    private static final int MAX_ROWS = 20;

    private final ReportService reportService;
    private final ClientMapper clientMapper;
    private final ContractMapper contractMapper;
    private final OrderMapper orderMapper;
    private final EmployeeMapper employeeMapper;

    private final ObjectMapper om = new ObjectMapper();

    private record Tool(String description, ObjectNode parameters, Function<JsonNode, Object> run) {}

    private Map<String, Tool> tools;

    private Map<String, Tool> tools() {
        if (tools == null) {
            Map<String, Tool> t = new LinkedHashMap<>();
            t.put("contract_stats", new Tool(
                    "Number of contracts signed and their total value (GBP) between two dates, for the whole company.",
                    dateRangeParams(), this::contractStats));
            t.put("order_stats", new Tool(
                    "Number of consultation orders (website leads) received between two dates, for the whole company.",
                    dateRangeParams(), this::orderStats));
            t.put("top_employees", new Tool(
                    "Top 5 employees by total contract value between two dates.",
                    dateRangeParams(), this::topEmployees));
            t.put("search_clients", new Tool(
                    "Find clients the current user can see, optionally by company name. Returns the total and up to "
                            + MAX_ROWS + " clients with contact details and owner.",
                    searchParams("companyName", "Part of the company name; omit to list all"), this::searchClients));
            t.put("search_contracts", new Tool(
                    "Find contracts the current user can see, optionally by contract name. Returns the total count, "
                            + "total value and up to " + MAX_ROWS + " contracts with client, owner, value and dates.",
                    searchParams("contractName", "Part of the contract name; omit to list all"), this::searchContracts));
            t.put("unassigned_orders", new Tool(
                    "Website consultation orders not yet assigned to anyone (managers only).",
                    objectSchema(), args -> unassignedOrders()));
            tools = t;
        }
        return tools;
    }

    /** Tool definitions in the Chat Completions "tools" format. */
    public ArrayNode definitions() {
        ArrayNode defs = om.createArrayNode();
        tools().forEach((name, tool) -> {
            ObjectNode fn = defs.addObject().put("type", "function").putObject("function");
            fn.put("name", name);
            fn.put("description", tool.description());
            fn.set("parameters", tool.parameters());
        });
        return defs;
    }

    /**
     * Runs a tool and returns its result as JSON text for the model.
     * Problems (unknown tool, bad arguments, business rule such as a too long date range) come back as
     * {"error": "..."} so the model can explain them instead of the request failing.
     */
    public String call(String name, String argumentsJson) {
        Tool tool = tools().get(name);
        if (tool == null) {
            return error("Unknown tool: " + name);
        }
        try {
            JsonNode args = argumentsJson == null || argumentsJson.isBlank()
                    ? om.createObjectNode() : om.readTree(argumentsJson);
            return om.writeValueAsString(tool.run().apply(args));
        } catch (BaseException e) {
            return error(e.getMessage());
        } catch (IllegalArgumentException | java.time.format.DateTimeParseException e) {
            return error("Invalid arguments: " + e.getMessage());
        } catch (tools.jackson.core.JacksonException e) {
            return error("Arguments are not valid JSON");
        }
    }

    // ---------- tools ----------

    private Object contractStats(JsonNode args) {
        LocalDate from = date(args, "from"), to = date(args, "to");
        NumberOfContractVO count = reportService.getContractNumberStatistics(from, to);
        ContractAmountVO amount = reportService.getContractAmountStatistics(from, to);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", from.toString());
        out.put("to", to.toString());
        out.put("contractCount", sum(count.getNumberOfContractList()).intValue());
        out.put("totalValueGbp", sum(amount.getContractAmountList()));
        return out;
    }

    private Object orderStats(JsonNode args) {
        LocalDate from = date(args, "from"), to = date(args, "to");
        NumberOfOrderVO vo = reportService.getOrderNumber(from, to);
        return Map.of("from", from.toString(), "to", to.toString(),
                "orderCount", sum(vo.getOrderNumberList()).intValue());
    }

    private Object topEmployees(JsonNode args) {
        LocalDate from = date(args, "from"), to = date(args, "to");
        TopEmployeeVO vo = reportService.getTopEmployee(from, to);
        String[] names = split(vo.getNameList());
        String[] amounts = split(vo.getAmountList());
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (int i = 0; i < names.length && i < amounts.length; i++) {
            ranking.add(Map.of("rank", i + 1, "name", names[i], "contractValueGbp", new BigDecimal(amounts[i])));
        }
        return Map.of("from", from.toString(), "to", to.toString(), "topEmployees", ranking);
    }

    private Object searchClients(JsonNode args) {
        ClientPageQueryDTO query = new ClientPageQueryDTO();
        query.setCompanyName(text(args, "companyName"));
        boolean archived = args.path("archived").asBoolean(false);

        List<Client> found;
        if (isManager()) {
            Long dept = BaseContext.getCurrentDepartment();
            found = archived ? clientMapper.managerArchivedPageQuery(query, dept) : clientMapper.managerPageQuery(query, dept);
        } else {
            Long me = BaseContext.getCurrentId();
            found = archived ? clientMapper.consultantArchivedPageQuery(query, me) : clientMapper.consultantPageQuery(query, me);
        }

        Map<Long, String> names = employeeNames();
        List<Map<String, Object>> rows = found.stream().limit(MAX_ROWS).map(c -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("companyName", c.getCompanyName());
            row.put("contactPerson", c.getContactPerson());
            row.put("contactEmail", c.getContactEmail());
            row.put("contactPhone", c.getContactPhone());
            row.put("owner", names.getOrDefault(c.getEmployeeId(), "-"));
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("archived", archived);
        out.put("total", found.size());
        out.put("clients", rows);
        return out;
    }

    private Object searchContracts(JsonNode args) {
        ContractPageQueryDTO query = new ContractPageQueryDTO();
        query.setContractName(text(args, "contractName"));
        boolean archived = args.path("archived").asBoolean(false);

        List<Contract> found;
        if (isManager()) {
            Long dept = BaseContext.getCurrentDepartment();
            found = archived ? contractMapper.managerArchivedPageQuery(query, dept) : contractMapper.managerPageQuery(query, dept);
        } else {
            Long me = BaseContext.getCurrentId();
            found = archived ? contractMapper.consultantArchivedPageQuery(query, me) : contractMapper.consultantPageQuery(query, me);
        }

        Map<Long, String> employees = employeeNames();
        Map<Long, String> clients = clientMapper.getAllClient().stream()
                .collect(Collectors.toMap(Client::getId, Client::getCompanyName, (a, b) -> a));
        List<Map<String, Object>> rows = found.stream().limit(MAX_ROWS).map(c -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("contractName", c.getContractName());
            row.put("contractNumber", c.getContractNumber());
            row.put("client", clients.getOrDefault(c.getClientId(), "-"));
            row.put("owner", employees.getOrDefault(c.getEmployeeId(), "-"));
            row.put("valueGbp", c.getAmount());
            row.put("startDate", String.valueOf(c.getStartDate()));
            row.put("endDate", String.valueOf(c.getEndDate()));
            return row;
        }).collect(Collectors.toList());

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("archived", archived);
        out.put("total", found.size());
        out.put("totalValueGbp", found.stream().mapToLong(c -> c.getAmount() == null ? 0 : c.getAmount()).sum());
        out.put("contracts", rows);
        return out;
    }

    private Object unassignedOrders() {
        if (!isManager()) {
            return Map.of("error", "Only managers can see unassigned orders.");
        }
        List<Order> orders = orderMapper.getUnassigned();
        List<Map<String, Object>> rows = orders.stream().limit(MAX_ROWS).map(o -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("companyName", o.getCompanyName());
            row.put("contactPerson", o.getContactPerson());
            row.put("contactEmail", o.getContactEmail());
            row.put("received", String.valueOf(o.getCreateTime()));
            return row;
        }).collect(Collectors.toList());
        return Map.of("total", orders.size(), "orders", rows);
    }

    // ---------- helpers ----------

    private boolean isManager() {
        return Role.MANAGER_POSITION.equals(BaseContext.getCurrentRole());
    }

    private Map<Long, String> employeeNames() {
        return employeeMapper.getAllEmployee().stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName, (a, b) -> a));
    }

    private static LocalDate date(JsonNode args, String field) {
        String value = text(args, field);
        if (value == null) {
            throw new IllegalArgumentException("'" + field + "' (yyyy-MM-dd) is required");
        }
        return LocalDate.parse(value);
    }

    private static String text(JsonNode args, String field) {
        JsonNode v = args.get(field);
        return v == null || v.isNull() || v.asString().isBlank() ? null : v.asString().trim();
    }

    private static String[] split(String csv) {
        return csv == null || csv.isBlank() ? new String[0] : csv.split("\\s*,\\s*");
    }

    private static BigDecimal sum(String csv) {
        BigDecimal total = BigDecimal.ZERO;
        for (String part : split(csv)) {
            total = total.add(new BigDecimal(part));
        }
        return total;
    }

    private String error(String message) {
        return om.createObjectNode().put("error", message).toString();
    }

    // ---------- JSON schemas for the tool parameters ----------

    private ObjectNode objectSchema() {
        ObjectNode schema = om.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        return schema;
    }

    private ObjectNode dateRangeParams() {
        ObjectNode schema = objectSchema();
        ObjectNode props = (ObjectNode) schema.get("properties");
        props.putObject("from").put("type", "string").put("description", "First day, yyyy-MM-dd (included)");
        props.putObject("to").put("type", "string").put("description", "Last day, yyyy-MM-dd (included)");
        schema.putArray("required").add("from").add("to");
        return schema;
    }

    private ObjectNode searchParams(String nameField, String nameDescription) {
        ObjectNode schema = objectSchema();
        ObjectNode props = (ObjectNode) schema.get("properties");
        props.putObject(nameField).put("type", "string").put("description", nameDescription);
        props.putObject("archived").put("type", "boolean")
                .put("description", "true to search archived records instead of active ones");
        return schema;
    }
}
