// host.jsx — Page-level host: two device frames synced + chrome + tweaks

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "palette": "forest",
  "mode": "light",
  "density": "cozy",
  "cardStyle": "outlined",
  "showAndroid": true,
  "showIOS": true
}/*EDITMODE-END*/;

const ALL_SCREENS = [
  { group: 'Onboarding', items: [
    { k: 'welcome', label: '1 · Accueil' },
    { k: 'join-code', label: '2 · Code projet' },
    { k: 'join-confirm', label: '3 · Confirmer projet' },
    { k: 'join-identity', label: "4 · Choisir l'identité" },
    { k: 'join-commissions', label: '5 · Commissions' },
    { k: 'notification-perm', label: '6 · Notifications' },
  ]},
  { group: 'Quotidien', items: [
    { k: 'home', label: 'Agenda (semaine)' },
    { k: 'commissions', label: 'Liste commissions' },
    { k: 'commission-detail', label: 'Détail commission' },
    { k: 'members', label: 'Membres' },
    { k: 'project', label: 'Projet + invitation' },
  ]},
  { group: 'Création + conflit', items: [
    { k: 'create-meeting', label: '1 · Nouvelle réunion' },
    { k: 'conflicts', label: '2 · Conflits détectés' },
    { k: 'suggestions', label: '3 · Créneaux libres' },
    { k: 'meeting-detail', label: 'Détail réunion' },
  ]},
  { group: 'Arbitrage', items: [
    { k: 'arbitration', label: "Écran d'arbitrage" },
    { k: 'organizer-status', label: 'Suivi organisateur' },
  ]},
];

