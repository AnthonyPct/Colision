// Colision shared components — Buttons, Cards, Chips, AppShell, TopBar, BottomNav, etc.

// ── TopBar ─────────────────────────────────────────────────────────────────
function ColTopBar({ t, title, subtitle, onBack, right, large = false, transparent = false }) {
  return (
    <div style={{
      padding: large ? '8px 20px 8px' : '8px 12px',
      background: transparent ? 'transparent' : t.bg,
      display: 'flex', flexDirection: 'column',
      borderBottom: large ? 'none' : `1px solid ${transparent ? 'transparent' : t.border2}`,
    }}>
      <div style={{ display: 'flex', alignItems: 'center', minHeight: 44 }}>
        <div style={{ width: 44, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {onBack && (
            <button onClick={onBack} style={{
              border: 'none', background: 'transparent', cursor: 'pointer',
              width: 40, height: 40, borderRadius: 999,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: t.ink, padding: 0,
            }}>
              <ColIcon name="arrow-left" size={22} color={t.ink} />
            </button>
          )}
        </div>
        <div style={{ flex: 1, textAlign: 'center', minWidth: 0 }}>
          {!large && title && (
            <div style={{ fontSize: 16, fontWeight: 600, color: t.ink, letterSpacing: -0.1,
              whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
          )}
          {!large && subtitle && (
            <div style={{ fontSize: 12, color: t.inkMuted, marginTop: 1 }}>{subtitle}</div>
          )}
        </div>
        <div style={{ width: 44, height: 44, display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}>
          {right}
        </div>
      </div>
      {large && title && (
        <div style={{ padding: '12px 4px 14px' }}>
          {subtitle && <div style={{ fontSize: 13, color: t.inkMuted, letterSpacing: 0.4, textTransform: 'uppercase', fontWeight: 600, marginBottom: 6 }}>{subtitle}</div>}
          <div style={{
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 38, lineHeight: '1.05', fontWeight: 400,
            color: t.ink, letterSpacing: -0.5,
          }}>{title}</div>
        </div>
      )}
    </div>
  );
}

// ── Buttons ────────────────────────────────────────────────────────────────
function ColButton({ t, label, onClick, variant = 'primary', size = 'md', full = false, icon, iconRight, disabled = false }) {
  const sizes = {
    sm: { h: 36, px: 14, fs: 14 },
    md: { h: 48, px: 20, fs: 16 },
    lg: { h: 56, px: 24, fs: 17 },
  };
  const s = sizes[size];
  let bg, fg, border = 'transparent';
  if (variant === 'primary') { bg = t.ink; fg = t.bg; }
  else if (variant === 'accent') { bg = t.accent; fg = t.accentOn; }
  else if (variant === 'secondary') { bg = t.bgRaised; fg = t.ink; border = t.borderStrong; }
  else if (variant === 'ghost') { bg = 'transparent'; fg = t.ink; }
  else if (variant === 'danger') { bg = t.conflictBg; fg = t.conflictInk; }

  return (
    <button onClick={disabled ? undefined : onClick} disabled={disabled} style={{
      height: s.h, padding: `0 ${s.px}px`, borderRadius: 999,
      background: bg, color: fg, border: `1px solid ${border}`,
      fontFamily: '"Geist", system-ui, sans-serif',
      fontSize: s.fs, fontWeight: 500, letterSpacing: -0.1,
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
      cursor: disabled ? 'not-allowed' : 'pointer',
      opacity: disabled ? 0.45 : 1,
      width: full ? '100%' : 'auto', minWidth: full ? '100%' : 0,
      flexShrink: 0,
    }}>
      {icon && <ColIcon name={icon} size={18} color={fg} />}
      <span>{label}</span>
      {iconRight && <ColIcon name={iconRight} size={18} color={fg} />}
    </button>
  );
}

// ── Card ───────────────────────────────────────────────────────────────────
function ColCard({ t, children, pad = 16, onClick, style = {}, raised = false, accentLeft }) {
  return (
    <div onClick={onClick} style={{
      background: t.bgRaised,
      borderRadius: 16,
      border: `1px solid ${t.borderSubtle}`,
      padding: pad,
      cursor: onClick ? 'pointer' : 'default',
      boxShadow: raised ? t.shadow1 : 'none',
      position: 'relative',
      overflow: 'hidden',
      ...style,
    }}>
      {accentLeft && (
        <div style={{
          position: 'absolute', left: 0, top: 0, bottom: 0, width: 4,
          background: accentLeft,
        }} />
      )}
      {children}
    </div>
  );
}

// ── Commission chip ────────────────────────────────────────────────────────
function ColCommissionChip({ commission, size = 'md', t }) {
  const sizes = { sm: { h: 22, fs: 11, dot: 6, gap: 6, px: 8 }, md: { h: 26, fs: 12, dot: 7, gap: 7, px: 10 }, lg: { h: 32, fs: 13, dot: 8, gap: 8, px: 12 } };
  const s = sizes[size];
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', gap: s.gap,
      height: s.h, padding: `0 ${s.px}px`, borderRadius: 999,
      background: commission.bg, color: commission.ink,
      fontSize: s.fs, fontWeight: 600, letterSpacing: 0.1,
      whiteSpace: 'nowrap',
    }}>
      <span style={{ width: s.dot, height: s.dot, borderRadius: '50%', background: commission.dot, flexShrink: 0 }} />
      {commission.name}
    </span>
  );
}

// ── Avatar (initial circle) ────────────────────────────────────────────────
function ColAvatar({ name, size = 40, t, ring = false }) {
  const initials = (name || '?').split(' ').map(p => p[0]).slice(0, 2).join('').toUpperCase();
  // stable color from name
  const palette = ['#0E7C66','#C9582B','#3B5F8F','#7B3FE4','#B8956A','#A8323F','#5C8049','#7E8439'];
  const idx = (name || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0) % palette.length;
  const bg = palette[idx];
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: bg, color: '#fff',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: '"Geist", system-ui', fontWeight: 600,
      fontSize: size * 0.4, letterSpacing: -0.2,
      flexShrink: 0,
      boxShadow: ring ? `0 0 0 3px ${t.bg}, 0 0 0 4px ${bg}40` : 'none',
    }}>{initials}</div>
  );
}

