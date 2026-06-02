// print-host.jsx — Static print layout: every key screen rendered iOS + Android side by side.

const PRINT_SCREENS = [
  { k: 'welcome', section: 'Onboarding', label: 'Accueil — premier lancement', desc: "Deux choix : créer un projet, rejoindre. Pas d'inscription." },
  { k: 'join-code', section: 'Onboarding', label: 'Saisie du code projet', desc: 'OTP-style 6 caractères, validation en temps réel.' },
  { k: 'join-confirm', section: 'Onboarding', label: 'Confirmation du projet', desc: "Vue d'ensemble : commissions, membres déjà inscrits." },
  { k: 'join-identity', section: 'Onboarding', label: "Choix de l'identité", desc: 'Sélection dans la liste pré-remplie, recherche, ajout libre.' },
  { k: 'join-commissions', section: 'Onboarding', label: 'Sélection des commissions', desc: 'Pré-cochées par le créateur du projet, ajustables.' },
  { k: 'notification-perm', section: 'Onboarding', label: 'Permission notifications', desc: 'Explication contextuelle avant le dialogue OS natif.' },

  { k: 'home', section: 'Quotidien', label: 'Agenda personnel — vue semaine', desc: 'Aggrégation des réunions des commissions. Bandeau conflit prioritaire.' },
  { k: 'home-month', section: 'Quotidien', label: 'Agenda personnel — vue mois', desc: 'Grille mensuelle avec puces de couleur par commission.' },
  { k: 'commissions', section: 'Quotidien', label: 'Liste des commissions', desc: 'Toutes les commissions du projet, avec celles dont je fais partie.' },
  { k: 'commission-detail', section: 'Quotidien', label: 'Détail d\'une commission', desc: 'Réunions à venir + membres de cette commission.' },
  { k: 'members', section: 'Quotidien', label: 'Liste des membres', desc: 'Recherche + commissions par membre.' },
  { k: 'project', section: 'Quotidien', label: 'Projet & invitation', desc: 'Code partage, raccourcis, zone sensible (quitter / supprimer).' },

  { k: 'create-meeting', section: 'Conflit', label: 'Création d\'une réunion', desc: 'Titre, date, début, durée, commissions. Détection live.' },
  { k: 'conflicts', section: 'Conflit', label: 'Conflits détectés', desc: 'Membres en conflit + 3 actions (suggérer / décaler / forcer).' },
  { k: 'suggestions', section: 'Conflit', label: 'Créneaux libres suggérés', desc: 'Algorithme propose 3 créneaux où tout le monde est libre.' },
  { k: 'meeting-detail', section: 'Conflit', label: 'Détail d\'une réunion', desc: 'Date, créneau, convoqués, créateur.' },

  { k: 'arbitration', section: 'Arbitrage', label: 'Écran d\'arbitrage (deep-link push)', desc: 'Deux réunions côte à côte, trois actions.' },
  { k: 'organizer-status', section: 'Arbitrage', label: 'Suivi consolidé organisateur', desc: 'Qui vient, qui va ailleurs, qui en attente.' },
];

function PrintHost() {
  // Always light mode forest for print
  const theme = React.useMemo(() => buildTheme({ palette: 'forest', mode: 'light', cardStyle: 'outlined' }), []);

  return (
    <div className="print-root">
      <PrintCover theme={theme}/>
      <PrintTokens theme={theme}/>
      {PRINT_SCREENS.map((s, i) => (
        <PrintPage key={s.k + i} screen={s} theme={theme}/>
      ))}
    </div>
  );
}

function PrintCover({ theme }) {
  return (
    <div className="print-page print-cover">
      <div className="print-cover-inner">
        <div className="print-logo-row">
          <div className="print-logo-mark" style={{ background: theme.primary }}>
            <Icon.Logo size={56} color={theme.onPrimary}/>
          </div>
          <div>
            <div className="print-eyebrow">Hi-fi prototype · Mai 2026</div>
            <div className="print-cover-title">Colision</div>
            <div className="print-cover-sub">Plus jamais deux réunions en même temps.</div>
          </div>
        </div>

        <div className="print-cover-grid">
          <div>
            <div className="print-eyebrow">Persona</div>
            <div className="print-cover-body">Sophie, conseillère municipale, peu à l'aise avec la tech. Si elle comprend, n'importe qui comprend.</div>
          </div>
          <div>
            <div className="print-eyebrow">Stack</div>
            <div className="print-cover-body">Kotlin Multiplatform · Compose Multiplatform · Material 3. UI 100 % partagée iOS / Android.</div>
          </div>
          <div>
            <div className="print-eyebrow">Valeur cœur</div>
            <div className="print-cover-body">Détection de conflit d'agenda cross-commissions en temps réel. Arbitrage en 6 secondes.</div>
          </div>
          <div>
            <div className="print-eyebrow">Design system</div>
            <div className="print-cover-body">Off-white terreux, vert forêt, type DM Sans, cartes arrondies 18px, touch targets ≥ 48px.</div>
          </div>
        </div>

        <div className="print-cover-footer">
          <span>{PRINT_SCREENS.length} écrans · iPhone + Pixel côte à côte</span>
          <span>colision · 2026-05-15</span>
        </div>
      </div>
    </div>
  );
}

