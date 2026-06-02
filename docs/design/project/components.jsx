// components.jsx — Colision primitive components (Compose Multiplatform-aligned)

// ────────────────────────────────────────────────────────────
// Button
// ────────────────────────────────────────────────────────────
function Button({ children, variant = 'primary', size = 'md', icon, trailing, onClick, theme, fullWidth, disabled, style, ...rest }) {
  const sizes = {
    sm: { h: 36, px: 16, fs: 14, gap: 6 },
    md: { h: 48, px: 20, fs: 16, gap: 8 },
    lg: { h: 56, px: 24, fs: 17, gap: 10 },
  }[size];

  const variants = {
    primary: { bg: theme.primary, fg: theme.onPrimary, border: 'transparent' },
    secondary: { bg: theme.primaryContainer, fg: theme.onPrimaryContainer, border: 'transparent' },
    tonal: { bg: theme.surfaceContainerHigh, fg: theme.onSurface, border: 'transparent' },
    outlined: { bg: 'transparent', fg: theme.onSurface, border: theme.outline },
    text: { bg: 'transparent', fg: theme.primary, border: 'transparent' },
    danger: { bg: theme.error, fg: theme.onError, border: 'transparent' },
    dangerOutlined: { bg: 'transparent', fg: theme.error, border: theme.error },
  }[variant];

  return (
    <button onClick={disabled ? undefined : onClick} disabled={disabled} style={{
      height: sizes.h, padding: `0 ${sizes.px}px`, gap: sizes.gap,
      borderRadius: variant === 'text' ? 8 : R.pill,
      background: variants.bg, color: variants.fg,
      border: `1.5px solid ${variants.border}`,
      fontFamily: theme.font, fontSize: sizes.fs, fontWeight: 600,
      letterSpacing: '-0.1px',
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.45 : 1,
      width: fullWidth ? '100%' : undefined,
      transition: 'transform 80ms ease, background 120ms ease',
      ...style,
    }} onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.98)'}
       onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
       onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
       {...rest}>
      {icon}
      {children}
      {trailing}
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// Card — outlined / elevated / filled
// ────────────────────────────────────────────────────────────
function Card({ children, theme, variant, style, onClick, padding = 16, ...rest }) {
  const v = variant || theme.cardStyle || 'outlined';
  const base = {
    background: theme.surface,
    borderRadius: R.lg,
    padding,
    fontFamily: theme.font,
    color: theme.onSurface,
    cursor: onClick ? 'pointer' : undefined,
    transition: 'transform 120ms ease',
  };
  const variants = {
    outlined: { ...base, border: `1px solid ${theme.outline}` },
    elevated: { ...base, boxShadow: `0 1px 0 ${theme.outlineSubtle}, 0 6px 18px -8px ${theme.shadow}` },
    filled: { ...base, background: theme.surfaceContainer, border: 'none' },
  };
  return (
    <div onClick={onClick} style={{ ...variants[v], ...style }} {...rest}>
      {children}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Chip
// ────────────────────────────────────────────────────────────
function Chip({ children, selected, onClick, theme, leading, size = 'md', variant = 'choice' }) {
  const sizes = {
    sm: { h: 28, px: 10, fs: 12 },
    md: { h: 36, px: 14, fs: 14 },
  }[size];

  let bg, fg, border;
  if (variant === 'choice') {
    bg = selected ? theme.primaryContainer : 'transparent';
    fg = selected ? theme.onPrimaryContainer : theme.onSurface;
    border = selected ? 'transparent' : theme.outline;
  } else if (variant === 'filter') {
    bg = selected ? theme.onSurface : theme.surfaceContainer;
    fg = selected ? theme.surface : theme.onSurfaceVariant;
    border = 'transparent';
  } else {
    bg = theme.surfaceContainer;
    fg = theme.onSurfaceVariant;
    border = 'transparent';
  }

  return (
    <button onClick={onClick} style={{
      height: sizes.h, padding: `0 ${sizes.px}px`,
      background: bg, color: fg, border: `1.25px solid ${border}`,
      borderRadius: R.pill, fontFamily: theme.font, fontSize: sizes.fs, fontWeight: 500,
      display: 'inline-flex', alignItems: 'center', gap: 6,
      cursor: onClick ? 'pointer' : 'default',
      flexShrink: 0,
    }}>
      {selected && variant === 'choice' && <Icon.Check size={14} stroke={fg} strokeWidth={2.5}/>}
      {leading}
      {children}
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// TextField
// ────────────────────────────────────────────────────────────
function TextField({ label, value, onChange, theme, placeholder, hint, error, size = 'md', mono, autoFocus, maxLength, style, leading, trailing }) {
  const [focused, setFocused] = React.useState(false);
  const h = size === 'lg' ? 64 : 56;
  const borderColor = error ? theme.error : focused ? theme.primary : theme.outline;

  return (
    <div style={{ ...style }}>
      <div style={{
        height: h, padding: '0 16px',
        background: theme.surface,
        border: `1.5px solid ${borderColor}`,
        borderRadius: R.md,
        display: 'flex', alignItems: 'center', gap: 12,
        transition: 'border-color 150ms ease',
        position: 'relative',
      }}>
        {leading}
        <div style={{ flex: 1, position: 'relative' }}>
          {label && (
            <div style={{
              position: 'absolute',
              left: 0,
              top: value || focused ? -2 : '50%',
              transform: value || focused ? 'translateY(0)' : 'translateY(-50%)',
              fontSize: value || focused ? 12 : 16,
              color: error ? theme.error : focused ? theme.primary : theme.onSurfaceVariant,
              fontFamily: theme.font, fontWeight: 500,
              transition: 'all 150ms ease',
              pointerEvents: 'none',
              background: theme.surface,
              padding: value || focused ? '0 4px' : '0',
              marginLeft: value || focused ? -4 : 0,
            }}>{label}</div>
          )}
          <input value={value || ''} onChange={(e) => onChange?.(e.target.value)}
            onFocus={() => setFocused(true)} onBlur={() => setFocused(false)}
            placeholder={!label ? placeholder : (focused ? placeholder : '')}
            autoFocus={autoFocus} maxLength={maxLength}
            style={{
              width: '100%', height: 24, border: 'none', outline: 'none',
              background: 'transparent', color: theme.onSurface,
              fontFamily: mono ? theme.fontMono : theme.font,
              fontSize: mono ? 22 : 16, fontWeight: mono ? 600 : 400,
              letterSpacing: mono ? '0.3em' : 'normal',
              paddingTop: label ? 14 : 0,
            }} />
        </div>
        {trailing}
      </div>
      {(hint || error) && (
        <div style={{
          marginTop: 6, paddingLeft: 16,
          fontSize: 12, color: error ? theme.error : theme.onSurfaceVariant,
          fontFamily: theme.font,
        }}>{error || hint}</div>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Banner — for inline alerts / conflicts / info
// ────────────────────────────────────────────────────────────
function Banner({ children, tone = 'info', icon, action, onAction, theme, dense, title }) {
  const tones = {
    info: { bg: theme.infoContainer, fg: theme.onInfoContainer, accent: theme.info },
    warning: { bg: theme.warningContainer, fg: theme.onWarningContainer, accent: theme.warning },
    error: { bg: theme.errorContainer, fg: theme.onErrorContainer, accent: theme.error },
    success: { bg: theme.successContainer, fg: theme.onPrimaryContainer, accent: theme.primary },
    neutral: { bg: theme.surfaceContainer, fg: theme.onSurface, accent: theme.onSurfaceVariant },
  }[tone];

  return (
    <div style={{
      background: tones.bg, color: tones.fg,
      borderRadius: R.md, padding: dense ? '10px 14px' : '14px 16px',
      display: 'flex', alignItems: 'flex-start', gap: 12,
      fontFamily: theme.font,
    }}>
      {icon !== false && (
        <div style={{ color: tones.accent, marginTop: 1, flexShrink: 0 }}>
          {icon || <Icon.Info size={20}/>}
        </div>
      )}
      <div style={{ flex: 1, minWidth: 0 }}>
        {title && <div style={{ fontSize: 15, fontWeight: 600, marginBottom: 2 }}>{title}</div>}
        <div style={{ fontSize: 14, lineHeight: '20px' }}>{children}</div>
        {action && (
          <button onClick={onAction} style={{
            marginTop: 8, background: 'transparent', border: 'none',
            color: tones.accent, fontFamily: theme.font, fontSize: 14, fontWeight: 600,
            padding: 0, cursor: 'pointer',
          }}>{action} →</button>
        )}
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Avatar — initials, with auto color
// ────────────────────────────────────────────────────────────
const AVATAR_COLORS = [
  ['#D7E8DC', '#0E5C44'], ['#F9DDD3', '#8A2F18'], ['#DBE5FA', '#1E3B8A'],
  ['#F8E6CC', '#7A4B0E'], ['#E5D9F9', '#4A2080'], ['#FCE0E8', '#8C1F47'],
  ['#D0EAEE', '#0F4F5C'], ['#E5EBD3', '#4A5C18'],
];
function avatarColors(name = '') {
  let h = 0;
  for (let i = 0; i < name.length; i++) h = (h * 31 + name.charCodeAt(i)) >>> 0;
  return AVATAR_COLORS[h % AVATAR_COLORS.length];
}
function initials(name = '') {
  const parts = name.trim().split(/\s+/);
  return (parts[0]?.[0] || '') + (parts[1]?.[0] || '');
}
function Avatar({ name, size = 40, theme, style }) {
  const [bg, fg] = avatarColors(name);
  return (
    <div style={{
      width: size, height: size, borderRadius: '50%',
      background: bg, color: fg,
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      fontFamily: theme?.font || '"DM Sans", sans-serif',
      fontSize: size * 0.4, fontWeight: 600,
      flexShrink: 0,
      ...style,
    }}>{initials(name).toUpperCase()}</div>
  );
}

// ────────────────────────────────────────────────────────────
// AppBar (top) — center title, leading + trailing
// ────────────────────────────────────────────────────────────
function AppBar({ title, leading, trailing, theme, scrolled, large, subtitle }) {
  return (
    <div style={{
      background: theme.background,
      borderBottom: scrolled ? `1px solid ${theme.divider}` : '1px solid transparent',
      paddingTop: 0,
      transition: 'border-color 150ms ease',
      position: 'sticky', top: 0, zIndex: 10,
    }}>
      <div style={{
        height: 56, display: 'flex', alignItems: 'center',
        padding: '0 8px', gap: 4,
      }}>
        <div style={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          {leading}
        </div>
        {!large && (
          <div style={{ flex: 1, textAlign: 'center', overflow: 'hidden' }}>
            <div style={{
              fontFamily: theme.font, fontSize: 17, fontWeight: 600,
              color: theme.onSurface, letterSpacing: '-0.1px',
              whiteSpace: 'nowrap', textOverflow: 'ellipsis', overflow: 'hidden',
            }}>{title}</div>
            {subtitle && <div style={{
              fontSize: 12, color: theme.onSurfaceVariant, fontFamily: theme.font,
            }}>{subtitle}</div>}
          </div>
        )}
        {large && <div style={{ flex: 1 }} />}
        <div style={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 0 }}>
          {trailing}
        </div>
      </div>
      {large && (
        <div style={{ padding: '4px 20px 16px' }}>
          <div style={{ ...T.headline, color: theme.onSurface, fontFamily: theme.font }}>{title}</div>
          {subtitle && <div style={{
            marginTop: 4, fontSize: 14, color: theme.onSurfaceVariant, fontFamily: theme.font,
          }}>{subtitle}</div>}
        </div>
      )}
    </div>
  );
}

// IconButton — circular tap target, used as leading/trailing in AppBar
function IconButton({ icon, onClick, theme, badge }) {
  return (
    <button onClick={onClick} style={{
      width: 40, height: 40, borderRadius: 20,
      background: 'transparent', border: 'none',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: theme.onSurface, cursor: 'pointer', position: 'relative',
    }}>
      {icon}
      {badge && (
        <div style={{
          position: 'absolute', top: 6, right: 6,
          width: 9, height: 9, borderRadius: '50%',
          background: theme.error, border: `2px solid ${theme.background}`,
        }} />
      )}
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// BottomNav
// ────────────────────────────────────────────────────────────
function BottomNav({ items, active, onChange, theme }) {
  return (
    <div style={{
      background: theme.surface,
      borderTop: `1px solid ${theme.divider}`,
      padding: '8px 8px 12px',
      display: 'flex', justifyContent: 'space-around',
      flexShrink: 0,
    }}>
      {items.map((it) => {
        const isActive = it.key === active;
        return (
          <button key={it.key} onClick={() => onChange(it.key)} style={{
            background: 'transparent', border: 'none', cursor: 'pointer',
            display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4,
            padding: '6px 12px', flex: 1, minWidth: 0,
          }}>
            <div style={{
              padding: '4px 18px', borderRadius: 16,
              background: isActive ? theme.primaryContainer : 'transparent',
              transition: 'background 150ms ease',
              color: isActive ? theme.onPrimaryContainer : theme.onSurfaceVariant,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              position: 'relative',
            }}>
              {it.icon}
              {it.badge && (
                <div style={{
                  position: 'absolute', top: 0, right: 10,
                  minWidth: 16, height: 16, borderRadius: 8, padding: '0 4px',
                  background: theme.error, color: theme.onError,
                  fontFamily: theme.font, fontSize: 10, fontWeight: 700,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  border: `2px solid ${theme.surface}`, boxSizing: 'content-box',
                }}>{it.badge}</div>
              )}
            </div>
            <div style={{
              fontFamily: theme.font, fontSize: 11, fontWeight: isActive ? 600 : 500,
              color: isActive ? theme.onSurface : theme.onSurfaceVariant,
              letterSpacing: '0.1px',
            }}>{it.label}</div>
          </button>
        );
      })}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// FAB — Floating Action Button (extended)
// ────────────────────────────────────────────────────────────
function FAB({ icon, label, onClick, theme, extended = true, style }) {
  return (
    <button onClick={onClick} style={{
      height: 56,
      padding: extended ? '0 20px' : 0,
      width: extended ? 'auto' : 56,
      background: theme.primaryContainer,
      color: theme.onPrimaryContainer,
      border: 'none', borderRadius: R.lg,
      display: 'inline-flex', alignItems: 'center', gap: 10,
      fontFamily: theme.font, fontSize: 15, fontWeight: 600,
      cursor: 'pointer',
      boxShadow: `0 1px 0 ${theme.outlineSubtle}, 0 10px 24px -10px rgba(14,124,102,0.45)`,
      ...style,
    }}>
      {icon}
      {extended && label}
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// ListRow — generic row inside a card
// ────────────────────────────────────────────────────────────
function ListRow({ leading, headline, supporting, trailing, onClick, theme, divider }) {
  return (
    <div onClick={onClick} style={{
      display: 'flex', alignItems: 'center', gap: 14,
      padding: '14px 16px', minHeight: 56,
      cursor: onClick ? 'pointer' : 'default',
      borderBottom: divider ? `1px solid ${theme.divider}` : 'none',
      fontFamily: theme.font,
    }}>
      {leading}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15, fontWeight: 500, color: theme.onSurface, letterSpacing: '-0.05px' }}>{headline}</div>
        {supporting && (
          <div style={{ fontSize: 13, color: theme.onSurfaceVariant, marginTop: 2 }}>{supporting}</div>
        )}
      </div>
      {trailing}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// SectionLabel — small heading inside a screen
// ────────────────────────────────────────────────────────────
function SectionLabel({ children, theme, style, action, onAction }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
      padding: '0 4px', ...style,
    }}>
      <div style={{
        fontFamily: theme.font, fontSize: 12, fontWeight: 600,
        color: theme.onSurfaceVariant,
        letterSpacing: '0.6px', textTransform: 'uppercase',
      }}>{children}</div>
      {action && (
        <button onClick={onAction} style={{
          background: 'transparent', border: 'none', cursor: 'pointer',
          color: theme.primary, fontFamily: theme.font, fontSize: 13, fontWeight: 600,
          padding: 0,
        }}>{action}</button>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Divider
// ────────────────────────────────────────────────────────────
function Divider({ theme, style }) {
  return <div style={{ height: 1, background: theme.divider, ...style }}/>;
}

// ────────────────────────────────────────────────────────────
// Sheet — bottom sheet container (modal-like surface)
// ────────────────────────────────────────────────────────────
function Sheet({ children, theme, onClose, title }) {
  return (
    <div style={{
      position: 'absolute', inset: 0, zIndex: 100,
      display: 'flex', flexDirection: 'column', justifyContent: 'flex-end',
    }}>
      <div onClick={onClose} style={{
        position: 'absolute', inset: 0, background: theme.scrim,
      }}/>
      <div style={{
        position: 'relative',
        background: theme.surface,
        borderTopLeftRadius: 28, borderTopRightRadius: 28,
        padding: '12px 0 0',
        maxHeight: '85%',
        display: 'flex', flexDirection: 'column',
        boxShadow: '0 -10px 30px rgba(0,0,0,0.12)',
      }}>
        <div style={{
          width: 36, height: 4, borderRadius: 2,
          background: theme.outline, margin: '0 auto 8px',
        }}/>
        {title && (
          <div style={{
            padding: '12px 20px 16px',
            fontFamily: theme.font, fontSize: 18, fontWeight: 700,
            color: theme.onSurface,
          }}>{title}</div>
        )}
        <div style={{ overflow: 'auto', padding: '0 20px 24px' }}>
          {children}
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// PushNotification — overlay simulating an OS push
// ────────────────────────────────────────────────────────────
function PushNotification({ theme, title, body, app = 'Colision', onTap, style }) {
  return (
    <div onClick={onTap} style={{
      position: 'absolute', top: 0, left: 0, right: 0, zIndex: 200,
      padding: '60px 12px 0',
      ...style,
    }}>
      <div style={{
        background: 'rgba(255,255,255,0.78)',
        backdropFilter: 'blur(24px) saturate(180%)',
        WebkitBackdropFilter: 'blur(24px) saturate(180%)',
        borderRadius: 22, padding: '12px 14px',
        boxShadow: '0 6px 24px rgba(0,0,0,0.12)',
        border: '0.5px solid rgba(0,0,0,0.08)',
        display: 'flex', gap: 12, alignItems: 'flex-start',
        cursor: 'pointer',
        fontFamily: theme.font,
      }}>
        <div style={{
          width: 38, height: 38, borderRadius: 9,
          background: theme.primary,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          flexShrink: 0,
        }}>
          <Icon.Logo size={24} color={theme.onPrimary} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 2 }}>
            <div style={{ fontSize: 13, fontWeight: 600, color: '#1A1F1C' }}>{app}</div>
            <div style={{ fontSize: 12, color: 'rgba(60,60,67,0.6)' }}>maintenant</div>
          </div>
          <div style={{ fontSize: 14, fontWeight: 600, color: '#1A1F1C', marginBottom: 2 }}>{title}</div>
          <div style={{ fontSize: 14, color: 'rgba(60,60,67,0.85)', lineHeight: '18px' }}>{body}</div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, {
  Button, Card, Chip, TextField, Banner, Avatar, AppBar, IconButton,
  BottomNav, FAB, ListRow, SectionLabel, Divider, Sheet, PushNotification,
  avatarColors, initials,
});
