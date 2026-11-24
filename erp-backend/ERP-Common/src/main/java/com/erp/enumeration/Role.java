package com.erp.enumeration;

/**
 * Who may call an endpoint (see @RequireRole).
 * Roles are position names from the database; admins can create new positions,
 * so STAFF means "anyone except the system admin" rather than a fixed list.
 */
public enum Role {
    /** Only the system admin (company setup: departments, positions, employees). */
    ADMIN,
    /** Everyone except the system admin (business data: clients, contracts, orders). */
    STAFF,
    /** Only managers. */
    MANAGER;

    public static final String ADMIN_POSITION = "System Admin";
    public static final String MANAGER_POSITION = "Manager";
}
