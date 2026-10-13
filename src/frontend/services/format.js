export const STATUS_OPTIONS = [
  { value: "Open", label: "Open" },
  { value: "Assigned", label: "Assigned" },
  { value: "InProgress", label: "In Progress" },
  { value: "Resolved", label: "Resolved" },
  { value: "Closed", label: "Closed" },
];

export function formatStatus(status) {
  const match = STATUS_OPTIONS.find((option) => option.value === status);
  return match ? match.label : status || "";
}

export function formatWhen(value) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return "";
  }
  return date.toLocaleString(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function nextStatus(status) {
  switch (status) {
    case "Open":
      return "Assigned";
    case "Assigned":
      return "InProgress";
    case "InProgress":
      return "Resolved";
    case "Resolved":
      return "Closed";
    default:
      return null;
  }
}

export function statusActionLabel(status) {
  switch (nextStatus(status)) {
    case "Assigned":
      return "Accept request";
    case "InProgress":
      return "Start work";
    case "Resolved":
      return "Mark resolved";
    case "Closed":
      return "Close request";
    default:
      return "";
  }
}
