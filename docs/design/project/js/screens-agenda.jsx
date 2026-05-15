// Agenda screens: personal week + month aggregated view, commission detail, members list, settings.

const DAYS_FR = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
const MONTHS_FR = ['Janvier','Février','Mars','Avril','Mai','Juin','Juillet','Août','Septembre','Octobre','Novembre','Décembre'];

// Week of May 18-24, 2026 (Mon-Sun)
const WEEK_DATES = ['2026-05-18','2026-05-19','2026-05-20','2026-05-21','2026-05-22','2026-05-23','2026-05-24'];

function meetingsOnDate(date, memberId) {
  const D = window.COLISION_DATA;
  const m = D.memberById(memberId) || D.memberById('m2');
  // Personal feed: meetings where one of the commissions is mine
  return D.MEETINGS.filter(r => r.date === date && r.commissions.some(c => m.commissions.includes(c)));
}

function pad2(n) { return String(n).padStart(2, '0'); }
function addMin(hhmm, min) {
  const [h, m] = hhmm.split(':').map(Number);
  const total = h * 60 + m + min;
  return `${pad2(Math.floor(total / 60))}:${pad2(total % 60)}`;
}

// ── Agenda — combined week timeline + day list ─────────────────────────────
function ScrAgenda({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const self = state.selfMemberId || 'm2';
  const me = D.memberById(self);
  const [tabView, setTabView] = React.useState('semaine'); // 'semaine' | 'mois'
  const todayIdx = 0; // pretend Mon is today
  const hasPending = state.pendingArbitration;

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      {/* Project header */}
      <div style={{ padding: '12px 20px 4px', display: 'flex', flexDirection: 'column' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <div style={{ fontSize: 11, color: t.inkMuted, fontWeight: 600, letterSpacing: 0.5, textTransform: 'uppercase' }}>
              {D.PROJECT.name.replace('Conseil municipal de ', '').slice(0, 22)}
            </div>
            <h1 style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 32, fontWeight: 400, lineHeight: 1.05, margin: '2px 0 0', letterSpacing: -0.3, color: t.ink }}>
              Bonjour, {me.name.split(' ')[0]}.
            </h1>
          </div>
          <button onClick={() => nav.go('settings')} style={{
            border: 'none', background: 'transparent', cursor: 'pointer', padding: 0,
          }}>
            <ColAvatar t={t} name={me.name} size={40} />
          </button>
        </div>
      </div>

      {/* Pending arbitration banner */}
      {hasPending && (
        <div style={{ padding: '12px 16px 0' }}>
          <button onClick={() => nav.go('arbitration')} style={{
            width: '100%', textAlign: 'left', cursor: 'pointer',
            background: t.conflictBg, border: `1px solid ${t.conflict}33`,
            borderRadius: 16, padding: '12px 14px',
            display: 'flex', alignItems: 'center', gap: 12, fontFamily: 'inherit',
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: 12, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
            }}>
              <ColIcon name="warn" size={20} color={t.conflict} strokeWidth={2} />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: t.conflictInk }}>Conflit à arbitrer</div>
              <div style={{ fontSize: 12, color: t.conflictInk, opacity: 0.85, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                Marc a créé une réunion Sport qui chevauche ton agenda.
              </div>
            </div>
            <ColIcon name="chevron-right" size={20} color={t.conflictInk} />
          </button>
        </div>
      )}

      {/* View tabs */}
      <div style={{ padding: '14px 16px 8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ display: 'flex', gap: 0, background: t.bgMuted, padding: 3, borderRadius: 999, border: `1px solid ${t.border2}` }}>
          {['semaine', 'mois'].map(v => (
            <button key={v} onClick={() => setTabView(v)} style={{
              padding: '6px 14px', borderRadius: 999, border: 'none', cursor: 'pointer',
              background: tabView === v ? t.bgRaised : 'transparent',
              color: tabView === v ? t.ink : t.inkMuted,
              fontWeight: tabView === v ? 600 : 500, fontSize: 13,
              fontFamily: 'inherit',
              boxShadow: tabView === v ? t.shadow1 : 'none',
            }}>{v[0].toUpperCase() + v.slice(1)}</button>
          ))}
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 4, color: t.inkMuted }}>
          <button style={{ width: 32, height: 32, background: 'transparent', border: 'none', cursor: 'pointer', borderRadius: 999, color: t.inkMuted, padding: 0 }}>
            <ColIcon name="chevron-left" size={20} color={t.inkMuted} />
          </button>
          <span style={{ fontSize: 14, fontWeight: 500, color: t.ink }}>Mai 2026</span>
          <button style={{ width: 32, height: 32, background: 'transparent', border: 'none', cursor: 'pointer', borderRadius: 999, color: t.inkMuted, padding: 0 }}>
            <ColIcon name="chevron-right" size={20} color={t.inkMuted} />
          </button>
        </div>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 100px' }}>
        {tabView === 'semaine' ? <WeekView t={t} self={self} todayIdx={todayIdx} nav={nav} /> : <MonthView t={t} self={self} nav={nav} />}
      </div>

      {/* FAB */}
      <button onClick={() => nav.go('create-meeting')} style={{
        position: 'absolute', right: 18, bottom: 84, zIndex: 5,
        height: 56, paddingLeft: 18, paddingRight: 22, borderRadius: 999,
        background: t.accent, color: '#fff', border: 'none',
        display: 'flex', alignItems: 'center', gap: 10,
        fontFamily: '"Geist", system-ui', fontSize: 15, fontWeight: 600, cursor: 'pointer',
        boxShadow: '0 6px 16px rgba(14,124,102,0.35), 0 2px 6px rgba(14,124,102,0.25)',
      }}>
        <ColIcon name="plus" size={22} color="#fff" strokeWidth={2.5} />
        Nouvelle réunion
      </button>

      <ColBottomNav t={t} tab="agenda" onTab={tab => set({ tab })} hasArbitration={hasPending} />
    </div>
  );
}

