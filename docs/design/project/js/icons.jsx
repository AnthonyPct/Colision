// Colision shared UI primitives — Compose-Multiplatform-flavored components rendered in React.
// Same UI on iOS + Android. Frame chrome is platform-specific; this is the shared app.

const C = window.COLISION_TOKENS;
const D = window.COLISION_DATA;

// ── Atomic building blocks ─────────────────────────────────────────────────
const colStyle = (gap = 0) => ({ display: 'flex', flexDirection: 'column', gap });
const rowStyle = (gap = 0) => ({ display: 'flex', flexDirection: 'row', gap, alignItems: 'center' });

function ColIcon({ name, size = 20, color = 'currentColor', strokeWidth = 1.75 }) {
  const props = { width: size, height: size, viewBox: '0 0 24 24', fill: 'none',
    stroke: color, strokeWidth, strokeLinecap: 'round', strokeLinejoin: 'round' };
  switch (name) {
    case 'plus':       return <svg {...props}><path d="M12 5v14M5 12h14"/></svg>;
    case 'check':      return <svg {...props}><path d="M5 12.5l4.5 4.5L19 7.5"/></svg>;
    case 'chevron-right': return <svg {...props}><path d="M9 6l6 6-6 6"/></svg>;
    case 'chevron-left':  return <svg {...props}><path d="M15 6l-6 6 6 6"/></svg>;
    case 'chevron-down':  return <svg {...props}><path d="M6 9l6 6 6-6"/></svg>;
    case 'arrow-left': return <svg {...props}><path d="M19 12H5M12 5l-7 7 7 7"/></svg>;
    case 'arrow-right': return <svg {...props}><path d="M5 12h14M12 5l7 7-7 7"/></svg>;
    case 'calendar':   return <svg {...props}><rect x="3.5" y="5" width="17" height="15.5" rx="2"/><path d="M8 3v4M16 3v4M3.5 10h17"/></svg>;
    case 'users':      return <svg {...props}><path d="M16 20v-1.5A3.5 3.5 0 0012.5 15h-5A3.5 3.5 0 004 18.5V20"/><circle cx="10" cy="8.5" r="3.5"/><path d="M20 20v-1.2c0-1.5-1-2.8-2.5-3.2M15.5 5.3a3.5 3.5 0 010 6.5"/></svg>;
    case 'more':       return <svg {...props}><circle cx="5" cy="12" r="1.2"/><circle cx="12" cy="12" r="1.2"/><circle cx="19" cy="12" r="1.2"/></svg>;
    case 'bell':       return <svg {...props}><path d="M6 8a6 6 0 1112 0c0 5 2 6 2 6H4s2-1 2-6z"/><path d="M10 19a2 2 0 004 0"/></svg>;
    case 'warn':       return <svg {...props}><path d="M12 4l9.5 16.5h-19L12 4z"/><path d="M12 10v5M12 18v.5"/></svg>;
    case 'clock':      return <svg {...props}><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/></svg>;
    case 'share':      return <svg {...props}><path d="M12 4v12M12 4l-4 4M12 4l4 4M5 14v4a2 2 0 002 2h10a2 2 0 002-2v-4"/></svg>;
    case 'copy':       return <svg {...props}><rect x="8" y="8" width="12" height="12" rx="2"/><path d="M16 8V6a2 2 0 00-2-2H6a2 2 0 00-2 2v8a2 2 0 002 2h2"/></svg>;
    case 'settings':   return <svg {...props}><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 00.4 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.7 1.7 0 00-1.8-.4 1.7 1.7 0 00-1 1.5V21a2 2 0 11-4 0v-.1a1.7 1.7 0 00-1.1-1.5 1.7 1.7 0 00-1.8.4l-.1.1a2 2 0 11-2.8-2.8l.1-.1a1.7 1.7 0 00.4-1.8 1.7 1.7 0 00-1.5-1H3a2 2 0 110-4h.1a1.7 1.7 0 001.5-1.1 1.7 1.7 0 00-.4-1.8l-.1-.1a2 2 0 112.8-2.8l.1.1a1.7 1.7 0 001.8.4H9a1.7 1.7 0 001-1.5V3a2 2 0 114 0v.1a1.7 1.7 0 001 1.5 1.7 1.7 0 001.8-.4l.1-.1a2 2 0 112.8 2.8l-.1.1a1.7 1.7 0 00-.4 1.8v.1a1.7 1.7 0 001.5 1H21a2 2 0 110 4h-.1a1.7 1.7 0 00-1.5 1z"/></svg>;
    case 'logout':     return <svg {...props}><path d="M15 4h3a2 2 0 012 2v12a2 2 0 01-2 2h-3M10 17l-5-5 5-5M5 12h12"/></svg>;
    case 'edit':       return <svg {...props}><path d="M11 4H6a2 2 0 00-2 2v12a2 2 0 002 2h12a2 2 0 002-2v-5"/><path d="M18.4 3.6a2 2 0 012.8 2.8L12 15.6 8 17l1.4-4 9-9z"/></svg>;
    case 'trash':      return <svg {...props}><path d="M4 7h16M9 7V4h6v3M6 7l1 13a2 2 0 002 2h6a2 2 0 002-2l1-13"/></svg>;
    case 'sparkle':    return <svg {...props}><path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3z"/></svg>;
    case 'home':       return <svg {...props}><path d="M4 11l8-7 8 7v8a2 2 0 01-2 2h-3v-6h-6v6H6a2 2 0 01-2-2v-8z"/></svg>;
    case 'x':          return <svg {...props}><path d="M6 6l12 12M18 6L6 18"/></svg>;
    case 'search':     return <svg {...props}><circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/></svg>;
    default: return null;
  }
}

window.ColIcon = ColIcon;
window.colStyle = colStyle;
window.rowStyle = rowStyle;
