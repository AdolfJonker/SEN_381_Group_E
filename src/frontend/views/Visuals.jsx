// Shared visual pieces for the CivicConnect views: category icons, status progress and illustrations.
// Decoration only. They carry no data the views do not already show as text.

const CATEGORY_STYLES = {
  "Facility Fault": { tone: "orange", path: "M13 2 4 14h7l-1 8 9-12h-7l1-8z" },
  "Damaged Equipment": {
    tone: "pink",
    path: "M14.7 6.3a4 4 0 0 0-5.4 5.2L3 17.8V21h3.2l6.3-6.3a4 4 0 0 0 5.2-5.4l-2.6 2.6-2.4-.6-.6-2.4 2.6-2.6z",
  },
  "Security Concern": { tone: "purple", path: "M12 3 5 6v5c0 4.4 3 8.3 7 10 4-1.7 7-5.6 7-10V6l-7-3z" },
  "IT Support": { tone: "blue", path: "M3 5h18v11H3zM8 20h8M12 16v4" },
  Maintenance: { tone: "green", path: "M4 4h13v5H4zM17 6.5h3V12h-8v3M10.5 15h3v6h-3z" },
  "Lost Property": { tone: "teal", path: "M10.5 4a6.5 6.5 0 1 0 0 13 6.5 6.5 0 0 0 0-13zM20 20l-4.8-4.8" },
  Other: { tone: "slate", path: "M5 12h.01M12 12h.01M19 12h.01M4 5h16v11H8l-4 4z" },
};

export function categoryTone(category) {
  return (CATEGORY_STYLES[category] ?? CATEGORY_STYLES.Other).tone;
}

export function CategoryIcon({ category, size = "md" }) {
  const style = CATEGORY_STYLES[category] ?? CATEGORY_STYLES.Other;
  return (
    <span className={`cat-icon cat-${style.tone} cat-${size}`} aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d={style.path} />
      </svg>
    </span>
  );
}

const STEPS = ["Open", "Assigned", "InProgress", "Resolved", "Closed"];
const STEP_LABELS = { Open: "Open", Assigned: "Assigned", InProgress: "In progress", Resolved: "Resolved", Closed: "Closed" };

/** Five-step progress bar for the fixed status order (FR009). */
export function StatusSteps({ status, showLabels = false, inline = false }) {
  const current = Math.max(0, STEPS.indexOf(status));
  const Wrap = inline ? "span" : "ol";
  const Item = inline ? "span" : "li";
  return (
    <Wrap
      className={`steps${showLabels ? " steps-labelled" : ""}`}
      role={inline ? "img" : undefined}
      aria-label={`Progress: step ${current + 1} of ${STEPS.length}, ${STEP_LABELS[STEPS[current]]}`}
    >
      {STEPS.map((step, i) => (
        <Item key={step} className={`step${i < current ? " done" : i === current ? " current" : ""}`} aria-hidden="true">
          <span className="step-dot" />
          {showLabels && <span className="step-label">{STEP_LABELS[step]}</span>}
        </Item>
      ))}
    </Wrap>
  );
}

export function Icon({ name }) {
  const paths = {
    plus: "M12 5v14M5 12h14",
    list: "M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01",
    home: "M3 11 12 4l9 7M5 10v10h14V10",
    refresh: "M20 11a8 8 0 0 0-14.9-3M4 4v4h4M4 13a8 8 0 0 0 14.9 3M20 20v-4h-4",
    back: "M15 18l-6-6 6-6",
    logout: "M15 4h4v16h-4M10 8l-4 4 4 4M6 12h10",
    search: "M11 4a7 7 0 1 0 0 14 7 7 0 0 0 0-14zM20 20l-4-4",
    arrow: "M5 12h14M13 6l6 6-6 6",
    clock: "M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18zM12 7v5l3 2",
    user: "M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21a8 8 0 0 1 16 0",
    tag: "M3 12V3h9l9 9-9 9-9-9zM8 8h.01",
    chat: "M4 5h16v11H8l-4 4z",
  };
  return (
    <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d={paths[name]} />
    </svg>
  );
}

export function BrandMark() {
  return (
    <span className="brand-mark" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M12 21s-6-5.3-6-11a6 6 0 0 1 12 0c0 5.7-6 11-6 11z" />
        <path d="m9.5 10 1.8 1.8L14.8 8.3" />
      </svg>
    </span>
  );
}