// Day strip + meeting list
function WeekView({ t, self, todayIdx, nav }) {
  const D = window.COLISION_DATA;
  const [selectedIdx, setSelectedIdx] = React.useState(todayIdx);

  return (
    <div>
      {/* Day strip */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 4, padding: '8px 0 14px' }}>
        {WEEK_DATES.map((d, i) => {
          const dayNum = Number(d.split('-')[2]);
          const isSel = i === selectedIdx;
          const isToday = i === todayIdx;
          const meetings = meetingsOnDate(d, self);
          return (
            <button key={d} onClick={() => setSelectedIdx(i)} style={{
              display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 6,
              padding: '8px 0 10px', borderRadius: 14, cursor: 'pointer',
              background: isSel ? t.ink : 'transparent', border: 'none',
              color: isSel ? t.bg : t.ink,
              fontFamily: 'inherit',
            }}>
              <div style={{
                fontSize: 11, fontWeight: 500, opacity: 0.7,
                color: isSel ? t.bg : t.inkMuted, letterSpacing: 0.2,
              }}>{DAYS_FR[i]}</div>
              <div style={{
                fontSize: 18, fontWeight: 600, color: isSel ? t.bg : t.ink,
                fontFamily: '"Geist", system-ui',
              }}>{dayNum}</div>
              <div style={{ display: 'flex', gap: 2, height: 6, alignItems: 'center' }}>
                {meetings.slice(0, 3).map((m, j) => {
                  const c = D.commissionById(m.commissions[0]);
                  return <span key={j} style={{ width: 4, height: 4, borderRadius: '50%', background: isSel ? t.bg : c.dot }} />;
                })}
                {meetings.length === 0 && <span style={{ width: 4, height: 4, opacity: 0 }} />}
              </div>
            </button>
          );
        })}
      </div>

      {/* Selected day list */}
      <div style={{ padding: '4px 4px 12px' }}>
        <div style={{ fontSize: 13, color: t.inkMuted, fontWeight: 600, letterSpacing: 0.4, textTransform: 'uppercase' }}>
          {humanDate(WEEK_DATES[selectedIdx])}
        </div>
      </div>
      <DayMeetingList t={t} date={WEEK_DATES[selectedIdx]} self={self} nav={nav} />
    </div>
  );
}

function DayMeetingList({ t, date, self, nav }) {
  const D = window.COLISION_DATA;
  const ms = meetingsOnDate(date, self).sort((a, b) => a.start.localeCompare(b.start));
  if (ms.length === 0) {
    return (
      <div style={{ padding: '32px 0', textAlign: 'center', color: t.inkSubtle, fontSize: 14 }}>
        Rien de prévu. Souffle.
      </div>
    );
  }
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      {ms.map(m => <MeetingRow key={m.id} t={t} meeting={m} nav={nav} />)}
    </div>
  );
}

