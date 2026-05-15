// screens-arbitrage.jsx — Arbitration screen + organizer consolidated status

// ────────────────────────────────────────────────────────────
// Arbitration screen — choose which meeting to attend
// ────────────────────────────────────────────────────────────
function ArbitrationScreen({ theme, nav }) {
  const arb = PENDING_ARBITRATIONS[0];
  const [choice, setChoice] = React.useState(null); // 'A' | 'B' | 'later'

  const renderCard = (m, key, label) => {
    const c = commissionById(m.commission);
    const endMin = parseInt(m.time.split(':')[0]) * 60 + parseInt(m.time.split(':')[1]) + m.duration;
    const endStr = `${String(Math.floor(endMin/60)).padStart(2,'0')}:${String(endMin%60).padStart(2,'0')}`;
    const sel = choice === key;
    return (
      <button onClick={() => setChoice(key)} style={{
        width: '100%', textAlign: 'left', cursor: 'pointer',
        background: sel ? theme.surface : theme.surface,
        border: `2px solid ${sel ? theme.primary : theme.outline}`,
        borderRadius: R.lg, padding: '18px 18px',
        fontFamily: theme.font, position: 'relative', overflow: 'hidden',
      }}>
        {sel && (
          <div style={{
            position: 'absolute', top: 12, right: 12,
            width: 28, height: 28, borderRadius: '50%',
            background: theme.primary, color: theme.onPrimary,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Icon.Check size={16} stroke={theme.onPrimary} strokeWidth={3}/>
          </div>
        )}
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
          <div style={{
            padding: '3px 10px', borderRadius: R.pill,
            background: c.color, color: c.accent,
            fontSize: 12, fontWeight: 600,
          }}>{c.emoji} {c.name}</div>
        </div>
        <div style={{
          fontSize: 18, fontWeight: 700, color: theme.onSurface,
          letterSpacing: '-0.2px', marginBottom: 12, lineHeight: '24px',
        }}>{m.title}</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: theme.onSurfaceVariant, marginBottom: 8 }}>
          <Icon.Clock size={16} stroke={theme.onSurfaceVariant}/>
          <div style={{ fontSize: 14, fontWeight: 500, fontVariantNumeric: 'tabular-nums' }}>{m.time} – {endStr}</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, color: theme.onSurfaceVariant }}>
          <Icon.User size={16} stroke={theme.onSurfaceVariant}/>
          <div style={{ fontSize: 13 }}>Organisé par <b style={{ color: theme.onSurface }}>{m.organizer}</b> · {m.membersGoing} membres convoqués</div>
        </div>
      </button>
    );
  };

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        leading={<IconButton theme={theme} icon={<Icon.X size={24}/>} onClick={() => nav('home')}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 100px' }}>
        <div style={{
          fontFamily: theme.font, fontSize: 12, fontWeight: 700,
          color: theme.error, textTransform: 'uppercase', letterSpacing: '0.6px',
          marginBottom: 8,
        }}>Conflit · Jeudi 21 mai à 20h00</div>
        <h1 style={{
          margin: '0 0 8px', fontFamily: theme.font, fontSize: 26, fontWeight: 700,
          letterSpacing: '-0.3px', color: theme.onSurface, lineHeight: '32px',
        }}>Tu ne peux pas être<br/>aux deux. Où vas-tu ?</h1>
        <p style={{
          margin: '0 0 22px', fontFamily: theme.font, fontSize: 14, lineHeight: '20px',
          color: theme.onSurfaceVariant,
        }}>{arb.note}</p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {renderCard(arb.meetingA, 'A', 'Option A')}
          <div style={{
            display: 'flex', alignItems: 'center', gap: 12,
            color: theme.onSurfaceMuted, fontFamily: theme.font, fontSize: 12, fontWeight: 700,
            textTransform: 'uppercase', letterSpacing: '0.6px',
          }}>
            <div style={{ flex: 1, height: 1, background: theme.divider }}/>
            <span>OU</span>
            <div style={{ flex: 1, height: 1, background: theme.divider }}/>
          </div>
          {renderCard(arb.meetingB, 'B', 'Option B')}
        </div>

        <button onClick={() => setChoice('later')} style={{
          width: '100%', marginTop: 14,
          background: choice === 'later' ? theme.surfaceContainerHigh : 'transparent',
          border: `1.5px solid ${choice === 'later' ? 'transparent' : theme.outline}`,
          borderRadius: R.md, padding: '14px 16px',
          fontFamily: theme.font, fontSize: 15, fontWeight: 500,
          color: theme.onSurfaceVariant, cursor: 'pointer',
          textAlign: 'center',
        }}>Je trancherai plus tard</button>
      </div>

      <div style={{
        padding: '12px 20px 16px', background: theme.background,
        borderTop: `1px solid ${theme.divider}`,
      }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth disabled={!choice}
          onClick={() => nav('home')}>
          {choice === 'A' ? 'Je vais à Tournoi inter-quartiers'
            : choice === 'B' ? 'Je vais à Budget jeunesse'
            : choice === 'later' ? 'Reporter ma décision'
            : 'Choisis une option'}
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Organizer status — consolidated view of arbitrages on my meetings
// ────────────────────────────────────────────────────────────
function OrganizerStatusScreen({ theme, nav }) {
  // mock: Marc's view of "Tournoi inter-quartiers" he created
  const stats = {
    title: 'Tournoi inter-quartiers',
    date: 'Jeudi 21 mai · 20h00',
    invited: 9,
    confirmed: 5,
    arbitrating: 1,
    elsewhere: 2,
    pending: 1,
  };

  const decisions = [
    { name: 'Sophie Picquet', state: 'pending', detail: 'En attente' },
    { name: 'Pierre Garnier', state: 'me', detail: 'Va à Sport (toi)' },
    { name: 'Léa Dubois', state: 'other', detail: 'Va à Conseil plénier' },
    { name: 'Olivier Caron', state: 'me', detail: 'Va à Sport (toi)' },
    { name: 'Vincent Aubry', state: 'me', detail: 'Va à Sport (toi)' },
    { name: 'Karine Joly', state: 'other', detail: 'Va à Travaux' },
  ];

  const stateColor = (s) => ({
    me: { bg: theme.successContainer, fg: theme.onPrimaryContainer, dot: theme.primary, label: 'À ta réunion' },
    other: { bg: theme.warningContainer, fg: theme.onWarningContainer, dot: theme.warning, label: 'Ailleurs' },
    pending: { bg: theme.surfaceContainer, fg: theme.onSurfaceVariant, dot: theme.onSurfaceMuted, label: 'En attente' },
  })[s];

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} title="Suivi des arbitrages"
        leading={<IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('home')}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 24px' }}>
        <Card theme={theme} variant="filled" style={{ marginBottom: 18 }}>
          <div style={{ fontSize: 12, color: theme.onSurfaceVariant, fontFamily: theme.font, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{stats.date}</div>
          <div style={{ fontSize: 20, fontWeight: 700, color: theme.onSurface, fontFamily: theme.font, marginTop: 4, letterSpacing: '-0.2px' }}>{stats.title}</div>
        </Card>

        {/* Progress bar */}
        <div style={{
          padding: 18, borderRadius: R.lg, background: theme.surface,
          border: `1px solid ${theme.outlineSubtle}`, marginBottom: 18, fontFamily: theme.font,
        }}>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 12 }}>
            <div style={{ fontSize: 36, fontWeight: 700, color: theme.onSurface, letterSpacing: '-0.5px' }}>{stats.confirmed}</div>
            <div style={{ fontSize: 18, color: theme.onSurfaceVariant }}>/ {stats.invited} confirmés</div>
          </div>
          <div style={{
            height: 10, borderRadius: 5, background: theme.surfaceContainer,
            display: 'flex', overflow: 'hidden', marginBottom: 12,
          }}>
            <div style={{ width: `${(stats.confirmed/stats.invited)*100}%`, background: theme.primary }}/>
            <div style={{ width: `${(stats.elsewhere/stats.invited)*100}%`, background: theme.warning }}/>
            <div style={{ width: `${(stats.pending/stats.invited)*100}%`, background: theme.outline }}/>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 14, fontSize: 13 }}>
            <Legend theme={theme} dot={theme.primary} label={`${stats.confirmed} viennent`}/>
            <Legend theme={theme} dot={theme.warning} label={`${stats.elsewhere} ailleurs`}/>
            <Legend theme={theme} dot={theme.outline} label={`${stats.pending} en attente`}/>
          </div>
        </div>

        <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Détail par membre</SectionLabel>
        <Card theme={theme} variant="outlined" padding={0}>
          {decisions.map((d, i) => {
            const s = stateColor(d.state);
            return (
              <div key={d.name} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '12px 16px',
                borderBottom: i < decisions.length - 1 ? `1px solid ${theme.divider}` : 'none',
                fontFamily: theme.font,
              }}>
                <Avatar name={d.name} size={36} theme={theme}/>
                <div style={{ flex: 1, fontSize: 15, fontWeight: 500, color: theme.onSurface }}>{d.name}</div>
                <div style={{
                  padding: '4px 10px', borderRadius: R.pill,
                  background: s.bg, color: s.fg,
                  fontSize: 12, fontWeight: 600,
                  display: 'inline-flex', alignItems: 'center', gap: 6,
                }}>
                  <div style={{ width: 6, height: 6, borderRadius: 3, background: s.dot }}/>
                  {s.label}
                </div>
              </div>
            );
          })}
        </Card>
      </div>
    </div>
  );
}