function Host() {
  const [tweaks, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const theme = React.useMemo(() => buildTheme(tweaks), [tweaks.palette, tweaks.mode, tweaks.density, tweaks.cardStyle]);

  const [state, setState] = React.useState({
    screen: 'home',
    params: {},
    agendaView: 'semaine',
    showPush: false,
  });

  // Trigger a fake push notification for the demo
  const sendFakePush = () => {
    setState(s => ({ ...s, showPush: true }));
    setTimeout(() => setState(s => ({ ...s, showPush: false })), 6000);
  };

  return (
    <div style={{ minHeight: '100vh', background: '#0E1310', color: '#F0EBE0' }}>
      <PageChrome theme={theme} state={state} setState={setState} sendFakePush={sendFakePush}/>

      <div style={{
        display: 'flex', justifyContent: 'center', alignItems: 'flex-start',
        gap: 60, padding: '40px 24px 80px', flexWrap: 'wrap',
      }}>
        {tweaks.showIOS && <FrameWithLabel kind="ios" theme={theme} state={state} setState={setState}/>}
        {tweaks.showAndroid && <FrameWithLabel kind="android" theme={theme} state={state} setState={setState}/>}
      </div>

      <ScreenPicker theme={theme} state={state} setState={setState}/>

      <TweaksPanel title="Tweaks">
        <TweakSection label="Couleurs">
          <TweakSelect label="Palette" value={tweaks.palette}
            options={['forest','coral','indigo','plum']}
            onChange={(v) => setTweak('palette', v)}/>
          <TweakColor label="Accent" value={PALETTES[tweaks.palette].primary}
            options={[PALETTES.forest.primary, PALETTES.coral.primary, PALETTES.indigo.primary, PALETTES.plum.primary]}
            onChange={(hex) => {
              const found = Object.entries(PALETTES).find(([_, p]) => p.primary === hex);
              if (found) setTweak('palette', found[0]);
            }}/>
          <TweakRadio label="Mode" value={tweaks.mode}
            options={['light','dark']}
            onChange={(v) => setTweak('mode', v)}/>
        </TweakSection>
        <TweakSection label="Composants">
          <TweakSelect label="Cartes" value={tweaks.cardStyle}
            options={['outlined','elevated','filled']}
            onChange={(v) => setTweak('cardStyle', v)}/>
        </TweakSection>
        <TweakSection label="Plateformes">
          <TweakToggle label="iOS visible" value={tweaks.showIOS} onChange={(v) => setTweak('showIOS', v)}/>
          <TweakToggle label="Android visible" value={tweaks.showAndroid} onChange={(v) => setTweak('showAndroid', v)}/>
        </TweakSection>
        <TweakSection label="Démo">
          <TweakButton label="Simuler un push" onClick={sendFakePush}/>
          <TweakButton label="Aller à l'arbitrage" onClick={() => setState(s => ({ ...s, screen: 'arbitration' }))}/>
          <TweakButton label="Recommencer l'onboarding" onClick={() => setState(s => ({ ...s, screen: 'welcome' }))}/>
        </TweakSection>
      </TweaksPanel>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Frame wrapper — iOS or Android, both rendering the same App
// ────────────────────────────────────────────────────────────
function FrameWithLabel({ kind, theme, state, setState }) {
  const label = kind === 'ios' ? 'iPhone 15 · iOS 17' : 'Pixel 8 · Android 14';
  const subLabel = 'Compose Multiplatform · UI partagée';
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14, flexShrink: 0 }}>
      <div style={{ textAlign: 'center', fontFamily: '"DM Sans", sans-serif' }}>
        <div style={{
          fontSize: 11, fontWeight: 700, color: '#80847E',
          textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: 4,
        }}>{kind === 'ios' ? 'Apple' : 'Google'}</div>
        <div style={{ fontSize: 16, fontWeight: 600, color: '#F0EBE0' }}>{label}</div>
        <div style={{ fontSize: 12, color: '#80847E', marginTop: 2 }}>{subLabel}</div>
      </div>
      {kind === 'ios' ? (
        <IOSDevice dark={theme.mode === 'dark'}>
          <App theme={theme} state={state} setState={setState} safeTop={58} safeBottom={22}/>
        </IOSDevice>
      ) : (
        <AndroidDevice dark={theme.mode === 'dark'}>
          <App theme={theme} state={state} setState={setState} safeTop={0} safeBottom={16}/>
        </AndroidDevice>
      )}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Page chrome — top header with project name + summary
// ────────────────────────────────────────────────────────────
function PageChrome({ theme, state, setState, sendFakePush }) {
  return (
    <div style={{
      padding: '32px 32px 8px',
      fontFamily: '"DM Sans", sans-serif',
      maxWidth: 1400, margin: '0 auto',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 44, height: 44, borderRadius: 12,
            background: theme.primary,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon.Logo size={28} color={theme.onPrimary}/>
          </div>
          <div>
            <div style={{ fontSize: 22, fontWeight: 700, color: '#F0EBE0', letterSpacing: '-0.3px' }}>Colision</div>
            <div style={{ fontSize: 13, color: '#80847E', marginTop: 1 }}>
              Hi-fi prototype · iOS + Android via Compose Multiplatform
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          <div style={{
            padding: '8px 12px', borderRadius: 999,
            background: '#1A1F1C', border: '1px solid #3A413D',
            fontSize: 12, color: '#B4B8B1', display: 'inline-flex', alignItems: 'center', gap: 8,
          }}>
            <div style={{ width: 6, height: 6, borderRadius: 3, background: theme.primary }}/>
            {state.screen.replace(/-/g, ' ')}
          </div>
          <button onClick={sendFakePush} style={{
            padding: '8px 14px', height: 36, borderRadius: 999,
            background: theme.primary, color: theme.onPrimary,
            border: 'none', cursor: 'pointer',
            fontFamily: '"DM Sans", sans-serif', fontSize: 13, fontWeight: 600,
            display: 'inline-flex', alignItems: 'center', gap: 6,
          }}>
            <Icon.BellAlert size={14} stroke={theme.onPrimary}/>Simuler un push
          </button>
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Screen picker — sticky bottom bar to jump anywhere
// ────────────────────────────────────────────────────────────
function ScreenPicker({ theme, state, setState }) {
  const [open, setOpen] = React.useState(false);
  return (
    <div style={{
      position: 'fixed', bottom: 16, left: '50%', transform: 'translateX(-50%)',
      zIndex: 1000, fontFamily: '"DM Sans", sans-serif',
    }}>
      {open && (
        <div style={{
          position: 'absolute', bottom: 56, left: '50%', transform: 'translateX(-50%)',
          width: 'min(720px, 92vw)', maxHeight: '70vh', overflow: 'auto',
          background: '#1A1F1C', border: '1px solid #3A413D',
          borderRadius: 20, padding: 16,
          boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
        }}>
          {ALL_SCREENS.map(group => (
            <div key={group.group} style={{ marginBottom: 16 }}>
              <div style={{
                fontSize: 11, fontWeight: 700, color: '#80847E',
                textTransform: 'uppercase', letterSpacing: '0.8px', marginBottom: 8,
                padding: '0 4px',
              }}>{group.group}</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {group.items.map(it => {
                  const active = state.screen === it.k;
                  return (
                    <button key={it.k} onClick={() => { setState(s => ({ ...s, screen: it.k, params: {}, showPush: false })); setOpen(false); }}
                      style={{
                        padding: '8px 12px', borderRadius: 999,
                        background: active ? theme.primary : '#222825',
                        color: active ? theme.onPrimary : '#F0EBE0',
                        border: 'none', cursor: 'pointer',
                        fontFamily: '"DM Sans", sans-serif', fontSize: 13, fontWeight: 500,
                      }}>{it.label}</button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      )}
      <button onClick={() => setOpen(!open)} style={{
        padding: '10px 18px', height: 44, borderRadius: 999,
        background: '#1A1F1C', color: '#F0EBE0',
        border: '1px solid #3A413D', cursor: 'pointer',
        fontFamily: '"DM Sans", sans-serif', fontSize: 14, fontWeight: 600,
        display: 'inline-flex', alignItems: 'center', gap: 8,
        boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
      }}>
        <Icon.Hash size={16} stroke="#F0EBE0"/>Aller à un écran
        <Icon.ChevronUp size={16} stroke="#F0EBE0" style={{ transform: open ? 'rotate(180deg)' : '', transition: 'transform 150ms' }}/>
      </button>
    </div>
  );
}

Object.assign(window, { Host });
