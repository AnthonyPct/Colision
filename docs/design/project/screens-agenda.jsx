// screens-agenda.jsx — Home agenda (week + month) and commission detail

// ────────────────────────────────────────────────────────────
// Helper: build meetings indexed by date
// ────────────────────────────────────────────────────────────
function meetingsByDate() {
  const map = {};
  MEETINGS.forEach(m => {
    (map[m.date] = map[m.date] || []).push(m);
  });
  return map;
}

// ────────────────────────────────────────────────────────────
// Home — agenda (week + month toggle)
// ────────────────────────────────────────────────────────────
function HomeScreen({ theme, nav, onCreate, view, setView }) {
  const me = MEMBERS.find(m => m.me);
  const arb = PENDING_ARBITRATIONS[0];
  const myMeetings = MEETINGS.filter(m =>
    m.commissions.some(c => me.commissions.includes(c))
  ).sort((a, b) => (a.date + a.time).localeCompare(b.date + b.time));

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background, overflow: 'hidden', position: 'relative' }}>
      <AppBar theme={theme}
        leading={<IconButton theme={theme} icon={<Icon.Logo size={24} color={theme.primary}/>}/>}
        title={null}
        trailing={
          <IconButton theme={theme} icon={<Icon.Bell size={22}/>} onClick={() => nav('arbitration')} badge={arb}/>
        }
        large
      />

      <div style={{ padding: '0 20px 4px' }}>
        <div style={{ ...T.headline, fontFamily: theme.font, color: theme.onSurface, letterSpacing: '-0.4px' }}>
          Bonjour {me.name.split(' ')[0]}
        </div>
        <div style={{ fontFamily: theme.font, fontSize: 14, color: theme.onSurfaceVariant, marginTop: 2 }}>
          Mercredi 20 mai · 4 réunions cette semaine
        </div>
      </div>

      {/* View toggle */}
      <div style={{ padding: '14px 20px 8px', display: 'flex', gap: 8 }}>
        {['Semaine', 'Mois'].map((v) => {
          const key = v.toLowerCase();
          const active = view === key;
          return (
            <button key={v} onClick={() => setView(key)} style={{
              padding: '8px 14px', borderRadius: R.pill,
              background: active ? theme.onSurface : 'transparent',
              color: active ? theme.background : theme.onSurfaceVariant,
              border: `1px solid ${active ? theme.onSurface : theme.outline}`,
              fontFamily: theme.font, fontSize: 13, fontWeight: 600,
              cursor: 'pointer',
            }}>{v}</button>
          );
        })}
        <div style={{ flex: 1 }}/>
        <button style={{
          padding: '8px 12px', borderRadius: R.pill, gap: 6,
          background: 'transparent', color: theme.onSurfaceVariant,
          border: `1px solid ${theme.outline}`,
          fontFamily: theme.font, fontSize: 13, fontWeight: 500,
          cursor: 'pointer', display: 'inline-flex', alignItems: 'center',
        }}>
          <Icon.Filter size={14} stroke={theme.onSurfaceVariant}/>Toutes
        </button>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '8px 20px 88px' }}>
        {arb && (
          <button onClick={() => nav('arbitration')} style={{
            width: '100%', padding: 16, marginBottom: 16,
            background: theme.errorContainer, color: theme.onErrorContainer,
            border: 'none', borderRadius: R.lg, textAlign: 'left',
            display: 'flex', gap: 12, alignItems: 'center', cursor: 'pointer',
            fontFamily: theme.font,
          }}>
            <div style={{
              width: 40, height: 40, borderRadius: 12,
              background: theme.error, color: theme.onError,
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              flexShrink: 0,
            }}>
              <Icon.AlertTriangle size={20} stroke={theme.onError}/>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ fontSize: 15, fontWeight: 700 }}>1 conflit à arbitrer</div>
              <div style={{ fontSize: 13, marginTop: 2, opacity: 0.9 }}>Jeudi 21 mai à 20h00 — choisis où tu vas</div>
            </div>
            <Icon.ChevronRight size={22} stroke={theme.onErrorContainer}/>
          </button>
        )}

        {view === 'semaine' ? (
          <WeekView theme={theme} nav={nav} meetings={myMeetings} me={me}/>
        ) : (
          <MonthView theme={theme} nav={nav} me={me}/>
        )}
      </div>

      <div style={{ position: 'absolute', right: 20, bottom: 20, zIndex: 5 }}>
        <FAB theme={theme} icon={<Icon.Plus size={22} stroke={theme.onPrimaryContainer} strokeWidth={2.5}/>}
          label="Nouvelle réunion" onClick={() => onCreate()}/>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Week View — timeline-style cards grouped by day
