// screens-onboarding.jsx — Welcome + Join flow + Notification permission

// ────────────────────────────────────────────────────────────
// Welcome — first launch
// ────────────────────────────────────────────────────────────
function WelcomeScreen({ theme, nav }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background, padding: '32px 24px 32px' }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end' }}>
        {/* Hero illustration: two overlapping circles + a meeting glyph */}
        <div style={{
          height: 240, position: 'relative', marginBottom: 40,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <svg viewBox="0 0 320 240" width="100%" height="100%">
            <defs>
              <linearGradient id="g1" x1="0" x2="1" y1="0" y2="1">
                <stop offset="0" stopColor={theme.primaryContainer}/>
                <stop offset="1" stopColor={theme.primaryContainer} stopOpacity="0.4"/>
              </linearGradient>
            </defs>
            {/* Two big overlapping circles representing two commissions */}
            <circle cx="120" cy="120" r="86" fill="url(#g1)" opacity="0.95"/>
            <circle cx="210" cy="120" r="86" fill={theme.primary} opacity="0.18"/>
            <circle cx="120" cy="120" r="86" fill="none" stroke={theme.primary} strokeWidth="2"/>
            <circle cx="210" cy="120" r="86" fill="none" stroke={theme.primary} strokeWidth="2"/>

            {/* small dots = members in each commission */}
            {[[88,86],[100,140],[140,76],[78,116],[152,156]].map(([x,y],i) =>
              <circle key={`a${i}`} cx={x} cy={y} r="5" fill={theme.primary}/>
            )}
            {[[238,86],[248,140],[228,76],[252,116],[200,168]].map(([x,y],i) =>
              <circle key={`b${i}`} cx={x} cy={y} r="5" fill={theme.primary}/>
            )}
            {/* Member in conflict (in overlap) — highlighted */}
            <circle cx="165" cy="120" r="9" fill={theme.error}/>
            <circle cx="165" cy="120" r="14" fill="none" stroke={theme.error} strokeWidth="1.5" strokeOpacity="0.4"/>
            <circle cx="165" cy="120" r="20" fill="none" stroke={theme.error} strokeWidth="1.2" strokeOpacity="0.2"/>
          </svg>
        </div>

        <div style={{ marginBottom: 12 }}>
          <Icon.Logo size={36} color={theme.primary}/>
        </div>
        <h1 style={{
          margin: 0, fontFamily: theme.font, fontSize: 32, fontWeight: 700, lineHeight: '38px',
          letterSpacing: '-0.6px', color: theme.onSurfaceStrong,
        }}>Plus jamais deux<br/>réunions en même temps.</h1>
        <p style={{
          marginTop: 12, marginBottom: 0,
          fontFamily: theme.font, fontSize: 16, lineHeight: '24px',
          color: theme.onSurfaceVariant,
        }}>Colision détecte les conflits d'agenda entre vos commissions, en temps réel. Sans compte, sans inscription.</p>
      </div>

      <div style={{ marginTop: 32, display: 'flex', flexDirection: 'column', gap: 12 }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth onClick={() => nav('create-project')}>
          Créer un projet
        </Button>
        <Button theme={theme} variant="outlined" size="lg" fullWidth onClick={() => nav('join-code')}>
          Rejoindre un projet
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Join: enter code
// ────────────────────────────────────────────────────────────
function JoinCodeScreen({ theme, nav }) {
  const [code, setCode] = React.useState('');
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} leading={
        <IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('welcome')}/>
      }/>
      <div style={{ padding: '0 24px 24px', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <h1 style={{
          margin: '8px 0 8px', fontFamily: theme.font, fontSize: 28, fontWeight: 700,
          letterSpacing: '-0.4px', color: theme.onSurface, lineHeight: '34px',
        }}>Saisis le code<br/>de ton projet</h1>
        <p style={{
          margin: '0 0 28px', fontFamily: theme.font, fontSize: 15,
          color: theme.onSurfaceVariant, lineHeight: '22px',
        }}>Six lettres ou chiffres, partagés par la personne qui a créé le projet.</p>

        {/* OTP-style boxes */}
        <div style={{ display: 'flex', gap: 8, justifyContent: 'space-between', marginBottom: 20 }}>
          {Array.from({ length: 6 }).map((_, i) => {
            const ch = code[i] || '';
            const isCursor = code.length === i;
            return (
              <div key={i} style={{
                flex: 1, aspectRatio: '1/1.15',
                background: theme.surface,
                border: `1.75px solid ${ch ? theme.primary : isCursor ? theme.primary : theme.outline}`,
                borderRadius: 12,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: theme.fontMono, fontSize: 28, fontWeight: 700,
                color: theme.onSurface, position: 'relative',
              }}>
                {ch || (isCursor && <div style={{
                  width: 2, height: 24, background: theme.primary,
                  animation: 'blink 1.1s infinite',
                }}/>)}
              </div>
            );
          })}
        </div>

        {code.length === 6 && (
          <Banner theme={theme} tone="success" icon={<Icon.Check size={18}/>}>
            Code reconnu : <b>{PROJECT.name}</b>
          </Banner>
        )}

        <div style={{ flex: 1 }}/>

        <Button theme={theme} variant="primary" size="lg" fullWidth
          disabled={code.length !== 6}
          onClick={() => nav('join-confirm')}>
          Continuer
        </Button>

        {/* Fake numeric pad for the prototype to feel typed */}
        <FakeCodePad code={code} setCode={setCode} theme={theme}/>
      </div>
    </div>
  );
}

function FakeCodePad({ code, setCode, theme }) {
  const keys = ['K','Q','7','H','2','P','⌫'];
  // when user presses, we append until 6 chars; ⌫ removes
  const onKey = (k) => {
    if (k === '⌫') setCode(code.slice(0, -1));
    else if (code.length < 6) setCode(code + k);
  };
  return (
    <div style={{
      marginTop: 20, padding: '12px 0 0',
      borderTop: `1px dashed ${theme.outline}`,
    }}>
      <div style={{
        fontSize: 11, color: theme.onSurfaceMuted, fontFamily: theme.font,
        textTransform: 'uppercase', letterSpacing: '0.6px', marginBottom: 8,
      }}>Démo — tape pour remplir</div>
      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
        {keys.map(k => (
          <button key={k} onClick={() => onKey(k)} style={{
            flex: 1, minWidth: 36, height: 38,
            background: theme.surface,
            border: `1px solid ${theme.outline}`,
            borderRadius: 10,
            fontFamily: theme.font, fontSize: 14, fontWeight: 600,
            color: theme.onSurface, cursor: 'pointer',
          }}>{k}</button>
        ))}
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Join: confirm project found
// ────────────────────────────────────────────────────────────
function JoinConfirmScreen({ theme, nav }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} leading={
        <IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('join-code')}/>
      }/>
      <div style={{ padding: '8px 24px 24px', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <div style={{
          fontFamily: theme.font, fontSize: 13, color: theme.onSurfaceVariant,
          textTransform: 'uppercase', letterSpacing: '0.6px', fontWeight: 600,
        }}>Tu rejoins</div>
        <h1 style={{
          margin: '8px 0 8px', fontFamily: theme.font, fontSize: 28, fontWeight: 700,
          letterSpacing: '-0.4px', color: theme.onSurface, lineHeight: '34px',
        }}>{PROJECT.name}</h1>
        <p style={{
          margin: '0 0 24px', fontFamily: theme.font, fontSize: 15,
          color: theme.onSurfaceVariant, lineHeight: '22px',
        }}>Créé par {PROJECT.createdBy}. {PROJECT.membersCount} membres, {COMMISSIONS.length} commissions.</p>

        <Card theme={theme} variant="filled" padding={0} style={{ marginBottom: 16 }}>
          <div style={{ padding: '14px 16px', borderBottom: `1px solid ${theme.divider}` }}>
            <div style={{ fontSize: 12, color: theme.onSurfaceVariant, fontFamily: theme.font, marginBottom: 6 }}>Les commissions</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {COMMISSIONS.map(c => (
                <div key={c.id} style={{
                  padding: '4px 10px', borderRadius: R.pill,
                  background: c.color, color: c.accent,
                  fontFamily: theme.font, fontSize: 13, fontWeight: 500,
                }}>{c.name}</div>
              ))}
            </div>
          </div>
          <div style={{ padding: '14px 16px' }}>
            <div style={{ fontSize: 12, color: theme.onSurfaceVariant, fontFamily: theme.font, marginBottom: 8 }}>Quelques membres déjà inscrits</div>
            <div style={{ display: 'flex' }}>
              {MEMBERS.slice(0, 6).map((m, i) => (
                <Avatar key={m.id} name={m.name} size={32} theme={theme}
                  style={{ marginLeft: i === 0 ? 0 : -8, border: `2px solid ${theme.surfaceContainer}` }}/>
              ))}
              <div style={{
                width: 32, height: 32, borderRadius: '50%',
                background: theme.surface, color: theme.onSurfaceVariant,
                marginLeft: -8, border: `2px solid ${theme.surfaceContainer}`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: theme.font, fontSize: 12, fontWeight: 600,
              }}>+{PROJECT.membersCount - 6}</div>
            </div>
          </div>
        </Card>

        <div style={{ flex: 1 }}/>

        <Button theme={theme} variant="primary" size="lg" fullWidth onClick={() => nav('join-identity')}>
          C'est bien mon projet
        </Button>
        <Button theme={theme} variant="text" size="md" fullWidth onClick={() => nav('join-code')}
          style={{ marginTop: 8 }}>
          Ce n'est pas le bon projet
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Join: select identity
// ────────────────────────────────────────────────────────────
function JoinIdentityScreen({ theme, nav }) {
  const [selected, setSelected] = React.useState('m2'); // Sophie
  const [query, setQuery] = React.useState('');
  const filtered = MEMBERS.filter(m => m.name.toLowerCase().includes(query.toLowerCase()));

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} leading={
        <IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('join-confirm')}/>
      }/>
      <div style={{ padding: '0 24px 16px' }}>
        <h1 style={{
          margin: '8px 0 8px', fontFamily: theme.font, fontSize: 28, fontWeight: 700,
          letterSpacing: '-0.4px', color: theme.onSurface, lineHeight: '34px',
        }}>Qui es-tu ?</h1>
        <p style={{
          margin: '0 0 20px', fontFamily: theme.font, fontSize: 15,
          color: theme.onSurfaceVariant, lineHeight: '22px',
        }}>Choisis ton nom dans la liste. Tu n'y es pas ? Ajoute-toi en bas.</p>

        <div style={{
          height: 48, padding: '0 14px', display: 'flex', alignItems: 'center', gap: 10,
          background: theme.surfaceContainer, borderRadius: R.pill,
        }}>
          <Icon.Search size={18} stroke={theme.onSurfaceVariant}/>
          <input value={query} onChange={(e) => setQuery(e.target.value)}
            placeholder="Rechercher ton nom"
            style={{
              flex: 1, height: 24, border: 'none', outline: 'none',
              background: 'transparent', color: theme.onSurface,
              fontFamily: theme.font, fontSize: 15,
            }}/>
        </div>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px' }}>
        {filtered.map(m => {
          const isSelected = m.id === selected;
          return (
            <button key={m.id} onClick={() => setSelected(m.id)} style={{
              width: '100%', display: 'flex', alignItems: 'center', gap: 14,
              padding: '12px 12px', minHeight: 60,
              background: isSelected ? theme.primaryContainer : 'transparent',
              border: 'none', borderRadius: 14, cursor: 'pointer',
              marginBottom: 2, textAlign: 'left',
            }}>
              <Avatar name={m.name} size={44} theme={theme}/>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{
                  fontFamily: theme.font, fontSize: 16, fontWeight: 500,
                  color: isSelected ? theme.onPrimaryContainer : theme.onSurface,
                }}>{m.name}</div>
                <div style={{
                  fontSize: 13, color: isSelected ? theme.onPrimaryContainer : theme.onSurfaceVariant,
                  fontFamily: theme.font, marginTop: 2, opacity: isSelected ? 0.8 : 1,
                }}>
                  {m.commissions.map(c => commissionById(c).name).join(' · ')}
                </div>
              </div>
              {isSelected && (
                <div style={{
                  width: 28, height: 28, borderRadius: '50%',
                  background: theme.primary, color: theme.onPrimary,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  <Icon.Check size={16} stroke={theme.onPrimary} strokeWidth={2.5}/>
                </div>
              )}
            </button>
          );
        })}

        <button style={{
          width: '100%', display: 'flex', alignItems: 'center', gap: 14,
          padding: '12px 12px', minHeight: 60,
          background: 'transparent', border: `1.5px dashed ${theme.outline}`,
          borderRadius: 14, cursor: 'pointer', marginTop: 8, marginBottom: 16,
          textAlign: 'left',
        }}>
          <div style={{
            width: 44, height: 44, borderRadius: '50%',
            background: theme.surfaceContainer, color: theme.onSurfaceVariant,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}><Icon.Plus size={22}/></div>
          <div style={{
            fontFamily: theme.font, fontSize: 16, fontWeight: 500,
            color: theme.onSurface,
          }}>Je m'ajoute moi-même</div>
        </button>
      </div>

      <div style={{
        padding: '12px 24px 16px',
        background: theme.background,
        borderTop: `1px solid ${theme.divider}`,
      }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth onClick={() => nav('join-commissions')}>
          C'est moi
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Join: confirm commissions
// ────────────────────────────────────────────────────────────
function JoinCommissionsScreen({ theme, nav }) {
  const [picked, setPicked] = React.useState(['c1','c5']); // pre-checked by Antoine
  const toggle = (id) => setPicked(p => p.includes(id) ? p.filter(x => x !== id) : [...p, id]);

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} leading={
        <IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('join-identity')}/>
      }/>

      <div style={{ flex: 1, overflow: 'auto' }}>
        <div style={{ padding: '0 24px 8px' }}>
          <h1 style={{
            margin: '8px 0 8px', fontFamily: theme.font, fontSize: 28, fontWeight: 700,
            letterSpacing: '-0.4px', color: theme.onSurface, lineHeight: '34px',
          }}>Tes commissions</h1>
          <p style={{
            margin: '0 0 16px', fontFamily: theme.font, fontSize: 15,
            color: theme.onSurfaceVariant, lineHeight: '22px',
          }}>Coche celles dont tu es membre. Tu pourras les modifier plus tard.</p>

          <Banner theme={theme} tone="info" dense icon={<Icon.Info size={18}/>}>
            Antoine a pré-coché deux commissions pour toi.
          </Banner>
        </div>

        <div style={{ padding: '16px 16px 16px' }}>
          {COMMISSIONS.map(c => {
            const sel = picked.includes(c.id);
            return (
              <button key={c.id} onClick={() => toggle(c.id)} style={{
                width: '100%', display: 'flex', alignItems: 'center', gap: 14,
                padding: '12px 14px', minHeight: 64,
                background: sel ? theme.surface : 'transparent',
                border: `1.5px solid ${sel ? theme.primary : theme.outline}`,
                borderRadius: 14, cursor: 'pointer',
                marginBottom: 8, textAlign: 'left',
              }}>
                <div style={{
                  width: 40, height: 40, borderRadius: 12,
                  background: c.color, display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 20,
                }}>{c.emoji}</div>
                <div style={{ flex: 1 }}>
                  <div style={{
                    fontFamily: theme.font, fontSize: 16, fontWeight: 600,
                    color: theme.onSurface,
                  }}>{c.name}</div>
                  <div style={{
                    fontSize: 13, color: theme.onSurfaceVariant,
                    fontFamily: theme.font, marginTop: 2,
                  }}>{MEMBERS.filter(m => m.commissions.includes(c.id)).length} membres</div>
                </div>
                <div style={{
                  width: 24, height: 24, borderRadius: 6,
                  background: sel ? theme.primary : 'transparent',
                  border: `2px solid ${sel ? theme.primary : theme.outline}`,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  {sel && <Icon.Check size={14} stroke={theme.onPrimary} strokeWidth={3}/>}
                </div>
              </button>
            );
          })}
        </div>
      </div>

      <div style={{
        padding: '12px 24px 16px',
        background: theme.background,
        borderTop: `1px solid ${theme.divider}`,
      }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth onClick={() => nav('notification-perm')}>
          Continuer ({picked.length} cochée{picked.length > 1 ? 's' : ''})
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Notification permission — explain before native dialog
// ────────────────────────────────────────────────────────────
function NotificationPermScreen({ theme, nav }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background, padding: '40px 24px 24px' }}>
      <div style={{ flex: 1 }}>
        <div style={{
          width: 88, height: 88, borderRadius: 22,
          background: theme.primaryContainer, color: theme.primary,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          marginBottom: 24,
        }}>
          <Icon.BellAlert size={44} stroke={theme.primary}/>
        </div>
        <h1 style={{
          margin: '0 0 12px', fontFamily: theme.font, fontSize: 28, fontWeight: 700,
          letterSpacing: '-0.4px', color: theme.onSurface, lineHeight: '34px',
        }}>Ne rate plus<br/>une coordination.</h1>
        <p style={{
          margin: '0 0 28px', fontFamily: theme.font, fontSize: 16, lineHeight: '24px',
          color: theme.onSurfaceVariant,
        }}>Colision t'envoie une notification quand une réunion crée un conflit avec ton agenda — pour que tu puisses trancher sans débat WhatsApp.</p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {[
            { i: <Icon.AlertTriangle size={20}/>, t: 'Conflit détecté', s: 'Quand quelqu\'un programme une réunion qui chevauche la tienne' },
            { i: <Icon.Calendar size={20}/>, t: 'Nouvelle réunion', s: 'Pour chaque réunion d\'une commission dont tu es membre' },
            { i: <Icon.Users size={20}/>, t: 'Arbitrages', s: 'Quand un autre membre choisit où il va' },
          ].map((row, i) => (
            <div key={i} style={{ display: 'flex', gap: 14, alignItems: 'flex-start' }}>
              <div style={{
                width: 40, height: 40, borderRadius: 12,
                background: theme.surfaceContainer, color: theme.primary,
                display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
              }}>{row.i}</div>
              <div>
                <div style={{ fontFamily: theme.font, fontSize: 15, fontWeight: 600, color: theme.onSurface }}>{row.t}</div>
                <div style={{ fontFamily: theme.font, fontSize: 13, color: theme.onSurfaceVariant, marginTop: 2, lineHeight: '18px' }}>{row.s}</div>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth onClick={() => nav('home')}>
          Activer les notifications
        </Button>
        <Button theme={theme} variant="text" size="md" fullWidth onClick={() => nav('home')}>
          Plus tard
        </Button>
      </div>
    </div>
  );
}

Object.assign(window, {
  WelcomeScreen, JoinCodeScreen, JoinConfirmScreen,
  JoinIdentityScreen, JoinCommissionsScreen, NotificationPermScreen,
});
