import { useUserStore } from "@/stores/user";

// Same rules as the backend AccessGuard, so dropdowns only offer choices the server accepts:
// a consultant works on their own records, a manager on their own department's.

const currentUser = () => useUserStore().userInfo;

export const isManager = () => currentUser().position === "Manager";

// Employees a record can be assigned to
export const assignableEmployees = (employees) => {
  const me = currentUser();
  return isManager()
    ? employees.filter((e) => e.departmentId === me.departmentId)
    : employees.filter((e) => e.id === me.id);
};

// Clients a contract can be linked to (the ones in your client list)
export const visibleClients = (clients) => {
  const me = currentUser();
  return isManager()
    ? clients.filter((c) => c.departmentId === me.departmentId)
    : clients.filter((c) => c.employeeId === me.id);
};

// Default assignee for a new record: a consultant can only pick themselves
export const defaultAssignee = () => (isManager() ? null : currentUser().id);