// ────────────────────────────────────────────────────────────
function WeekView({ theme, nav, meetings, me }) {
  // Group by date
  const grouped = {};
  meetings.forEach(m => (grouped[m.date] = grouped[m.date] || []).push(m));
  const dates = Object.keys(grouped).sort();

  return (
    <div>
      {dates.map((date, di) => {
        const d = fmtDate(date);
        const isToday = date === '2026-05-20';
        return (
          <div key={date} style={{ marginBottom: 22 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 10, marginBottom: 10, paddingLeft: 4 }}>
              <div style={{
                fontFamily: theme.font, fontSize: 13, fontWeight: 700,
                color: isToday ? theme.primary : theme.onSurfaceVariant,
                textTransform: 'uppercase', letterSpacing: '0.6px',
              }}>{isToday ? 'Aujourd\'hui' : d.weekdayFull} {d.day}</div>
              {isToday && <div style={{
                width: 6, height: 6, borderRadius: 3, background: theme.primary,
              }}/>}
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {grouped[date].map(m => <MeetingCard key={m.id} m={m} theme={theme} nav={nav} me={me}/>)}
            </div>
          </div>
        );
      })}
    </div>
  );
}

function MeetingCard({ m, theme, nav, me, dense }) {
  const cs = m.commissions.map(c => commissionById(c));
  const startMin = parseInt(m.time.split(':')[0]) * 60 + parseInt(m.time.split(':')[1]);
  const endMin = startMin + m.duration;
  const fmt = (mm) => `${String(Math.floor(mm/60)).padStart(2,'0')}:${String(mm%60).padStart(2,'0')}`;
  const conflict = m.conflicted;
  const accent = cs[0]?.accent || theme.primary;

  return (
    <button onClick={() => nav('meeting-detail', { meetingId: m.id })} style={{
      width: '100%', textAlign: 'left', cursor: 'pointer',
      background: theme.surface,
      border: `1px solid ${conflict ? theme.error : theme.outlineSubtle}`,
      borderRadius: R.lg, padding: '14px 16px',
      fontFamily: theme.font,
      display: 'flex', gap: 14, alignItems: 'stretch',
      position: 'relative', overflow: 'hidden',
    }}>
      {/* time rail */}
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', minWidth: 52 }}>
        <div style={{ fontSize: 16, fontWeight: 700, color: theme.onSurface, fontVariantNumeric: 'tabular-nums' }}>{m.time}</div>
        <div style={{ fontSize: 11, color: theme.onSurfaceMuted, marginTop: 1, fontVariantNumeric: 'tabular-nums' }}>{fmt(endMin)}</div>
        <div style={{
          fontSize: 10, color: theme.onSurfaceMuted, marginTop: 6,
          background: theme.surfaceContainer, padding: '2px 6px', borderRadius: 6,
        }}>{m.duration}min</div>
      </div>

      {/* vertical accent */}
      <div style={{ width: 3, borderRadius: 2, background: accent }}/>

      {/* body */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 16, fontWeight: 600, color: theme.onSurface, letterSpacing: '-0.1px', lineHeight: '20px' }}>
          {m.title}
        </div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginTop: 8 }}>
          {cs.map(c => (
            <div key={c.id} style={{
              padding: '2px 8px', borderRadius: R.pill,
              background: c.color, color: c.accent,
              fontFamily: theme.font, fontSize: 12, fontWeight: 500,
              display: 'inline-flex', alignItems: 'center', gap: 4,
            }}>{c.emoji} {c.name}</div>
          ))}
        </div>
        {conflict && (
          <div style={{
            marginTop: 10, display: 'flex', alignItems: 'center', gap: 6,
            color: theme.error, fontFamily: theme.font, fontSize: 13, fontWeight: 600,
          }}>
            <Icon.AlertTriangle size={14}/>Tu es en conflit sur ce créneau
          </div>
        )}
      </div>
    </button>
  );
}

