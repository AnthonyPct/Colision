// screens-meeting.jsx — Create meeting, conflict detection, suggestions, detail

// ────────────────────────────────────────────────────────────
// Create meeting — date, time, duration, commissions, title
// ────────────────────────────────────────────────────────────
function CreateMeetingScreen({ theme, nav }) {
  const [date, setDate] = React.useState('2026-05-21');
  const [time, setTime] = React.useState('20:00');
  const [duration, setDuration] = React.useState(90);
  const [commissions, setCommissions] = React.useState(['c2']);
  const [title, setTitle] = React.useState('Tournoi inter-quartiers');

  // Live conflict count — for the demo, when date=21/05 & time=20:00 & sport commission, 3 conflicts
  const hasConflict = date === '2026-05-21' && time === '20:00' && commissions.includes('c2');
  const conflictCount = hasConflict ? 3 : 0;

  const toggleCom = (id) => setCommissions(c => c.includes(id) ? c.filter(x => x !== id) : [...c, id]);

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        title="Nouvelle réunion"
        leading={<IconButton theme={theme} icon={<Icon.X size={24}/>} onClick={() => nav('home')}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 100px' }}>
        {/* Title */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel theme={theme} style={{ marginBottom: 8 }}>Titre (optionnel)</SectionLabel>
          <input value={title} onChange={(e) => setTitle(e.target.value)}
            placeholder="Ex. : Préparation du budget"
            style={{
              width: '100%', height: 52, padding: '0 16px', boxSizing: 'border-box',
              background: theme.surface, border: `1.5px solid ${theme.outline}`,
              borderRadius: R.md, outline: 'none',
              fontFamily: theme.font, fontSize: 16, color: theme.onSurface,
            }}/>
        </div>

        {/* Date */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel theme={theme} style={{ marginBottom: 8 }}>Date</SectionLabel>
          <DateStrip theme={theme} value={date} onChange={setDate}/>
        </div>

        {/* Time + Duration */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 20 }}>
          <div>
            <SectionLabel theme={theme} style={{ marginBottom: 8 }}>Début</SectionLabel>
            <div style={{
              height: 56, padding: '0 16px', background: theme.surface,
              border: `1.5px solid ${time === '20:00' && hasConflict ? theme.error : theme.outline}`,
              borderRadius: R.md, display: 'flex', alignItems: 'center', gap: 10,
            }}>
              <Icon.Clock size={20} stroke={theme.onSurfaceVariant}/>
              <div style={{ fontFamily: theme.font, fontSize: 18, fontWeight: 600, color: theme.onSurface, fontVariantNumeric: 'tabular-nums' }}>
                {time}
              </div>
            </div>
          </div>
          <div>
            <SectionLabel theme={theme} style={{ marginBottom: 8 }}>Durée</SectionLabel>
            <div style={{ display: 'flex', gap: 6 }}>
              {[60, 90, 120].map(d => (
                <button key={d} onClick={() => setDuration(d)} style={{
                  flex: 1, height: 56, borderRadius: R.md,
                  background: duration === d ? theme.primaryContainer : theme.surface,
                  color: duration === d ? theme.onPrimaryContainer : theme.onSurface,
                  border: `1.5px solid ${duration === d ? 'transparent' : theme.outline}`,
                  fontFamily: theme.font, fontSize: 14, fontWeight: 600,
                  cursor: 'pointer',
                }}>{d < 60 ? `${d}min` : `${d/60}h${d%60 ? '30' : ''}`}</button>
              ))}
            </div>
          </div>
        </div>

        {/* Commissions */}
        <div style={{ marginBottom: 20 }}>
          <SectionLabel theme={theme} style={{ marginBottom: 8 }}>Commission(s) concernée(s)</SectionLabel>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {COMMISSIONS.map(c => (
              <button key={c.id} onClick={() => toggleCom(c.id)} style={{
                padding: '8px 12px', borderRadius: R.pill, gap: 6,
                background: commissions.includes(c.id) ? c.color : 'transparent',
                color: commissions.includes(c.id) ? c.accent : theme.onSurfaceVariant,
                border: `1.5px solid ${commissions.includes(c.id) ? 'transparent' : theme.outline}`,
                fontFamily: theme.font, fontSize: 14, fontWeight: 500,
                display: 'inline-flex', alignItems: 'center', cursor: 'pointer',
              }}>
                <span>{c.emoji}</span>{c.name}
              </button>
            ))}
          </div>
        </div>

        {/* Live conflict banner */}
        {hasConflict ? (
          <button onClick={() => nav('conflicts')} style={{
            width: '100%', padding: 16,
            background: theme.errorContainer, color: theme.onErrorContainer,
            border: 'none', borderRadius: R.lg, textAlign: 'left',
            display: 'flex', gap: 12, alignItems: 'center', cursor: 'pointer',
            fontFamily: theme.font, marginTop: 8,
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: 12,
              background: theme.error, color: theme.onError,
              display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}>
              <Icon.AlertTriangle size={18} stroke={theme.onError}/>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15, fontWeight: 700 }}>{conflictCount} membres déjà mobilisés</div>
              <div style={{ fontSize: 13, marginTop: 2, opacity: 0.9 }}>Tape pour voir qui et choisir une action</div>
            </div>
            <Icon.ChevronRight size={22} stroke={theme.onErrorContainer}/>
          </button>
        ) : (
          <Banner theme={theme} tone="success" icon={<Icon.Check size={18}/>}>
            Aucun conflit détecté sur ce créneau.
          </Banner>
        )}
      </div>

      <div style={{
        padding: '12px 20px 16px', background: theme.background,
        borderTop: `1px solid ${theme.divider}`,
      }}>
        <Button theme={theme} variant={hasConflict ? 'outlined' : 'primary'} size="lg" fullWidth
          onClick={() => hasConflict ? nav('conflicts') : nav('home')}>
          {hasConflict ? `Voir les ${conflictCount} conflits` : 'Créer la réunion'}
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Date strip — horizontally scrollable next N days
// ────────────────────────────────────────────────────────────
function DateStrip({ theme, value, onChange }) {
  // 14 days from May 18, 2026
  const dates = [];
  for (let i = 0; i < 14; i++) {
    const day = 18 + i;
    if (day <= 31) {
      const iso = `2026-05-${String(day).padStart(2, '0')}`;
      dates.push({ iso, day, dowIdx: ((4 + i) % 7) }); // 18 may = mon = idx 0... actually 18 may 2026 is monday
    }
  }
  return (
    <div style={{ display: 'flex', gap: 8, overflow: 'auto', paddingBottom: 4, margin: '0 -20px', padding: '4px 20px' }}>
      {dates.map(d => {
        const sel = d.iso === value;
        return (
          <button key={d.iso} onClick={() => onChange(d.iso)} style={{
            minWidth: 56, height: 72, borderRadius: 14, flexShrink: 0,
            background: sel ? theme.onSurface : theme.surface,
            border: `1.5px solid ${sel ? theme.onSurface : theme.outline}`,
            display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2,
            cursor: 'pointer', fontFamily: theme.font,
          }}>
            <div style={{
              fontSize: 11, fontWeight: 600,
              color: sel ? theme.background : theme.onSurfaceVariant,
              textTransform: 'uppercase', letterSpacing: '0.5px',
            }}>{DAYS_FR[d.dowIdx].replace('.','')}</div>
            <div style={{
              fontSize: 22, fontWeight: 700,
              color: sel ? theme.background : theme.onSurface,
            }}>{d.day}</div>
          </button>
        );
      })}
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Conflict screen — show all conflicted members + 3 actions
// ────────────────────────────────────────────────────────────
function ConflictsScreen({ theme, nav }) {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        title="Conflits détectés"
        leading={<IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('create-meeting')}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 24px' }}>
        <div style={{
          padding: '20px 20px', borderRadius: R.lg,
          background: theme.errorContainer, color: theme.onErrorContainer,
          fontFamily: theme.font, marginBottom: 18,
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
            <Icon.AlertTriangle size={22} stroke={theme.error}/>
            <div style={{ fontSize: 22, fontWeight: 700, color: theme.error }}>3 conflits</div>
          </div>
          <div style={{ fontSize: 14, lineHeight: '20px' }}>
            Trois membres de la commission <b>Sport</b> sont déjà mobilisés <b>jeudi 21 mai à 20h00</b>.
          </div>
        </div>

        <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Qui est en conflit</SectionLabel>
        <Card theme={theme} variant="outlined" padding={0}>
          {NEW_MEETING_CONFLICTS.map((c, i) => {
            const commission = commissionById(c.commission);
            return (
              <div key={i} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
                borderBottom: i < NEW_MEETING_CONFLICTS.length - 1 ? `1px solid ${theme.divider}` : 'none',
                fontFamily: theme.font,
              }}>
                <Avatar name={c.member} size={40} theme={theme}/>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: theme.onSurface }}>{c.member}</div>
                  <div style={{ fontSize: 13, color: theme.onSurfaceVariant, marginTop: 2, display: 'flex', alignItems: 'center', gap: 6 }}>
                    <span style={{ width: 7, height: 7, borderRadius: 4, background: commission.accent }}/>
                    {commission.name} · {c.time}
                  </div>
                </div>
              </div>
            );
          })}
        </Card>

        <SectionLabel theme={theme} style={{ marginTop: 24, marginBottom: 10 }}>Que veux-tu faire ?</SectionLabel>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          <ActionRow theme={theme}
            icon={<Icon.Sparkles size={22} stroke={theme.primary}/>}
            iconBg={theme.primaryContainer}
            title="Voir d'autres horaires libres"
            sub="On te suggère 3 créneaux où personne n'est pris"
            primary
            onClick={() => nav('suggestions')}/>
          <ActionRow theme={theme}
            icon={<Icon.Edit size={20} stroke={theme.onSurface}/>}
            iconBg={theme.surfaceContainerHigh}
            title="Décaler la réunion"
            sub="Reviens à l'étape précédente pour modifier"
            onClick={() => nav('create-meeting')}/>
          <ActionRow theme={theme}
            icon={<Icon.AlertTriangle size={20} stroke={theme.error}/>}
            iconBg={theme.errorContainer}
            title="Créer quand même"
            sub="Les 3 conflictés recevront une notification pour trancher"
            danger
            onClick={() => nav('home')}/>
        </div>
      </div>
    </div>
  );
}

function ActionRow({ theme, icon, iconBg, title, sub, onClick, primary, danger }) {
  return (
    <button onClick={onClick} style={{
      width: '100%', textAlign: 'left', cursor: 'pointer',
      background: theme.surface,
      border: `1.5px solid ${primary ? theme.primary : danger ? theme.outline : theme.outline}`,
      borderRadius: R.lg, padding: '14px 16px',
      display: 'flex', alignItems: 'center', gap: 14,
      fontFamily: theme.font,
    }}>
      <div style={{
        width: 44, height: 44, borderRadius: 12,
        background: iconBg,
        display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
      }}>{icon}</div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 16, fontWeight: 600, color: danger ? theme.error : theme.onSurface }}>{title}</div>
        <div style={{ fontSize: 13, color: theme.onSurfaceVariant, marginTop: 2, lineHeight: '18px' }}>{sub}</div>
      </div>
      <Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// Suggestions — 3 free slots
// ────────────────────────────────────────────────────────────
function SuggestionsScreen({ theme, nav }) {
  const [picked, setPicked] = React.useState(null);
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        title="Créneaux libres"
        leading={<IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('conflicts')}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 100px' }}>
        <h2 style={{
          margin: '0 0 6px', fontFamily: theme.font, fontSize: 22, fontWeight: 700,
          color: theme.onSurface, letterSpacing: '-0.2px',
        }}>3 créneaux pour ta réunion Sport</h2>
        <p style={{
          margin: '0 0 20px', fontFamily: theme.font, fontSize: 14, lineHeight: '20px',
          color: theme.onSurfaceVariant,
        }}>Tous les membres de la commission Sport sont libres sur ces horaires (durée 1h30).</p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          {SUGGESTED_SLOTS.map((s, i) => {
            const sel = picked === i;
            const d = fmtDate(s.date);
            const endMin = parseInt(s.time.split(':')[0]) * 60 + parseInt(s.time.split(':')[1]) + s.duration;
            const endStr = `${String(Math.floor(endMin/60)).padStart(2,'0')}:${String(endMin%60).padStart(2,'0')}`;
            return (
              <button key={i} onClick={() => setPicked(i)} style={{
                width: '100%', textAlign: 'left', cursor: 'pointer',
                background: sel ? theme.primaryContainer : theme.surface,
                border: `1.75px solid ${sel ? theme.primary : theme.outline}`,
                borderRadius: R.lg, padding: '16px 18px',
                fontFamily: theme.font,
                display: 'flex', alignItems: 'center', gap: 16,
              }}>
                <div style={{
                  width: 56, height: 64, borderRadius: 12,
                  background: sel ? theme.primary : theme.surfaceContainer,
                  color: sel ? theme.onPrimary : theme.onSurface,
                  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
                  flexShrink: 0,
                }}>
                  <div style={{ fontSize: 11, fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.5px', opacity: 0.8 }}>{DAYS_FR[d.dow].replace('.','')}</div>
                  <div style={{ fontSize: 24, fontWeight: 700, lineHeight: '28px' }}>{d.day}</div>
                  <div style={{ fontSize: 10, fontWeight: 600, opacity: 0.8 }}>{d.monthName.slice(0,3)}.</div>
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 18, fontWeight: 700, color: sel ? theme.onPrimaryContainer : theme.onSurface, fontVariantNumeric: 'tabular-nums' }}>
                    {s.time} – {endStr}
                  </div>
                  <div style={{ fontSize: 13, color: sel ? theme.onPrimaryContainer : theme.onSurfaceVariant, marginTop: 4, display: 'flex', alignItems: 'center', gap: 6 }}>
                    <Icon.Check size={14} stroke={sel ? theme.primary : theme.success} strokeWidth={2.5}/>
                    Tout le monde est libre
                  </div>
                </div>
                {sel && (
                  <div style={{
                    width: 28, height: 28, borderRadius: '50%',
                    background: theme.primary, color: theme.onPrimary,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}>
                    <Icon.Check size={16} stroke={theme.onPrimary} strokeWidth={3}/>
                  </div>
                )}
              </button>
            );
          })}
        </div>

        <div style={{
          marginTop: 20, padding: 14, borderRadius: R.md,
          background: theme.surfaceContainer,
          display: 'flex', alignItems: 'center', gap: 10,
          fontFamily: theme.font,
        }}>
          <Icon.Sparkles size={18} stroke={theme.primary}/>
          <div style={{ fontSize: 13, color: theme.onSurfaceVariant, lineHeight: '18px' }}>
            Aucun ne convient ? <button style={{ background: 'none', border: 'none', color: theme.primary, fontWeight: 600, fontSize: 13, padding: 0, cursor: 'pointer', fontFamily: theme.font }}>Voir 5 autres horaires</button>
          </div>
        </div>
      </div>

      <div style={{
        padding: '12px 20px 16px', background: theme.background,
        borderTop: `1px solid ${theme.divider}`,
      }}>
        <Button theme={theme} variant="primary" size="lg" fullWidth disabled={picked === null}
          onClick={() => nav('home')}>
          Créer la réunion
        </Button>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Meeting detail
// ────────────────────────────────────────────────────────────
function MeetingDetailScreen({ theme, nav, params }) {
  const m = MEETINGS.find(x => x.id === params.meetingId) || MEETINGS[2];
  const cs = m.commissions.map(c => commissionById(c));
  const d = fmtDate(m.date);
  const endMin = parseInt(m.time.split(':')[0]) * 60 + parseInt(m.time.split(':')[1]) + m.duration;
  const endStr = `${String(Math.floor(endMin/60)).padStart(2,'0')}:${String(endMin%60).padStart(2,'0')}`;
  const creator = memberById(m.creator);

  // members attending
  const attending = MEMBERS.filter(mb => mb.commissions.some(c => m.commissions.includes(c)));
  const isOrganizer = creator && creator.me;

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        leading={<IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('home')}/>}
        trailing={<IconButton theme={theme} icon={<Icon.MoreH size={22}/>}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 24px' }}>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 12 }}>
          {cs.map(c => (
            <div key={c.id} style={{
              padding: '4px 10px', borderRadius: R.pill,
              background: c.color, color: c.accent,
              fontFamily: theme.font, fontSize: 13, fontWeight: 500,
            }}>{c.emoji} {c.name}</div>
          ))}
        </div>
        <h1 style={{
          margin: 0, fontFamily: theme.font, fontSize: 26, fontWeight: 700,
          letterSpacing: '-0.3px', color: theme.onSurface, lineHeight: '32px',
        }}>{m.title}</h1>

        <div style={{
          marginTop: 18, padding: 16, borderRadius: R.lg,
          background: theme.surface, border: `1px solid ${theme.outlineSubtle}`,
        }}>
          <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
            <div style={{
              width: 48, height: 48, borderRadius: 12,
              background: theme.surfaceContainer,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: theme.primary, flexShrink: 0,
            }}>
              <Icon.Calendar size={22}/>
            </div>
            <div>
              <div style={{ fontFamily: theme.font, fontSize: 17, fontWeight: 600, color: theme.onSurface }}>
                {d.weekdayFull.charAt(0).toUpperCase() + d.weekdayFull.slice(1)} {d.day} {d.monthName}
              </div>
              <div style={{ fontFamily: theme.font, fontSize: 14, color: theme.onSurfaceVariant, marginTop: 2 }}>
                {m.time} – {endStr} · {m.duration} minutes
              </div>
            </div>
          </div>
        </div>

        {m.conflicted && (
          <div style={{ marginTop: 12 }}>
            <Banner theme={theme} tone="warning"
              icon={<Icon.AlertTriangle size={20}/>}
              title="Tu es en conflit"
              action="Choisir où je vais"
              onAction={() => nav('arbitration')}>
              Cette réunion chevauche un autre engagement.
            </Banner>
          </div>
        )}

        <div style={{ marginTop: 24 }}>
          <SectionLabel theme={theme} style={{ marginBottom: 10 }}>
            Convoqués ({attending.length})
          </SectionLabel>
          <Card theme={theme} variant="outlined" padding={0}>
            {attending.slice(0, 6).map((mb, i) => (
              <div key={mb.id} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '12px 16px',
                borderBottom: i < Math.min(attending.length, 6) - 1 ? `1px solid ${theme.divider}` : 'none',
              }}>
                <Avatar name={mb.name} size={36} theme={theme}/>
                <div style={{ flex: 1, fontFamily: theme.font, fontSize: 15, fontWeight: 500, color: theme.onSurface }}>
                  {mb.name}{mb.me ? ' · toi' : ''}
                </div>
                {mb.id === 'm2' && m.conflicted && (
                  <div style={{
                    padding: '2px 8px', borderRadius: R.pill,
                    background: theme.errorContainer, color: theme.onErrorContainer,
                    fontFamily: theme.font, fontSize: 11, fontWeight: 600,
                  }}>Conflit</div>
                )}
              </div>
            ))}
            {attending.length > 6 && (
              <div style={{
                padding: '12px 16px', borderTop: `1px solid ${theme.divider}`,
                fontFamily: theme.font, fontSize: 14, fontWeight: 500,
                color: theme.primary, textAlign: 'center', cursor: 'pointer',
              }}>Voir les {attending.length - 6} autres</div>
            )}
          </Card>
        </div>

        <div style={{ marginTop: 24, display: 'flex', alignItems: 'center', gap: 10 }}>
          <Avatar name={creator?.name || 'Marc Bernard'} size={28} theme={theme}/>
          <div style={{ fontFamily: theme.font, fontSize: 13, color: theme.onSurfaceVariant }}>
            Créée par <b style={{ color: theme.onSurface }}>{creator?.name || 'Marc Bernard'}</b>
          </div>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, {
  CreateMeetingScreen, ConflictsScreen, SuggestionsScreen, MeetingDetailScreen,
});