/** Big pin tile used on the requester's call-to-action card. */
export function PinTile() {
  return (
    <span className="pin-tile" aria-hidden="true">
      <svg viewBox="0 0 48 48">
        <path d="M24 44s-15-13.4-15-26a15 15 0 0 1 30 0c0 12.6-15 26-15 26z" fill="#FF6B2C" />
        <circle cx="24" cy="18" r="8" fill="#fff" />
        <path d="m20 18 3 3 5.5-5.8" fill="none" stroke="#FF6B2C" strokeWidth="2.6" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    </span>
  );
}

/** Friendly city scene for the login screen (original drawing). */
export function CityScene() {
  return (
    <svg className="city-scene" viewBox="0 0 400 220" role="img" aria-label="Illustration of a town with a location pin">
      <defs>
        <linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#DDF0FF" />
          <stop offset="1" stopColor="#FFF4EC" />
        </linearGradient>
      </defs>
      <rect width="400" height="220" fill="url(#sky)" />
      <circle cx="330" cy="48" r="22" fill="#FFD166" />
      <ellipse cx="150" cy="58" rx="30" ry="10" fill="#fff" />
      <ellipse cx="172" cy="50" rx="20" ry="9" fill="#fff" />
      <ellipse cx="250" cy="70" rx="26" ry="8" fill="#fff" opacity="0.9" />
      <path d="M0 170 Q100 130 200 165 T400 155 V220 H0z" fill="#B9E4C9" />
      <g fill="#7FB3E6">
        <rect x="40" y="110" width="44" height="80" rx="6" />
        <rect x="150" y="88" width="52" height="102" rx="6" />
        <rect x="292" y="104" width="46" height="86" rx="6" />
      </g>
      <g fill="#FF9F6E">
        <rect x="92" y="128" width="50" height="62" rx="6" />
        <rect x="214" y="118" width="64" height="72" rx="6" />
        <rect x="346" y="132" width="40" height="58" rx="6" />
      </g>
      <g fill="#fff" opacity="0.85">
        <rect x="50" y="122" width="9" height="9" rx="2" /><rect x="66" y="122" width="9" height="9" rx="2" />
        <rect x="50" y="140" width="9" height="9" rx="2" /><rect x="66" y="140" width="9" height="9" rx="2" />
        <rect x="162" y="100" width="10" height="10" rx="2" /><rect x="180" y="100" width="10" height="10" rx="2" />
        <rect x="162" y="120" width="10" height="10" rx="2" /><rect x="180" y="120" width="10" height="10" rx="2" />
        <rect x="162" y="140" width="10" height="10" rx="2" /><rect x="180" y="140" width="10" height="10" rx="2" />
        <rect x="102" y="140" width="10" height="10" rx="2" /><rect x="122" y="140" width="10" height="10" rx="2" />
        <rect x="226" y="132" width="11" height="11" rx="2" /><rect x="246" y="132" width="11" height="11" rx="2" />
        <rect x="302" y="116" width="9" height="9" rx="2" /><rect x="318" y="116" width="9" height="9" rx="2" />
      </g>
      <rect y="188" width="400" height="32" fill="#8FCFA8" />
      <rect y="198" width="400" height="8" fill="#FFFFFF" opacity="0.6" />
      <g transform="translate(232 18)">
        <ellipse cx="22" cy="92" rx="16" ry="4" fill="#000" opacity="0.12" />
        <path d="M22 88s-20-19-20-40a20 20 0 0 1 40 0c0 21-20 40-20 40z" fill="#FF6B2C" />
        <circle cx="22" cy="47" r="11" fill="#fff" />
        <path d="m16.5 47 3.8 3.8 7-7.5" fill="none" stroke="#FF6B2C" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
      </g>
      <g transform="translate(70 70)">
        <rect width="62" height="30" rx="15" fill="#fff" />
        <path d="M14 30l-4 9 12-9z" fill="#fff" />
        <circle cx="19" cy="15" r="3.5" fill="#FF6B2C" /><circle cx="31" cy="15" r="3.5" fill="#FFD166" /><circle cx="43" cy="15" r="3.5" fill="#4F9CF9" />
      </g>
    </svg>
  );
}
