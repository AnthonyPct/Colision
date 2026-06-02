// Colision — main app orchestrator. Renders two synced frames (iOS + Android) showing
// the same Compose-style UI. State is lifted to <ColisionStage> so both views step
// together when the user clicks either one.

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accent": "teal",
  "dark": false,
  "density": "confortable",
  "showPush": false
}/*EDITMODE-END*/;

// ── App router — pure renderer ────────────────────────────────────────────
function ColisionApp({ tokens: t, state, set, nav, screen }) {
  switch (screen) {
    case 'welcome':          return <ScrWelcome t={t} nav={nav} />;
    case 'create-project':   return <ScrCreateProject t={t} nav={nav} state={state} set={set} />;
    case 'project-created':  return <ScrProjectCreated t={t} nav={nav} state={state} />;
    case 'join-code':        return <ScrJoinCode t={t} nav={nav} state={state} set={set} />;
    case 'confirm-project':  return <ScrConfirmProject t={t} nav={nav} />;
    case 'select-identity':  return <ScrSelectIdentity t={t} nav={nav} state={state} set={set} />;
    case 'pick-commissions': return <ScrPickCommissions t={t} nav={nav} state={state} set={set} />;
    case 'agenda':           return <ScrAgenda t={t} nav={nav} state={state} set={set} />;
    case 'commissions':      return <ScrCommissions t={t} nav={nav} state={state} set={set} />;
    case 'commission-detail':return <ScrCommissionDetail t={t} nav={nav} state={state} />;
    case 'create-meeting':   return <ScrCreateMeeting t={t} nav={nav} state={state} set={set} />;
    case 'conflicts':        return <ScrConflicts t={t} nav={nav} state={state} />;
    case 'suggestions':      return <ScrSuggestions t={t} nav={nav} state={state} set={set} />;
    case 'meeting-created':  return <ScrMeetingCreated t={t} nav={nav} state={state} />;
    case 'arbitration':      return <ScrArbitration t={t} nav={nav} state={state} set={set} />;
    case 'more':             return <ScrMore t={t} nav={nav} state={state} set={set} />;
    case 'members':          return <ScrMembers t={t} nav={nav} state={state} />;
    case 'settings':         return <ScrMore t={t} nav={nav} state={state} set={set} />;
    default:                 return <ScrWelcome t={t} nav={nav} />;
  }
}

// ── Inner phone surface: scales the app UI to the device viewport ────────
function PhoneSurface({ tokens, state, set, nav, screen, platform, showPush, dismissPush }) {
  return (
    <div style={{
      position: 'absolute', inset: 0, overflow: 'hidden',
      display: 'flex', flexDirection: 'column',
      background: tokens.bg,
    }}>
      <ColisionApp tokens={tokens} state={state} set={set} nav={nav} screen={screen} />
      {showPush && (
        <PushNotification t={tokens} platform={platform}
          onTap={() => {
            dismissPush();
            nav.go('arbitration');
          }} />
      )}
    </div>
  );
}

