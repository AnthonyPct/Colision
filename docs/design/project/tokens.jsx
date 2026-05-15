// tokens.jsx — Design tokens for Colision
// Single source of truth. Compose Multiplatform-friendly (M3-style names).

const PALETTES = {
  forest: {
    primary: '#0E7C66',
    primaryContainer: '#C7E8DC',
    onPrimary: '#FFFFFF',
    onPrimaryContainer: '#032E25',
    primaryHover: '#0A6857',
  },
  coral: {
    primary: '#C8553D',
    primaryContainer: '#F9DDD3',
    onPrimary: '#FFFFFF',
    onPrimaryContainer: '#421006',
    primaryHover: '#A8442F',
  },
  indigo: {
    primary: '#3F5BD9',
    primaryContainer: '#DBE5FA',
    onPrimary: '#FFFFFF',
    onPrimaryContainer: '#0B1A57',
    primaryHover: '#3349B8',
  },
  plum: {
    primary: '#7B3FE4',
    primaryContainer: '#E5D9F9',
    onPrimary: '#FFFFFF',
    onPrimaryContainer: '#2B0E5C',
    primaryHover: '#6730C4',
  },
};

const LIGHT = {
  background: '#FAF7F2',
  surface: '#FFFFFF',
  surfaceContainer: '#F4EFE5',
  surfaceContainerHigh: '#EBE5D6',
  surfaceDim: '#E8E2D3',

  outline: '#DDD5C3',
  outlineSubtle: '#EBE5D6',
  divider: '#E8E2D3',

  onSurface: '#1A1F1C',
  onSurfaceStrong: '#0E1310',
  onSurfaceVariant: '#5A615C',
  onSurfaceMuted: '#8A8F89',

  error: '#C8553D',
  errorContainer: '#F9DDD3',
  onError: '#FFFFFF',
  onErrorContainer: '#421006',

  warning: '#C77F1A',
  warningContainer: '#F8E6CC',
  onWarningContainer: '#3D2900',

  info: '#4A6FB5',
  infoContainer: '#DBE5FA',
  onInfoContainer: '#0B1A57',

  success: '#0E7C66',
  successContainer: '#C7E8DC',

  scrim: 'rgba(15, 19, 16, 0.55)',
  shadow: 'rgba(15, 19, 16, 0.08)',
};

const DARK = {
  background: '#121512',
  surface: '#1A1F1C',
  surfaceContainer: '#222825',
  surfaceContainerHigh: '#2A312D',
  surfaceDim: '#0E1310',

  outline: '#3A413D',
  outlineSubtle: '#2A312D',
  divider: '#2A312D',

  onSurface: '#F0EBE0',
  onSurfaceStrong: '#FFFFFF',
  onSurfaceVariant: '#B4B8B1',
  onSurfaceMuted: '#80847E',

  error: '#E78870',
  errorContainer: '#5C2615',
  onError: '#FFFFFF',
  onErrorContainer: '#F9DDD3',

  warning: '#E7B463',
  warningContainer: '#5C4416',
  onWarningContainer: '#F8E6CC',

  info: '#88A4E8',
  infoContainer: '#1F3279',
  onInfoContainer: '#DBE5FA',

  success: '#83D5C6',
  successContainer: '#1F4A40',

  scrim: 'rgba(0, 0, 0, 0.6)',
  shadow: 'rgba(0, 0, 0, 0.35)',
};

// Spacing scale (4-based)
const SP = { 1: 4, 2: 8, 3: 12, 4: 16, 5: 20, 6: 24, 7: 28, 8: 32, 10: 40, 12: 48, 16: 64 };

// Radius scale
const R = { xs: 6, sm: 10, md: 14, lg: 18, xl: 24, pill: 999 };

// Type scale (DM Sans)
const T = {
  display: { fontSize: 32, lineHeight: '40px', fontWeight: 700, letterSpacing: '-0.5px' },
  headline: { fontSize: 26, lineHeight: '32px', fontWeight: 700, letterSpacing: '-0.3px' },
  titleL: { fontSize: 20, lineHeight: '26px', fontWeight: 600, letterSpacing: '-0.2px' },
  titleM: { fontSize: 17, lineHeight: '22px', fontWeight: 600, letterSpacing: '-0.1px' },
  titleS: { fontSize: 15, lineHeight: '20px', fontWeight: 600 },
  bodyL: { fontSize: 17, lineHeight: '24px', fontWeight: 400 },
  bodyM: { fontSize: 15, lineHeight: '22px', fontWeight: 400 },
  bodyS: { fontSize: 13, lineHeight: '18px', fontWeight: 400 },
  label: { fontSize: 13, lineHeight: '18px', fontWeight: 500, letterSpacing: '0.1px' },
  caption: { fontSize: 11, lineHeight: '14px', fontWeight: 500, letterSpacing: '0.4px', textTransform: 'uppercase' },
  button: { fontSize: 16, lineHeight: '20px', fontWeight: 600, letterSpacing: '-0.1px' },
};

// Build a theme by composing palette + mode
function buildTheme({ palette = 'forest', mode = 'light', density = 'cozy', cardStyle = 'outlined' } = {}) {
  const p = PALETTES[palette] || PALETTES.forest;
  const base = mode === 'dark' ? DARK : LIGHT;
  return {
    ...base,
    ...p,
    mode,
    density,
    cardStyle,
    palette,
    font: '"DM Sans", -apple-system, BlinkMacSystemFont, system-ui, sans-serif',
    fontMono: '"JetBrains Mono", "SF Mono", ui-monospace, monospace',
  };
}

Object.assign(window, { PALETTES, LIGHT, DARK, SP, R, T, buildTheme });