function PrintTokens({ theme }) {
  const swatches = [
    { name: 'Primary',          v: theme.primary,           sub: '#0E7C66 — actions, focus' },
    { name: 'Primary container',v: theme.primaryContainer,  sub: 'fond doux pour zones actives' },
    { name: 'Background',       v: theme.background,        sub: 'off-white terreux' },
    { name: 'Surface',          v: theme.surface,           sub: 'cartes, listes' },
    { name: 'Surface container',v: theme.surfaceContainer,  sub: 'inputs, chips' },
    { name: 'On surface',       v: theme.onSurface,         sub: 'texte principal' },
    { name: 'On surface variant',v: theme.onSurfaceVariant, sub: 'texte secondaire' },
    { name: 'Error / conflit',  v: theme.error,             sub: '#C8553D' },
    { name: 'Error container',  v: theme.errorContainer,    sub: 'fond doux conflit' },
    { name: 'Warning',          v: theme.warning,           sub: 'arbitrages en attente' },
    { name: 'Success',          v: theme.success,           sub: 'créneaux libres' },
    { name: 'Outline',          v: theme.outline,           sub: 'bordures cartes' },
  ];

  return (
    <div className="print-page print-tokens">
      <div className="print-page-header">
        <div className="print-eyebrow">Design system</div>
        <div className="print-page-title">Tokens</div>
      </div>
      <div className="print-tokens-grid">
        <div>
          <div className="print-section-title">Palette</div>
          <div className="swatches">
            {swatches.map(s => (
              <div className="swatch" key={s.name}>
                <div className="swatch-chip" style={{ background: s.v, borderColor: theme.outline }}/>
                <div>
                  <div className="swatch-name">{s.name}</div>
                  <div className="swatch-sub">{s.sub}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
        <div>
          <div className="print-section-title">Typographie · DM Sans</div>
          <div className="type-stack">
            <div><div className="type-label">Display 32 / 700</div><div style={{ fontSize: 32, fontWeight: 700, letterSpacing: '-0.5px', lineHeight: 1.1 }}>Plus jamais deux réunions</div></div>
            <div><div className="type-label">Headline 26 / 700</div><div style={{ fontSize: 26, fontWeight: 700, letterSpacing: '-0.3px' }}>Mardi 21 mai</div></div>
            <div><div className="type-label">Title 20 / 600</div><div style={{ fontSize: 20, fontWeight: 600 }}>Tournoi inter-quartiers</div></div>
            <div><div className="type-label">Body 15 / 400</div><div style={{ fontSize: 15 }}>Trois membres déjà mobilisés sur ce créneau.</div></div>
            <div><div className="type-label">Caption 11 / 600 · uppercase</div><div style={{ fontSize: 11, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.6px', color: theme.onSurfaceVariant }}>Aujourd'hui · Conflit</div></div>
          </div>
          <div className="print-section-title" style={{ marginTop: 22 }}>Spacing &amp; radius</div>
          <div className="spacing-stack">
            {[6, 10, 14, 18, 24].map(r => (
              <div key={r} className="radius-demo">
                <div style={{ width: 60, height: 40, borderRadius: r, background: theme.primary }}/>
                <div className="swatch-sub">{r}px</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

function PrintPage({ screen, theme }) {
  // For 'home-month', we need to render the home with month view
  const isMonth = screen.k === 'home-month';
  const state = {
    screen: isMonth ? 'home' : screen.k,
    params: {},
    agendaView: isMonth ? 'mois' : 'semaine',
    showPush: false,
  };
  const setState = () => {}; // static

  return (
    <div className="print-page">
      <div className="print-page-header">
        <div className="print-eyebrow">{screen.section}</div>
        <div className="print-page-title">{screen.label}</div>
        <div className="print-page-desc">{screen.desc}</div>
      </div>
      <div className="print-frames">
        <div className="print-frame-wrap">
          <div className="print-platform-tag">iPhone · iOS</div>
          <IOSDevice dark={false}>
            <App theme={theme} state={state} setState={setState} safeTop={58} safeBottom={22}/>
          </IOSDevice>
        </div>
        <div className="print-frame-wrap">
          <div className="print-platform-tag">Pixel · Android</div>
          <AndroidDevice dark={false}>
            <App theme={theme} state={state} setState={setState} safeTop={0} safeBottom={16}/>
          </AndroidDevice>
        </div>
      </div>
      <div className="print-page-footer">
        <span>Colision · Hi-fi prototype</span>
        <span>Compose Multiplatform · UI 100 % partagée</span>
      </div>
    </div>
  );
}

Object.assign(window, { PrintHost });