function MeetingRow({ t, meeting, nav }) {
  const D = window.COLISION_DATA;
  const cs = meeting.commissions.map(id => D.commissionById(id));
  const end = addMin(meeting.start, meeting.durationMin);
  return (
    <button onClick={() => nav && nav.go && nav.go('agenda')} style={{
      width: '100%', textAlign: 'left', padding: 0, background: 'transparent', border: 'none', cursor: 'pointer', fontFamily: 'inherit',
    }}>
      <div style={{
        background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`,
        padding: '14px 14px 14px 16px', display: 'flex', gap: 14, alignItems: 'stretch',
        position: 'relative', overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', left: 0, top: 10, bottom: 10, width: 4, background: cs[0].dot, borderRadius: '0 4px 4px 0' }} />
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', minWidth: 48, paddingTop: 2 }}>
          <div style={{ fontSize: 17, fontWeight: 600, color: t.ink, letterSpacing: -0.2, fontFamily: '"Geist Mono", monospace' }}>
            {meeting.start}
          </div>
          <div style={{ fontSize: 12, color: t.inkMuted, letterSpacing: -0.1, fontFamily: '"Geist Mono", monospace' }}>{end}</div>
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: 15, fontWeight: 600, color: t.ink, marginBottom: 6, lineHeight: 1.25 }}>{meeting.title}</div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {cs.map(c => <ColCommissionChip key={c.id} commission={c} size="sm" t={t} />)}
          </div>
        </div>
      </div>
    </button>
  );
}

function humanDate(iso) {
  const d = new Date(iso + 'T00:00');
  return `${DAYS_FR[(d.getDay() + 6) % 7]}. ${d.getDate()} ${MONTHS_FR[d.getMonth()].toLowerCase()}`;
}

// ── Month View ─────────────────────────────────────────────────────────────
function MonthView({ t, self, nav }) {
  const D = window.COLISION_DATA;
  // Render May 2026 (35 cells: starts Friday May 1)
  // May 1 2026 is a Friday => 4 empty cells before
  const monthStart = new Date(2026, 4, 1);
  const startDay = (monthStart.getDay() + 6) % 7; // Mon-based
  const daysInMonth = 31;
  const cells = [];
  for (let i = 0; i < startDay; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);
  while (cells.length % 7) cells.push(null);

  return (
    <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, overflow: 'hidden', padding: 6 }}>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: 0 }}>
        {DAYS_FR.map((d, i) => (
          <div key={i} style={{ textAlign: 'center', fontSize: 11, fontWeight: 600, color: t.inkSubtle, padding: '6px 0 8px', letterSpacing: 0.2 }}>{d[0]}</div>
        ))}
        {cells.map((d, i) => {
          if (!d) return <div key={i} />;
          const iso = `2026-05-${pad2(d)}`;
          const ms = meetingsOnDate(iso, self);
          const isToday = d === 18;
          const dots = ms.slice(0, 4).map((m, j) => D.commissionById(m.commissions[0]));
          return (
            <div key={i} style={{
              aspectRatio: '1/1.05', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'flex-start',
              padding: '6px 0 4px', borderRadius: 10, position: 'relative',
              background: isToday ? t.accentMuted : 'transparent',
            }}>
              <div style={{
                fontSize: 13, fontWeight: isToday ? 700 : 500, color: isToday ? t.accentStrong : t.ink,
                fontFamily: '"Geist", system-ui',
              }}>{d}</div>
              <div style={{ display: 'flex', gap: 2, marginTop: 'auto', marginBottom: 4, height: 6 }}>
                {dots.map((c, j) => <span key={j} style={{ width: 5, height: 5, borderRadius: '50%', background: c.dot }} />)}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

// ── Commissions index ─────────────────────────────────────────────────────
function ScrCommissions({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title="Commissions" large
        subtitle={`${D.COMMISSIONS.length} commissions`} />
      <div style={{ flex: 1, overflow: 'auto', padding: '4px 16px 100px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {D.COMMISSIONS.map(c => {
            const memberCount = D.membersOfCommission(c.id).length;
            const upcoming = D.MEETINGS.filter(m => m.commissions.includes(c.id)).length;
            const me = D.memberById(state.selfMemberId);
            const mine = me && me.commissions.includes(c.id);
            return (
              <button key={c.id} onClick={() => { set({ openCommission: c.id }); nav.go('commission-detail'); }} style={{
                width: '100%', display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
                borderRadius: 16, background: t.bgRaised,
                border: `1px solid ${t.borderSubtle}`, cursor: 'pointer', textAlign: 'left',
                fontFamily: 'inherit',
              }}>
                <div style={{ width: 44, height: 44, borderRadius: 12, background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <div style={{ width: 14, height: 14, borderRadius: '50%', background: c.dot }} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <div style={{ fontSize: 16, fontWeight: 600, color: t.ink }}>{c.name}</div>
                    {mine && <span style={{ fontSize: 10, fontWeight: 600, background: t.accentMuted, color: t.accentStrong, padding: '2px 6px', borderRadius: 999, letterSpacing: 0.4 }}>MOI</span>}
                  </div>
                  <div style={{ fontSize: 13, color: t.inkMuted, marginTop: 2 }}>
                    {memberCount} membres · {upcoming} {upcoming > 1 ? 'réunions' : 'réunion'}
                  </div>
                </div>
                <ColIcon name="chevron-right" size={20} color={t.inkSubtle} />
              </button>
            );
          })}
        </div>
        <button onClick={() => {}} style={{
          width: '100%', marginTop: 12, padding: '14px 16px', borderRadius: 16,
          background: 'transparent', border: `1px dashed ${t.borderStrong}`,
          color: t.ink, fontSize: 15, fontWeight: 500, cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          fontFamily: 'inherit',
        }}>
          <ColIcon name="plus" size={18} color={t.ink} /> Créer une commission
        </button>
      </div>
      <ColBottomNav t={t} tab="commissions" onTab={tab => set({ tab })} />
    </div>
  );
}

// ── Commission detail ─────────────────────────────────────────────────────
function ScrCommissionDetail({ t, nav, state }) {
  const D = window.COLISION_DATA;
  const c = D.commissionById(state.openCommission || 'c2');
  const meetings = D.MEETINGS.filter(m => m.commissions.includes(c.id)).sort((a, b) => (a.date + a.start).localeCompare(b.date + b.start));
  const members = D.membersOfCommission(c.id);

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('commissions')} right={
        <button style={{ width: 40, height: 40, borderRadius: 999, background: 'transparent', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', color: t.ink }}>
          <ColIcon name="more" size={22} color={t.ink} />
        </button>
      } />
      {/* Hero header */}
      <div style={{ padding: '0 20px 16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <div style={{ width: 56, height: 56, borderRadius: 16, background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <div style={{ width: 20, height: 20, borderRadius: '50%', background: c.dot }} />
          </div>
          <div>
            <div style={{ fontSize: 12, color: t.inkMuted, fontWeight: 600, letterSpacing: 0.5, textTransform: 'uppercase' }}>Commission</div>
            <h1 style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 32, fontWeight: 400, lineHeight: 1.05, margin: '2px 0 0', letterSpacing: -0.3 }}>{c.name}</h1>
          </div>
        </div>
        {/* Member stack */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 18 }}>
          <div style={{ display: 'flex' }}>
            {members.slice(0, 5).map((m, i) => (
              <div key={m.id} style={{ marginLeft: i > 0 ? -10 : 0, position: 'relative', zIndex: 5 - i }}>
                <ColAvatar t={t} name={m.name} size={28} ring />
              </div>
            ))}
            {members.length > 5 && (
              <div style={{
                marginLeft: -10, width: 28, height: 28, borderRadius: '50%',
                background: t.bgMuted, color: t.inkMuted, border: `2px solid ${t.bg}`,
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 11, fontWeight: 600,
              }}>+{members.length - 5}</div>
            )}
          </div>
          <span style={{ fontSize: 13, color: t.inkMuted }}>{members.length} membres</span>
        </div>
      </div>

      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 100px' }}>
        <ColSectionLabel t={t}>À venir</ColSectionLabel>
        {meetings.length === 0 ? (
          <div style={{ padding: '24px 0', textAlign: 'center', color: t.inkSubtle, fontSize: 14 }}>Aucune réunion prévue.</div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 4 }}>
            {meetings.map(m => <CommissionMeetingRow key={m.id} t={t} meeting={m} commission={c} />)}
          </div>
        )}
      </div>
    </div>
  );
}

function CommissionMeetingRow({ t, meeting, commission }) {
  const D = window.COLISION_DATA;
  const d = new Date(meeting.date + 'T00:00');
  const end = addMin(meeting.start, meeting.durationMin);
  return (
    <div style={{
      background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`,
      padding: '14px 16px', display: 'flex', gap: 14, alignItems: 'center',
    }}>
      <div style={{
        width: 50, height: 56, borderRadius: 12, background: commission.bg,
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
      }}>
        <div style={{ fontSize: 10, fontWeight: 700, color: commission.ink, letterSpacing: 0.5, textTransform: 'uppercase' }}>{MONTHS_FR[d.getMonth()].slice(0, 3)}</div>
        <div style={{ fontSize: 20, fontWeight: 700, color: commission.ink, lineHeight: 1 }}>{d.getDate()}</div>
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15, fontWeight: 600, color: t.ink, marginBottom: 4 }}>{meeting.title}</div>
        <div style={{ fontSize: 12, color: t.inkMuted, fontFamily: '"Geist Mono", monospace' }}>
          {DAYS_FR[(d.getDay() + 6) % 7]}. {meeting.start} → {end}
        </div>
      </div>
      <ColIcon name="chevron-right" size={20} color={t.inkSubtle} />
    </div>
  );
}

Object.assign(window, {
  ScrAgenda, ScrCommissions, ScrCommissionDetail,
  WEEK_DATES, DAYS_FR, MONTHS_FR, meetingsOnDate, humanDate, addMin, pad2,
});