// ────────────────────────────────────────────────────────────
// Month View — calendar grid
// ────────────────────────────────────────────────────────────
function MonthView({ theme, nav, me }) {
  // May 2026: Fri 1 → Sun 31. weekday(1) = friday (idx 4 mon=0)
  const firstDow = 4; // friday
  const daysInMonth = 31;
  const cells = [];
  // leading empties
  for (let i = 0; i < firstDow; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);
  // trailing to fill 6 rows
  while (cells.length % 7 !== 0) cells.push(null);

  const myMeetings = MEETINGS.filter(m => m.commissions.some(c => me.commissions.includes(c)));
  const byDay = {};
  myMeetings.forEach(m => {
    const d = parseInt(m.date.split('-')[2]);
    (byDay[d] = byDay[d] || []).push(m);
  });

  return (
    <div>
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        marginBottom: 12, paddingLeft: 4, paddingRight: 4,
      }}>
        <div style={{ fontFamily: theme.font, fontSize: 18, fontWeight: 700, color: theme.onSurface }}>
          Mai 2026
        </div>
        <div style={{ display: 'flex', gap: 4 }}>
          <IconButton theme={theme} icon={<Icon.ChevronLeft size={20} stroke={theme.onSurfaceVariant}/>}/>
          <IconButton theme={theme} icon={<Icon.ChevronRight size={20} stroke={theme.onSurfaceVariant}/>}/>
        </div>
      </div>

      <Card theme={theme} variant="outlined" padding={12}>
        {/* day headers */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 2, marginBottom: 6 }}>
          {DAYS_FR.map((d, i) => (
            <div key={i} style={{
              textAlign: 'center', fontFamily: theme.font, fontSize: 11,
              fontWeight: 600, color: theme.onSurfaceMuted,
              textTransform: 'uppercase', letterSpacing: '0.5px',
              paddingBottom: 4,
            }}>{d.replace('.','')}</div>
          ))}
        </div>
        {/* cells */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 2 }}>
          {cells.map((d, i) => {
            if (!d) return <div key={i} style={{ aspectRatio: '1/1.1' }}/>;
            const items = byDay[d] || [];
            const isToday = d === 20;
            const hasConflict = items.some(m => m.conflicted);
            return (
              <div key={i} style={{
                aspectRatio: '1/1.1', borderRadius: 8, padding: 4,
                background: isToday ? theme.primaryContainer : 'transparent',
                display: 'flex', flexDirection: 'column', alignItems: 'center',
                cursor: items.length ? 'pointer' : 'default',
              }}>
                <div style={{
                  fontFamily: theme.font, fontSize: 13,
                  fontWeight: isToday ? 700 : 500,
                  color: isToday ? theme.onPrimaryContainer : theme.onSurface,
                }}>{d}</div>
                <div style={{ display: 'flex', gap: 2, marginTop: 'auto', justifyContent: 'center', flexWrap: 'wrap', maxWidth: '100%' }}>
                  {items.slice(0, 3).map(m => {
                    const c = commissionById(m.commissions[0]);
                    return <div key={m.id} style={{
                      width: 5, height: 5, borderRadius: 3,
                      background: m.conflicted ? theme.error : c.accent,
                    }}/>;
                  })}
                </div>
              </div>
            );
          })}
        </div>
      </Card>

      <div style={{ marginTop: 18 }}>
        <SectionLabel theme={theme}>À venir cette semaine</SectionLabel>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 10 }}>
          {myMeetings.slice(0, 3).map(m => <MeetingCard key={m.id} m={m} theme={theme} nav={nav} me={me}/>)}
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Commission tab — list of all commissions
// ────────────────────────────────────────────────────────────
function CommissionsScreen({ theme, nav }) {
  const me = MEMBERS.find(m => m.me);
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme} title="Commissions" large
        leading={null}
        trailing={<IconButton theme={theme} icon={<Icon.Plus size={22}/>}/>}
      />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 88px' }}>
        <div style={{ display: 'flex', gap: 6, marginBottom: 16, overflow: 'auto' }}>
          <Chip theme={theme} selected={true} variant="filter">Toutes ({COMMISSIONS.length})</Chip>
          <Chip theme={theme} variant="filter">Les miennes ({me.commissions.length})</Chip>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {COMMISSIONS.map(c => {
            const mine = me.commissions.includes(c.id);
            const meetings = MEETINGS.filter(m => m.commissions.includes(c.id));
            return (
              <button key={c.id} onClick={() => nav('commission-detail', { commissionId: c.id })}
                style={{
                  width: '100%', textAlign: 'left', cursor: 'pointer',
                  background: theme.surface,
                  border: `1px solid ${theme.outlineSubtle}`,
                  borderRadius: R.lg, padding: 16,
                  display: 'flex', gap: 14, alignItems: 'center',
                  fontFamily: theme.font,
                }}>
                <div style={{
                  width: 48, height: 48, borderRadius: 14,
                  background: c.color,
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: 22, flexShrink: 0,
                }}>{c.emoji}</div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ fontSize: 16, fontWeight: 600, color: theme.onSurface }}>{c.name}</div>
                    {mine && <div style={{
                      padding: '2px 8px', borderRadius: R.pill,
                      background: theme.primaryContainer, color: theme.onPrimaryContainer,
                      fontSize: 11, fontWeight: 600,
                    }}>Toi</div>}
                  </div>
                  <div style={{ fontSize: 13, color: theme.onSurfaceVariant, marginTop: 2 }}>
                    {MEMBERS.filter(m => m.commissions.includes(c.id)).length} membres · {meetings.length} réunion{meetings.length > 1 ? 's' : ''}
                  </div>
                </div>
                <Icon.ChevronRight size={20} stroke={theme.onSurfaceMuted}/>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ────────────────────────────────────────────────────────────
// Commission detail — meetings + members
// ────────────────────────────────────────────────────────────
function CommissionDetailScreen({ theme, nav, params }) {
  const c = commissionById(params.commissionId || 'c2');
  const meetings = MEETINGS.filter(m => m.commissions.includes(c.id))
    .sort((a, b) => (a.date + a.time).localeCompare(b.date + b.time));
  const members = MEMBERS.filter(m => m.commissions.includes(c.id));

  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: theme.background }}>
      <AppBar theme={theme}
        leading={<IconButton theme={theme} icon={<Icon.ChevronLeft size={26}/>} onClick={() => nav('commissions')}/>}
        trailing={<IconButton theme={theme} icon={<Icon.MoreH size={22}/>}/>}
      />
      <div style={{ flex: 1, overflow: 'auto' }}>
        <div style={{ padding: '8px 20px 20px' }}>
          <div style={{
            width: 64, height: 64, borderRadius: 18,
            background: c.color,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: 32, marginBottom: 14,
          }}>{c.emoji}</div>
          <h1 style={{
            margin: 0, fontFamily: theme.font, fontSize: 28, fontWeight: 700,
            letterSpacing: '-0.3px', color: theme.onSurface,
          }}>{c.name}</h1>
          <div style={{ fontSize: 14, color: theme.onSurfaceVariant, fontFamily: theme.font, marginTop: 4 }}>
            {members.length} membres · {meetings.length} réunion{meetings.length > 1 ? 's' : ''}
          </div>
        </div>

        <div style={{ padding: '0 20px 16px' }}>
          <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Réunions à venir</SectionLabel>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {meetings.map(m => <MeetingCard key={m.id} m={m} theme={theme} nav={nav} me={MEMBERS.find(x => x.me)}/>)}
          </div>
        </div>

        <div style={{ padding: '0 20px 24px' }}>
          <SectionLabel theme={theme} style={{ marginBottom: 10 }}>Membres ({members.length})</SectionLabel>
          <Card theme={theme} variant="outlined" padding={0}>
            {members.map((m, i) => (
              <div key={m.id} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '12px 16px',
                borderBottom: i < members.length - 1 ? `1px solid ${theme.divider}` : 'none',
              }}>
                <Avatar name={m.name} size={36} theme={theme}/>
                <div style={{ flex: 1, fontFamily: theme.font, fontSize: 15, fontWeight: 500, color: theme.onSurface }}>
                  {m.name}{m.me ? ' · toi' : ''}
                </div>
              </div>
            ))}
          </Card>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, {
  HomeScreen, WeekView, MonthView, MeetingCard,
  CommissionsScreen, CommissionDetailScreen,
});
