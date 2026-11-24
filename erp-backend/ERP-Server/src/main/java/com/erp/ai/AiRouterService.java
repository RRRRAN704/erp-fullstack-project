package com.erp.ai;

import com.erp.constant.MessageConstant;
import com.erp.exception.AiUnavailableException;
import com.erp.exception.InvalidDateRangeException;
import com.erp.service.ReportService;
import com.erp.vo.ContractAmountVO;
import com.erp.vo.NumberOfContractVO;
import com.erp.vo.NumberOfOrderVO;
import com.erp.vo.TopEmployeeVO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRouterService {

    // LLM client to parse natural language into a JSON command
    private final LlmClient llm;
    private final ReportService reportService;
    private final ObjectMapper om = new ObjectMapper();

    // Dates are UK dates, whatever time zone the server runs in
    private static final ZoneId UK = ZoneId.of("Europe/London");

    // Default date range: the last 7 days up to and including today (both ends included)
    private static LocalDate[] defaultRange(LocalDate today) {
        return new LocalDate[]{today.minusDays(6), today};
    }

    public Map<String, Object> ask(String question) {

        LocalDate today = LocalDate.now(UK);

        // 1) Ask the LLM to output a strict JSON command (action/from/to/…)
        String json;
        try {
            json = llm.chatToJson(Prompts.nl2Json(today), question);
        } catch (RuntimeException e) {
            // out of credit, timeout, outage...: a clear message instead of HTTP 500
            log.warn("AI assistant: LLM call failed: {}", e.getMessage());
            throw new AiUnavailableException(MessageConstant.AI_UNAVAILABLE);
        }

        // 2) Parse the JSON safely; if parsing fails, fall back to an empty JSON object
        ObjectNode cmd;
        try {
            cmd = (ObjectNode) om.readTree(json);
        } catch (Exception e) {
            // default Json
            cmd = om.createObjectNode();
        }

        // 3) Autofill missing fields with sensible defaults
        String action = getTextOr(cmd, "action", "unknown");

        LocalDate[] def = defaultRange(today);
        LocalDate from = parseDateOr(cmd, "from", def[0]);
        LocalDate to   = parseDateOr(cmd, "to",   def[1]);

        // Both ends are included; a reversed range falls back to the default
        if (from.isAfter(to)) {
            from = def[0];
            to   = def[1];
        }


        // 4) Prepare a normalized, predictable output payload
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        // Return the parsed results of autofilled data
        ObjectNode normalized = om.createObjectNode();
        normalized.put("action", action);
        normalized.put("from", from.toString());
        normalized.put("to", to.toString());
        out.put("parsed", normalized);  // echo back what we understood


        // 5) Route based on 'action' and call domain services to get real data
        try {
            route(action, from, to, out);
        } catch (InvalidDateRangeException e) {
            // e.g. "since 2020": the reports cover at most 366 days at a time
            out.put("text", "I can only look at up to 366 days at a time. Please ask about a shorter period.");
            out.put("data", new LinkedHashMap<String, Object>());
        }
        return out;
    }

    private void route(String action, LocalDate from, LocalDate to, Map<String, Object> out) {
        switch (action) {
            case "contractNumber": {
                NumberOfContractVO vo = reportService.getContractNumberStatistics(from, to);
                int total = sumCsvInt(vo.getNumberOfContractList());
                out.put("text", String.format("from %s to %s, number of contracts: %d",
                        from, to, total));
                out.put("data", vo);
                break;
            }
            case "contractAmount": {
                ContractAmountVO vo = reportService.getContractAmountStatistics(from, to);
                BigDecimal total = sumCsvBig(vo.getContractAmountList());
                out.put("text", String.format("from %s to %s, contract amounts: GBP %s",
                        from, to, total.toPlainString()));
                out.put("data", vo);
                break;
            }
            case "orderNumber": {
                NumberOfOrderVO vo = reportService.getOrderNumber(from, to);
                int total = sumCsvInt(vo.getOrderNumberList());
                out.put("text", String.format("from %s to %s, number of consultation orders: %d",
                        from, to, total));
                out.put("data", vo);
                break;
            }
            case "topEmployee": {
                TopEmployeeVO vo = reportService.getTopEmployee(from, to);
                String nameStr = vo.getNameList();
                String amountStr = vo.getAmountList();  // "10000,8000,6000"

                String[] names = nameStr != null ? nameStr.split(",") : new String[0];
                String[] amounts = amountStr != null ? amountStr.split(",") : new String[0];

                StringBuilder sb = new StringBuilder("Top Five Employees: ");
                for (int i = 0; i < names.length && i < amounts.length; i++) {
                    sb.append(names[i].trim())
                            .append(" (contract amount: ")
                            .append(amounts[i].trim())
                            .append(")");
                    if (i < names.length - 1 && i < amounts.length - 1) sb.append(", ");
                }
                if (names.length == 0) sb.append("no data");

                out.put("text", sb.toString());
                out.put("data", vo);
                break;
            }
            default: {
                // Unknown intent → friendly guidance + empty data
                out.put("text",
                        "This question is not supported yet. Try: contract number / contract amount / order number / top 5 employees");
                out.put("data", new LinkedHashMap<String, Object>());
                break;
            }
        }
    }

    // ========== Tool Methods: Safe Value Retrieval/Parsing ==========

    // Get a text field safely; if missing or null,  default value
    private static String getTextOr(ObjectNode n, String field, String defVal) {
        return (n != null && n.has(field) && !n.get(field).isNull())
                ? n.get(field).asString()
                : defVal;
    }

    // Parse date string safely; if invalid/missing , default date
    private static LocalDate parseDateOr(ObjectNode n, String field, LocalDate defVal) {
        try {
            String v = getTextOr(n, field, null);
            if (v == null || v.trim().isEmpty()) return defVal;
            return LocalDate.parse(v.trim());
        } catch (Exception e) {
            return defVal;
        }
    }

    // Sum integers from CSV like "1,2,3"
    private static int sumCsvInt(String csv) {
        if (csv == null || csv.trim().isEmpty()) return 0;
        int s = 0;
        String[] parts = csv.split(",");
        for (int i = 0; i < parts.length; i++) {
            String t = parts[i].trim();
            if (!t.isEmpty()) s += Integer.parseInt(t);
        }
        return s;
    }

    // Sum BigDecimal from CSV (for money), avoiding floating-point errors
    private static BigDecimal sumCsvBig(String csv) {
        if (csv == null || csv.trim().isEmpty()) return BigDecimal.ZERO;
        BigDecimal s = BigDecimal.ZERO;
        String[] parts = csv.split(",");
        for (int i = 0; i < parts.length; i++) {
            String t = parts[i].trim();
            if (!t.isEmpty()) s = s.add(new BigDecimal(t));
        }
        return s;
    }
}