// ── Bottom Nav ─────────────────────────────────────────────────────────────
function ColBottomNav({ t, tab, onTab, hasArbitration }) {
  const items = [
    { id: 'agenda',     label: 'Agenda',      icon: 'calendar' },
    { id: 'commissions',label: 'Commissions', icon: 'users' },
    { id: 'more',       label: 'Plus',        icon: 'more' },
  ];
  return (
    <div style={{
      background: t.bgRaised,
      borderTop: `1px solid ${t.borderSubtle}`,
      padding: '6px 8px 10px',
      display: 'flex', justifyContent: 'space-around',
      position: 'relative',
    }}>
      {items.map(i => {
        const active = tab === i.id;
        const badge = i.id === 'agenda' && hasArbitration;
        return (
          <button key={i.id} onClick={() => onTab(i.id)} style={{
            flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 3,
            background: 'transparent', border: 'none', cursor: 'pointer',
            padding: '6px 8px', borderRadius: 12, position: 'relative',
            color: active ? t.ink : t.inkMuted,
          }}>
            <div style={{ position: 'relative' }}>
              <ColIcon name={i.icon} size={22} color={active ? t.accent : t.inkMuted} strokeWidth={active ? 2 : 1.6} />
              {badge && (
                <span style={{
                  position: 'absolute', top: -3, right: -6, width: 8, height: 8, borderRadius: '50%',
                  background: t.conflict, border: `2px solid ${t.bgRaised}`,
                }} />
              )}
            </div>
            <span style={{
              fontSize: 11, fontWeight: active ? 600 : 500, letterSpacing: 0.1,
              color: active ? t.ink : t.inkMuted,
            }}>{i.label}</span>
          </button>
        );
      })}
    </div>
  );
}

// ── Field / input wrapper ──────────────────────────────────────────────────
function ColField({ t, label, hint, children, error }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
      {label && <label style={{ fontSize: 13, fontWeight: 600, color: t.inkMuted, letterSpacing: 0.1 }}>{label}</label>}
      {children}
      {hint && !error && <div style={{ fontSize: 12, color: t.inkSubtle }}>{hint}</div>}
      {error && <div style={{ fontSize: 12, color: t.conflict, fontWeight: 500 }}>{error}</div>}
    </div>
  );
}

function ColTextInput({ t, value, onChange, placeholder, autoFocus, big = false, mono = false, align = 'left' }) {
  return (
    <input type="text" value={value || ''} onChange={e => onChange && onChange(e.target.value)} placeholder={placeholder} autoFocus={autoFocus} style={{
      height: big ? 64 : 52, padding: '0 16px',
      borderRadius: 14, border: `1px solid ${t.borderStrong}`,
      background: t.bgRaised, color: t.ink,
      fontFamily: mono ? '"Geist Mono", monospace' : '"Geist", system-ui, sans-serif',
      fontSize: big ? 28 : 16, fontWeight: big ? 500 : 400, letterSpacing: big ? 4 : -0.1,
      textAlign: align, width: '100%', boxSizing: 'border-box',
      outline: 'none',
    }} />
  );
}

// ── Section header ─────────────────────────────────────────────────────────
function ColSectionLabel({ t, children, action }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
      padding: '6px 4px',
    }}>
      <span style={{
        fontSize: 12, fontWeight: 600, color: t.inkMuted,
        letterSpacing: 0.6, textTransform: 'uppercase',
      }}>{children}</span>
      {action}
    </div>
  );
}

// ── Page screen container ─────────────────────────────────────────────────
function ColScreen({ t, children, pad = '0 16px 16px', background }) {
  return (
    <div style={{
      background: background || t.bg, flex: 1, overflow: 'auto',
      padding: pad, color: t.ink,
      fontFamily: '"Geist", system-ui, sans-serif',
      fontSize: 15,
      WebkitFontSmoothing: 'antialiased',
    }}>{children}</div>
  );
}

// expose
Object.assign(window, {
  ColTopBar, ColButton, ColCard, ColCommissionChip, ColAvatar,
  ColBottomNav, ColField, ColTextInput, ColSectionLabel, ColScreen,
});
