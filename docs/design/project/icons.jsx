// icons.jsx — Hand-tuned line icons for Colision. 24px viewBox, stroke 1.75.

const Ico = ({ d, size = 24, stroke = 'currentColor', fill = 'none', strokeWidth = 1.75, children }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill={fill} stroke={stroke}
       strokeWidth={strokeWidth} strokeLinecap="round" strokeLinejoin="round"
       style={{ flexShrink: 0, display: 'block' }}>
    {d ? <path d={d} /> : children}
  </svg>
);

const Icon = {
  Calendar: (p) => <Ico {...p}>
    <rect x="3" y="5" width="18" height="16" rx="2.5"/>
    <path d="M3 9h18M8 3v4M16 3v4"/>
  </Ico>,
  CalendarGrid: (p) => <Ico {...p}>
    <rect x="3" y="5" width="18" height="16" rx="2.5"/>
    <path d="M3 10h18M8 3v4M16 3v4M9 14h.01M13 14h.01M17 14h.01M9 18h.01M13 18h.01"/>
  </Ico>,
  Plus: (p) => <Ico d="M12 5v14M5 12h14" {...p}/>,
  Check: (p) => <Ico d="M4 12.5l5 5L20 7" {...p}/>,
  X: (p) => <Ico d="M6 6l12 12M18 6L6 18" {...p}/>,
  ChevronLeft: (p) => <Ico d="M15 6l-6 6 6 6" {...p}/>,
  ChevronRight: (p) => <Ico d="M9 6l6 6-6 6" {...p}/>,
  ChevronDown: (p) => <Ico d="M6 9l6 6 6-6" {...p}/>,
  ChevronUp: (p) => <Ico d="M6 15l6-6 6 6" {...p}/>,
  ArrowRight: (p) => <Ico d="M5 12h14M13 6l6 6-6 6" {...p}/>,
  ArrowLeft: (p) => <Ico d="M19 12H5M11 6l-6 6 6 6" {...p}/>,
  Bell: (p) => <Ico {...p}>
    <path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9"/>
    <path d="M10.3 21a1.94 1.94 0 003.4 0"/>
  </Ico>,
  BellAlert: (p) => <Ico {...p}>
    <path d="M6 8a6 6 0 0112 0c0 7 3 9 3 9H3s3-2 3-9"/>
    <path d="M10.3 21a1.94 1.94 0 003.4 0"/>
    <circle cx="18" cy="6" r="3" fill="currentColor" stroke="none"/>
  </Ico>,
  Users: (p) => <Ico {...p}>
    <path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2"/>
    <circle cx="9" cy="7" r="4"/>
    <path d="M22 21v-2a4 4 0 00-3-3.87M16 3.13a4 4 0 010 7.75"/>
  </Ico>,
  User: (p) => <Ico {...p}>
    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </Ico>,
  Settings: (p) => <Ico {...p}>
    <circle cx="12" cy="12" r="3"/>
    <path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 11-2.83 2.83l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 11-4 0v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 11-2.83-2.83l.06-.06A1.65 1.65 0 004.6 15a1.65 1.65 0 00-1.51-1H3a2 2 0 110-4h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 112.83-2.83l.06.06A1.65 1.65 0 009 4.6a1.65 1.65 0 001-1.51V3a2 2 0 114 0v.09A1.65 1.65 0 0015 4.6a1.65 1.65 0 001.82-.33l.06-.06a2 2 0 112.83 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 010 4h-.09a1.65 1.65 0 00-1.51 1z"/>
  </Ico>,
  AlertTriangle: (p) => <Ico {...p}>
    <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
    <path d="M12 9v4M12 17h.01"/>
  </Ico>,
  Info: (p) => <Ico {...p}>
    <circle cx="12" cy="12" r="9"/>
    <path d="M12 8h.01M11 12h1v4h1"/>
  </Ico>,
  Clock: (p) => <Ico {...p}>
    <circle cx="12" cy="12" r="9"/>
    <path d="M12 7v5l3 2"/>
  </Ico>,
  Folder: (p) => <Ico d="M22 19a2 2 0 01-2 2H4a2 2 0 01-2-2V5a2 2 0 012-2h5l2 3h9a2 2 0 012 2z" {...p}/>,
  Hash: (p) => <Ico d="M4 9h16M4 15h16M10 3L8 21M16 3l-2 18" {...p}/>,
  Search: (p) => <Ico {...p}>
    <circle cx="11" cy="11" r="7"/>
    <path d="M21 21l-4.35-4.35"/>
  </Ico>,
  Copy: (p) => <Ico {...p}>
    <rect x="9" y="9" width="13" height="13" rx="2"/>
    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
  </Ico>,
  Share: (p) => <Ico {...p}>
    <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
    <path d="M8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98"/>
  </Ico>,
  Edit: (p) => <Ico d="M12 20h9M16.5 3.5a2.121 2.121 0 113 3L7 19l-4 1 1-4L16.5 3.5z" {...p}/>,
  Trash: (p) => <Ico {...p}>
    <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6"/>
  </Ico>,
  Sparkles: (p) => <Ico {...p}>
    <path d="M12 3l1.7 4.7L18 9.5l-4.3 1.8L12 16l-1.7-4.7L6 9.5l4.3-1.8L12 3z"/>
    <path d="M19 14l.8 2.2L22 17l-2.2.8L19 20l-.8-2.2L16 17l2.2-.8L19 14zM5 14l.8 2.2L8 17l-2.2.8L5 20l-.8-2.2L2 17l2.2-.8L5 14z"/>
  </Ico>,
  Dot: ({ size = 8, fill = 'currentColor' }) => (
    <svg width={size} height={size} viewBox="0 0 8 8"><circle cx="4" cy="4" r="4" fill={fill}/></svg>
  ),
  Logo: ({ size = 32, color = '#0E7C66' }) => (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none">
      <circle cx="11" cy="16" r="8" stroke={color} strokeWidth="2.2"/>
      <circle cx="21" cy="16" r="8" stroke={color} strokeWidth="2.2"/>
      <path d="M11.5 11.5L20.5 20.5M11.5 20.5L20.5 11.5" stroke={color} strokeWidth="2.2" strokeLinecap="round"/>
    </svg>
  ),
  MoreH: (p) => <Ico {...p}>
    <circle cx="5" cy="12" r="1.5" fill="currentColor" stroke="none"/>
    <circle cx="12" cy="12" r="1.5" fill="currentColor" stroke="none"/>
    <circle cx="19" cy="12" r="1.5" fill="currentColor" stroke="none"/>
  </Ico>,
  Filter: (p) => <Ico d="M22 3H2l8 9.46V19l4 2v-8.54L22 3z" {...p}/>,
  LogOut: (p) => <Ico {...p}>
    <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/>
  </Ico>,
};

Object.assign(window, { Icon });
