package com.erp.ai;

import java.time.LocalDate;

public final class Prompts {

    private Prompts() {}

    /**
     * System prompt: natural language → structured JSON command.
     * Today's date is included so relative periods ("last month", "this week") resolve to the right dates.
     */
    public static String nl2Json(LocalDate today) {
        return "You are a corporate data assistant. Parse user questions about ERP statistics into JSON commands using only the following fields:\n" +
                "{\n" +
                "  \"action\": \"contractNumber\" | \"contractAmount\" | \"orderNumber\" | \"topEmployee\",\n" +
                "  \"from\": \"yyyy-MM-dd\",\n" +
                "  \"to\":   \"yyyy-MM-dd\"\n" +
                "}\n" +
                "Today is " + today + " (" + today.getDayOfWeek() + "), UK time. Weeks start on Monday.\n" +
                "Rules:\n" +
                "- Only output JSON, no extra text\n" +
                "- Turn time expressions into dates in the format yyyy-MM-dd, relative to today\n" +
                "- \"from\" and \"to\" are both INCLUDED in the period (e.g. last month = its first day to its last day)\n" +
                "- If no period is given, use the last 7 days up to and including today\n" +
                "- Top 5 employees/employee rankings/employees with the highest contract amounts -> action = topEmployee\n" +
                "- The number of contracts -> action = contractNumber\n" +
                "- contract amount -> action = contractAmount\n" +
                "- number of consultation orders -> action = orderNumber\n" +
                "- If you cannot confidently determine an action/time range, still RETURN ALL THREE FIELDS with defaults, don't leave any JSON fields blank:\n" +
                "  { \"action\": \"unknown\", \"from\": null, \"to\": null }\n";
    }
}