// ── Stage — orchestrates state + lays out the two frames ─────────────────
function ColisionStage() {
  const { tweaks, setTweak, TweaksPanel, TweakSection, TweakRadio, TweakToggle, TweakSelect, TweakColor, TweakButton } = window;
  const [t, setT] = window.useTweaks(TWEAK_DEFAULTS);
  const tokens = React.useMemo(() => window.COLISION_TOKENS.build({
    accent: t.accent, dark: t.dark, density: t.density,
  }), [t.accent, t.dark, t.density]);

  // Shared app state (controls both phones in sync)
  const [screen, setScreen] = React.useState('welcome');
  const [appState, setAppState] = React.useState({
    selfMemberId: 'm2',           // Sophie
    selfCommissions: ['c1', 'c2', 'c5'],
    pendingArbitration: false,
    pickedSlot: undefined,
    arbitrationChoice: undefined,
    tab: 'agenda',
    draftProject: 'Conseil municipal de Saint-Machin',
    meetingDraft: undefined,
    openCommission: 'c2',
    joinCode: '',
  });
  const setApp = React.useCallback((patch) => {
    setAppState(prev => ({ ...prev, ...patch }));
  }, []);

  const nav = React.useMemo(() => ({
    go: (s) => setScreen(s),
    screen,
  }), [screen]);

  // Demo push notification toggle
  const triggerPush = () => {
    setApp({ pendingArbitration: true });
    setTimeout(() => setT({ showPush: false }), 5000); // auto-dismiss after 5s
  };

  // Outer page background subtle pattern
  const pageBg = t.dark ? '#0A100E' : '#EEE7D5';

  return (
    <div style={{
      minHeight: '100vh', width: '100%',
      background: pageBg, color: tokens.ink,
      fontFamily: '"Geist", system-ui, sans-serif',
      padding: '32px 24px 80px',
      display: 'flex', flexDirection: 'column', alignItems: 'center',
      WebkitFontSmoothing: 'antialiased',
    }}>
      {/* Hero strip with brand, screen label, jump shortcuts */}
      <StageHeader t={t} tokens={tokens} screen={screen} setScreen={setScreen} setApp={setApp} setT={setT} />

      {/* The two synced devices */}
      <div style={{
        display: 'flex', gap: 36, alignItems: 'flex-start', marginTop: 24,
        flexWrap: 'wrap', justifyContent: 'center',
      }}>
        <DeviceLabel label="iPhone · iOS 17" t={t} accent={tokens.accent}>
          <IOSDevice width={390} height={820} dark={t.dark}>
            <PhoneSurface
              tokens={tokens} state={appState} set={setApp} nav={nav} screen={screen}
              platform="ios" showPush={t.showPush}
              dismissPush={() => setT({ showPush: false })} />
          </IOSDevice>
        </DeviceLabel>

        <DeviceLabel label="Pixel · Android 14" t={t} accent={tokens.accent}>
          <AndroidDevice width={390} height={820} dark={t.dark}>
            <PhoneSurface
              tokens={tokens} state={appState} set={setApp} nav={nav} screen={screen}
              platform="android" showPush={t.showPush}
              dismissPush={() => setT({ showPush: false })} />
          </AndroidDevice>
        </DeviceLabel>
      </div>

      <StageFooter t={t} tokens={tokens} />

      <TweaksPanel title="Tweaks" defaultOpen={false} initialPosition="bottom-right">
        <TweakSection title="Brand">
          <TweakColor label="Accent"
            value={t.accent}
            onChange={v => setT('accent', v)}
            options={[
              { value: 'teal',   color: '#0E7C66' },
              { value: 'indigo', color: '#3F5BD9' },
              { value: 'rust',   color: '#C9582B' },
              { value: 'plum',   color: '#7B3FE4' },
            ]} />
        </TweakSection>

        <TweakSection title="Surface">
          <TweakToggle label="Mode sombre" value={t.dark} onChange={v => setT('dark', v)} />
          <TweakRadio label="Densité"
            value={t.density}
            onChange={v => setT('density', v)}
            options={[{ value: 'confortable', label: 'Confort' }, { value: 'compact', label: 'Compact' }]} />
        </TweakSection>

        <TweakSection title="Démo">
          <TweakButton label={t.showPush ? 'Masquer le push' : 'Simuler push « conflit »'}
            onClick={() => {
              if (t.showPush) {
                setT('showPush', false);
              } else {
                setApp({ pendingArbitration: true });
                setT('showPush', true);
              }
            }} />
          <TweakButton label="Restart onboarding"
            onClick={() => { setScreen('welcome'); setApp({ pendingArbitration: false, pickedSlot: undefined, joinCode: '' }); }} />
        </TweakSection>
      </TweaksPanel>
    </div>
  );
}

function DeviceLabel({ label, t, accent, children }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8,
        fontSize: 11, fontWeight: 600, color: t.dark ? '#A6AEA9' : '#5C6963',
        textTransform: 'uppercase', letterSpacing: 1,
      }}>
        <span style={{ width: 6, height: 6, borderRadius: '50%', background: accent }} />
        {label}
      </div>
      {children}
    </div>
  );
}