function Legend({ theme, dot, label }) {
  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: theme.onSurfaceVariant, fontFamily: theme.font }}>
      <div style={{ width: 8, height: 8, borderRadius: 4, background: dot }}/>
      {label}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Members list
// ────────────────────────────────────────────────────────────
function MembersScreen({ theme, nav }) {
  const [query, setQuery] = React.useState('');
  const filtered = MEMBERS.filter(m => m.name.toLowerCase().includes(query.toLowerCase()));

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} title="Membres" large
        leading={null}
        trailing={<IconButton theme={theme} icon={<Icon.Plus size={22}/>}/>}
      />
      <div style={{ padding: '0 20px 12px' }}>
        <div style={{
          height: 44, padding: '0 14px', display: 'flex', alignItems: 'center', gap: 10,
          background: theme.surfaceContainer, borderRadius: R.pill,
        }}>
          <Icon.Search size={18} stroke={theme.onSurfaceVariant}/>
          <input value={query} onChange={(e) => setQuery(e.target.value)}
            placeholder="Rechercher un membre"
            style={{
              flex: 1, height: 24, border: 'none', outline: 'none',
              background: 'transparent', color: theme.onSurface,
              fontFamily: theme.font, fontSize: 14,
            }}/>
        </div>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 88px' }}>
        <SectionLabel theme={theme} style={{ marginTop: 4, marginBottom: 10 }}>{filtered.length} membres</SectionLabel>
        <Card theme={theme} variant="outlined" padding={0}>
          {filtered.map((m, i) => (
            <div key={m.id} style={{
              display: 'flex', alignItems: 'center', gap: 14, padding: '12px 16px',
              borderBottom: i < filtered.length - 1 ? `1px solid ${theme.divider}` : 'none',
              fontFamily: theme.font,
            }}>
              <Avatar name={m.name} size={40} theme={theme}/>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 15, fontWeight: 500, color: theme.onSurface }}>
                  {m.name}{m.me ? ' · toi' : ''}
                </div>
                <div style={{ fontSize: 12, color: theme.onSurfaceVariant, marginTop: 2 }}>
                  {m.commissions.length} commission{m.commissions.length > 1 ? 's' : ''}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 2 }}>
                {m.commissions.slice(0, 3).map(cid => {
                  const c = commissionById(cid);
                  return <div key={cid} style={{
                    width: 7, height: 7, borderRadius: 4, background: c.accent,
                  }}/>;
                })}
              </div>
            </div>
          ))}
        </Card>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Project settings
