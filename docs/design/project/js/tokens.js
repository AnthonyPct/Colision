// Colision design tokens — light + dark, density, accent

window.COLISION_TOKENS = (function () {
  const ACCENTS = {
    teal:    { dot: '#0E7C66', strong: '#08493C', soft: '#D6EAE3', muted: '#E6F0EC', on: '#FFFFFF' },
    indigo:  { dot: '#3F5BD9', strong: '#22357D', soft: '#DEE3F7', muted: '#ECEFFB', on: '#FFFFFF' },
    rust:    { dot: '#C9582B', strong: '#7E3614', soft: '#FBE9DF', muted: '#FAEFE9', on: '#FFFFFF' },
    plum:    { dot: '#7B3FE4', strong: '#42235E', soft: '#E8DDF2', muted: '#EFE9F7', on: '#FFFFFF' },
  };

  const LIGHT = {
    bg:           '#F5F1E8',
    bgRaised:     '#FFFFFF',
    bgMuted:      '#FAF6EC',
    bgInverse:    '#10221E',
    ink:          '#10221E',
    inkMuted:     '#5C6963',
    inkSubtle:    '#9AA39E',
    inkOnAccent:  '#FFFFFF',
    borderSubtle: '#EAE3D2',
    borderStrong: '#D6CDB7',
    border2:      '#F0EAD9',
    conflict:     '#C9582B',
    conflictInk:  '#7E3614',
    conflictBg:   '#FBE9DF',
    success:      '#0E7C66',
    info:         '#3B5F8F',
    overlay:      'rgba(16, 34, 30, 0.42)',
    shadow1:      '0 1px 2px rgba(15, 31, 27, 0.05)',
    shadow2:      '0 1px 2px rgba(15, 31, 27, 0.04), 0 8px 24px rgba(15, 31, 27, 0.06)',
    shadow3:      '0 2px 4px rgba(15, 31, 27, 0.05), 0 16px 40px rgba(15, 31, 27, 0.10)',
  };

  const DARK = {
    bg:           '#0F1714',
    bgRaised:     '#19211E',
    bgMuted:      '#141C19',
    bgInverse:    '#F5F1E8',
    ink:          '#F0EBDC',
    inkMuted:     '#A6AEA9',
    inkSubtle:    '#6F7872',
    inkOnAccent:  '#FFFFFF',
    borderSubtle: '#22302B',
    borderStrong: '#324039',
    border2:      '#1E2823',
    conflict:     '#E1825A',
    conflictInk:  '#FBE9DF',
    conflictBg:   '#3A211A',
    success:      '#5CCFB5',
    info:         '#7FA0CC',
    overlay:      'rgba(0, 0, 0, 0.6)',
    shadow1:      '0 1px 2px rgba(0,0,0,0.4)',
    shadow2:      '0 1px 2px rgba(0,0,0,0.3), 0 8px 24px rgba(0,0,0,0.4)',
    shadow3:      '0 2px 4px rgba(0,0,0,0.3), 0 16px 40px rgba(0,0,0,0.5)',
  };

  const DENSITY = {
    confortable: { rowH: 56, gap: 12, cardPad: 16, titleSize: 28 },
    compact:     { rowH: 48, gap: 8,  cardPad: 12, titleSize: 24 },
  };

  function build(opts) {
    const mode = opts.dark ? DARK : LIGHT;
    const accent = ACCENTS[opts.accent] || ACCENTS.teal;
    const density = DENSITY[opts.density] || DENSITY.confortable;
    return {
      ...mode,
      accent: accent.dot,
      accentStrong: accent.strong,
      accentSoft: accent.soft,
      accentMuted: accent.muted,
      accentOn: accent.on,
      density,
      dark: !!opts.dark,
    };
  }

  return { LIGHT, DARK, ACCENTS, DENSITY, build };
})();