// ── Top of page: brand + screen pager ────────────────────────────────────
function StageHeader({ t, tokens, screen, setScreen, setApp, setT }) {
  const SCREENS = [
    { id: 'welcome',           label: '1. Bienvenue' },
    { id: 'create-project',    label: '2. Créer projet' },
    { id: 'project-created',   label: '3. Code généré' },
    { id: 'join-code',         label: '4. Rejoindre' },
    { id: 'confirm-project',   label: '5. Confirmer projet' },
    { id: 'select-identity',   label: '6. Qui es-tu' },
    { id: 'pick-commissions',  label: '7. Mes commissions' },
    { id: 'agenda',            label: '8. Agenda' },
    { id: 'commissions',       label: '9. Commissions' },
    { id: 'commission-detail', label: '10. Détail commission' },
    { id: 'create-meeting',    label: '11. Nouvelle réunion' },
    { id: 'conflicts',         label: '12. ⚠ Conflits' },
    { id: 'suggestions',       label: '13. ✨ Suggestions' },
    { id: 'meeting-created',   label: '14. Créée' },
    { id: 'arbitration',       label: '15. Arbitrage' },
    { id: 'more',              label: '16. Plus' },
    { id: 'members',           label: '17. Membres' },
  ];
  const idx = SCREENS.findIndex(s => s.id === screen);
  const inkPrimary = t.dark ? '#F0EBDC' : '#10221E';
  const inkMuted = t.dark ? '#A6AEA9' : '#5C6963';
  return (
    <div style={{ width: '100%', maxWidth: 900, display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
        <ColLogo t={tokens} size={42} />
        <div>
          <div style={{ fontSize: 11, color: inkMuted, fontWeight: 600, letterSpacing: 1.4, textTransform: 'uppercase' }}>Prototype interactif</div>
          <h1 style={{
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 36, fontWeight: 400, margin: 0, lineHeight: 1, letterSpacing: -0.4,
            color: inkPrimary,
          }}>Colision</h1>
        </div>
        <div style={{ flex: 1 }} />
        <div style={{
          fontSize: 12, color: inkMuted, padding: '6px 12px',
          background: t.dark ? '#19211E' : '#FFFFFF99', borderRadius: 999,
          border: `1px solid ${t.dark ? '#22302B' : '#E8E0CD'}`,
        }}>
          Compose Multiplatform · même UI sur les deux
        </div>
      </div>

      {/* Pager */}
      <div style={{
        display: 'flex', gap: 6, overflowX: 'auto', padding: '6px 2px',
        marginLeft: -2, marginRight: -2, scrollbarWidth: 'thin',
      }}>
        {SCREENS.map((s, i) => {
          const active = s.id === screen;
          return (
            <button key={s.id} onClick={() => setScreen(s.id)} style={{
              flexShrink: 0, padding: '7px 12px', borderRadius: 999,
              background: active ? inkPrimary : (t.dark ? '#19211E' : '#FFFFFF'),
              color: active ? (t.dark ? '#0F1714' : '#F5F1E8') : inkMuted,
              border: `1px solid ${active ? inkPrimary : (t.dark ? '#22302B' : '#E8E0CD')}`,
              fontFamily: 'inherit', fontSize: 13, fontWeight: 500, cursor: 'pointer',
              whiteSpace: 'nowrap',
            }}>{s.label}</button>
          );
        })}
      </div>

      {/* Prev / next */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <button onClick={() => setScreen(SCREENS[Math.max(0, idx - 1)].id)} disabled={idx <= 0} style={{
          padding: '8px 14px', borderRadius: 999, fontFamily: 'inherit',
          background: t.dark ? '#19211E' : '#FFFFFF', color: inkPrimary,
          border: `1px solid ${t.dark ? '#22302B' : '#E8E0CD'}`,
          cursor: idx > 0 ? 'pointer' : 'not-allowed', opacity: idx > 0 ? 1 : 0.4,
          display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 500,
        }}>← Précédent</button>
        <div style={{ fontSize: 13, color: inkMuted }}>
          Écran <strong style={{ color: inkPrimary }}>{idx + 1}</strong> / {SCREENS.length}
        </div>
        <button onClick={() => setScreen(SCREENS[Math.min(SCREENS.length - 1, idx + 1)].id)} disabled={idx >= SCREENS.length - 1} style={{
          padding: '8px 14px', borderRadius: 999, fontFamily: 'inherit',
          background: t.dark ? '#19211E' : '#FFFFFF', color: inkPrimary,
          border: `1px solid ${t.dark ? '#22302B' : '#E8E0CD'}`,
          cursor: idx < SCREENS.length - 1 ? 'pointer' : 'not-allowed', opacity: idx < SCREENS.length - 1 ? 1 : 0.4,
          display: 'inline-flex', alignItems: 'center', gap: 6, fontSize: 13, fontWeight: 500,
        }}>Suivant →</button>
      </div>
    </div>
  );
}

function StageFooter({ t, tokens }) {
  const inkMuted = t.dark ? '#A6AEA9' : '#5C6963';
  return (
    <div style={{
      marginTop: 48, maxWidth: 700, textAlign: 'center',
      fontSize: 13, color: inkMuted, lineHeight: 1.6,
    }}>
      Les deux téléphones sont synchronisés : cliquer dans l'un ou l'autre fait avancer les deux.
      C'est l'idée de Compose Multiplatform — une seule UI Kotlin, deux plateformes.
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<ColisionStage />);