// ────────────────────────────────────────────────────────────
function ProjectScreen({ theme, nav }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} title="Projet" large
        leading={null}
        trailing={<IconButton theme={theme} icon={<Icon.Settings size={22}/>}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 88px' }}>
        <Card theme={theme} variant="filled" padding={20} style={{ marginBottom: 18 }}>
          <div style={{ fontFamily: theme.font, fontSize: 12, color: theme.onSurfaceVariant, fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.5px' }}>
            Tu fais partie de
          </div>
          <div style={{ fontFamily: theme.font, fontSize: 20, fontWeight: 700, color: theme.onSurface, marginTop: 6, letterSpacing: '-0.2px' }}>
            {PROJECT.name}
          </div>
          <div style={{ fontFamily: theme.font, fontSize: 13, color: theme.onSurfaceVariant, marginTop: 4 }}>
            Créé par {PROJECT.createdBy} · {PROJECT.membersCount} membres
          </div>
        </Card>

        <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Inviter</SectionLabel>
        <Card theme={theme} variant="outlined" padding={20} style={{ marginBottom: 24 }}>
          <div style={{ fontFamily: theme.font, fontSize: 13, color: theme.onSurfaceVariant, marginBottom: 8 }}>
            Code à partager
          </div>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 12,
            padding: '14px 18px', borderRadius: R.md,
            background: theme.surfaceContainer,
          }}>
            <div style={{
              flex: 1, fontFamily: theme.fontMono, fontSize: 28, fontWeight: 700,
              color: theme.onSurface, letterSpacing: '0.3em',
            }}>{PROJECT.code}</div>
            <button style={{
              padding: '8px 14px', height: 40, borderRadius: R.pill,
              background: theme.onSurface, color: theme.background,
              border: 'none', cursor: 'pointer',
              fontFamily: theme.font, fontSize: 13, fontWeight: 600,
              display: 'inline-flex', alignItems: 'center', gap: 6,
            }}>
              <Icon.Copy size={14} stroke={theme.background}/>Copier
            </button>
          </div>
          <Button theme={theme} variant="outlined" size="md" fullWidth
            icon={<Icon.Share size={18}/>} style={{ marginTop: 12 }}>
            Partager le code
          </Button>
        </Card>

        <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Mes raccourcis</SectionLabel>
        <Card theme={theme} variant="outlined" padding={0} style={{ marginBottom: 24 }}>
          <ListRow theme={theme} divider
            leading={<Icon.Calendar size={22} stroke={theme.onSurfaceVariant}/>}
            headline="Mes arbitrages en cours"
            supporting="1 conflit à trancher"
            trailing={<Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>}
            onClick={() => nav('arbitration')}/>
          <ListRow theme={theme} divider
            leading={<Icon.Users size={22} stroke={theme.onSurfaceVariant}/>}
            headline="Réunions que j'organise"
            supporting="Voir le suivi des présences"
            trailing={<Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>}
            onClick={() => nav('organizer-status')}/>
          <ListRow theme={theme}
            leading={<Icon.Bell size={22} stroke={theme.onSurfaceVariant}/>}
            headline="Notifications"
            supporting="Activées"
            trailing={<Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>}/>
        </Card>

        <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Zone sensible</SectionLabel>
        <Card theme={theme} variant="outlined" padding={0}>
          <ListRow theme={theme} divider
            leading={<Icon.LogOut size={22} stroke={theme.error}/>}
            headline={<span style={{ color: theme.error }}>Quitter le projet</span>}
            supporting="Tu pourras revenir avec le code"
            trailing={<Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>}/>
          <ListRow theme={theme}
            leading={<Icon.Trash size={22} stroke={theme.error}/>}
            headline={<span style={{ color: theme.error }}>Supprimer le projet</span>}
            supporting="Définitif pour tous les membres"
            trailing={<Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>}/>
        </Card>
      </div>
    </div>
  );
}

Object.assign(window, {
  ArbitrationScreen, OrganizerStatusScreen, MembersScreen, ProjectScreen,
});
