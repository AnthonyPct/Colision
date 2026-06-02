// Meeting creation flow: form, conflict detection, free-slot suggestions, success.

function ScrCreateMeeting({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const draft = state.meetingDraft || {
    title: 'Coordination tournoi inter-quartiers',
    date: '2026-05-26',
    start: '19:00',
    durationMin: 90,
    commissions: ['c2'], // Sport — preselected
  };

  const updateDraft = (patch) => set({ meetingDraft: { ...draft, ...patch } });
  const cs = draft.commissions.map(id => D.commissionById(id)).filter(Boolean);

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title="Nouvelle réunion" onBack={() => nav.go('agenda')} />
      <div style={{ flex: 1, overflow: 'auto', padding: '4px 16px 16px' }}>
        {/* Title */}
        <div style={{ marginTop: 4, marginBottom: 16 }}>
          <input value={draft.title} onChange={e => updateDraft({ title: e.target.value })} placeholder="Titre (facultatif)" style={{
            width: '100%', boxSizing: 'border-box',
            border: 'none', borderBottom: `1px solid ${t.borderSubtle}`,
            background: 'transparent', padding: '14px 4px 12px', outline: 'none',
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 24, fontWeight: 400, color: t.ink, letterSpacing: -0.2,
          }} />
        </div>

        {/* Date row */}
        <ColCard t={t} pad={0} style={{ marginBottom: 10 }}>
          <FieldRow t={t} icon="calendar" label="Date">
            <span style={{ fontSize: 15, fontWeight: 600, color: t.ink }}>Mardi 26 mai 2026</span>
          </FieldRow>
          <div style={{ height: 1, background: t.border2, margin: '0 16px' }} />
          <FieldRow t={t} icon="clock" label="Heure et durée">
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
              <span style={{ fontSize: 15, fontWeight: 600, color: t.ink, fontFamily: '"Geist Mono", monospace' }}>{draft.start}</span>
              <span style={{ fontSize: 13, color: t.inkMuted }}>• {Math.floor(draft.durationMin / 60)}h{draft.durationMin % 60 ? pad2(draft.durationMin % 60) : ''}</span>
            </div>
          </FieldRow>
        </ColCard>

        {/* Duration chips */}
        <div style={{ display: 'flex', gap: 6, padding: '4px 0 16px', overflowX: 'auto' }}>
          {[30, 45, 60, 90, 120, 180].map(d => {
            const sel = d === draft.durationMin;
            return (
              <button key={d} onClick={() => updateDraft({ durationMin: d })} style={{
                padding: '8px 14px', borderRadius: 999, fontSize: 13, fontWeight: 600,
                background: sel ? t.ink : t.bgRaised, color: sel ? t.bg : t.ink,
                border: `1px solid ${sel ? t.ink : t.borderSubtle}`,
                cursor: 'pointer', fontFamily: 'inherit', flexShrink: 0,
              }}>
                {d < 60 ? `${d} min` : d % 60 === 0 ? `${d / 60} h` : `${Math.floor(d/60)}h${pad2(d % 60)}`}
              </button>
            );
          })}
        </div>

        {/* Commissions picker */}
        <ColSectionLabel t={t}>Commission(s) concernée(s)</ColSectionLabel>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, padding: '6px 2px 12px' }}>
          {D.COMMISSIONS.map(c => {
            const on = draft.commissions.includes(c.id);
            return (
              <button key={c.id} onClick={() => {
                const next = on ? draft.commissions.filter(x => x !== c.id) : [...draft.commissions, c.id];
                updateDraft({ commissions: next });
              }} style={{
                display: 'inline-flex', alignItems: 'center', gap: 7,
                padding: '8px 12px 8px 10px', borderRadius: 999,
                background: on ? c.bg : t.bgRaised,
                border: `1px solid ${on ? c.dot : t.borderSubtle}`,
                cursor: 'pointer', fontFamily: 'inherit',
                color: on ? c.ink : t.ink, fontSize: 13, fontWeight: 600,
              }}>
                <span style={{ width: 7, height: 7, borderRadius: '50%', background: c.dot }} />
                {c.name}
              </button>
            );
          })}
        </div>

        {/* Preview: who's invited */}
        <ColSectionLabel t={t}>Sera invité.e.s</ColSectionLabel>
        <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, padding: '12px 14px', marginTop: 4 }}>
          <InvitedPreview t={t} commissionIds={draft.commissions} />
        </div>
      </div>

      <div style={{ padding: '12px 16px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <ColButton t={t} label="Vérifier les conflits" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={cs.length === 0}
          onClick={() => nav.go('conflicts')} />
      </div>
    </div>
  );
}

function FieldRow({ t, icon, label, children, onClick }) {
  return (
    <div onClick={onClick} style={{
      display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
      cursor: onClick ? 'pointer' : 'default',
    }}>
      <div style={{ width: 22, color: t.inkMuted, display: 'flex', alignItems: 'center' }}>
        <ColIcon name={icon} size={20} color={t.inkMuted} />
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 12, color: t.inkMuted, fontWeight: 600, letterSpacing: 0.3, textTransform: 'uppercase' }}>{label}</div>
        <div style={{ marginTop: 2 }}>{children}</div>
      </div>
      <ColIcon name="chevron-right" size={18} color={t.inkSubtle} />
    </div>
  );
}

function InvitedPreview({ t, commissionIds }) {
  const D = window.COLISION_DATA;
  const memberSet = new Set();
  commissionIds.forEach(cid => D.membersOfCommission(cid).forEach(m => memberSet.add(m.id)));
  const members = Array.from(memberSet).map(id => D.memberById(id));
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
      <div style={{ display: 'flex' }}>
        {members.slice(0, 6).map((m, i) => (
          <div key={m.id} style={{ marginLeft: i > 0 ? -8 : 0, position: 'relative', zIndex: 6 - i }}>
            <ColAvatar t={t} name={m.name} size={28} ring />
          </div>
        ))}
      </div>
      <div style={{ flex: 1 }}>
        <div style={{ fontSize: 14, fontWeight: 600, color: t.ink }}>{members.length} membres</div>
        <div style={{ fontSize: 12, color: t.inkMuted }}>recevront une notification</div>
      </div>
    </div>
  );
}

// ── Conflicts screen (Marc's view at meeting creation) ───────────────────
function ScrConflicts({ t, nav, state }) {
  const D = window.COLISION_DATA;
  // The conflicts as defined in PRD journey 3
  const conflicts = [
    { memberId: 'm2',  meetingId: 'r8', overlapStart: '20:00', overlapEnd: '21:30' },
    { memberId: 'm4',  meetingId: 'r5', overlapStart: '19:30', overlapEnd: '21:00' },
    { memberId: 'm5',  meetingId: 'r4', overlapStart: '20:00', overlapEnd: '22:00' },
  ];

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('create-meeting')} />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 16px' }}>
        {/* Big warning header */}
        <div style={{ marginTop: 4 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16, background: t.conflictBg,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <ColIcon name="warn" size={28} color={t.conflict} strokeWidth={2} />
          </div>
          <h1 style={{
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 32, fontWeight: 400, lineHeight: 1.05,
            margin: '16px 0 6px', letterSpacing: -0.3,
          }}>
            {conflicts.length} conflits<br />
            <span style={{ color: t.inkMuted }}>sur ce créneau.</span>
          </h1>
          <p style={{ fontSize: 15, color: t.inkMuted, margin: 0, lineHeight: 1.4 }}>
            Ces membres sont déjà mobilisés ailleurs au même moment.
          </p>
        </div>

        {/* Conflicts list */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 22 }}>
          {conflicts.map((cf, i) => {
            const m = D.memberById(cf.memberId);
            const r = D.MEETINGS.find(x => x.id === cf.meetingId);
            const c = D.commissionById(r.commissions[0]);
            return (
              <div key={i} style={{
                background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`,
                padding: '12px 14px', display: 'flex', gap: 12, alignItems: 'center',
              }}>
                <ColAvatar t={t} name={m.name} size={40} />
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: t.ink }}>{m.name}</div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginTop: 4, flexWrap: 'wrap' }}>
                    <ColCommissionChip commission={c} size="sm" t={t} />
                    <span style={{ fontSize: 12, color: t.inkMuted, fontFamily: '"Geist Mono", monospace' }}>
                      {cf.overlapStart}–{cf.overlapEnd}
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* Conflict timeline visual */}
        <div style={{ marginTop: 22 }}>
          <ColSectionLabel t={t}>Visualisation</ColSectionLabel>
          <ConflictTimeline t={t} conflicts={conflicts} />
        </div>
      </div>

      <div style={{ padding: '12px 16px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <ColButton t={t} label="Voir des créneaux libres" full size="lg" variant="accent" icon="sparkle"
          onClick={() => nav.go('suggestions')} />
        <div style={{ display: 'flex', gap: 8 }}>
          <ColButton t={t} label="Décaler" size="md" variant="secondary"
            onClick={() => nav.go('create-meeting')}
            full />
          <ColButton t={t} label="Créer quand même" size="md" variant="ghost"
            onClick={() => nav.go('meeting-created')}
            full />
        </div>
      </div>
    </div>
  );
}

function ConflictTimeline({ t, conflicts }) {
  const D = window.COLISION_DATA;
  // The proposed meeting is 19:00–20:30 (Marc's Sport)
  const startH = 18, endH = 22;
  const totalMin = (endH - startH) * 60;

  const toPct = (hhmm) => {
    const [h, m] = hhmm.split(':').map(Number);
    return ((h - startH) * 60 + m) / totalMin * 100;
  };

  // Proposed: 19:00–20:30
  const proposed = { start: '19:00', end: '20:30' };

  return (
    <div style={{
      background: t.bgRaised, border: `1px solid ${t.borderSubtle}`, borderRadius: 16,
      padding: 14,
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: t.inkSubtle, fontFamily: '"Geist Mono", monospace', marginBottom: 8, padding: '0 2px' }}>
        {['18h','19h','20h','21h','22h'].map(h => <span key={h}>{h}</span>)}
      </div>
      {/* Proposed row */}
      <TimelineRow t={t} label="Ma réunion" thick>
        <div style={{
          position: 'absolute', height: 24, borderRadius: 8,
          background: t.ink, color: t.bg,
          left: toPct(proposed.start) + '%',
          width: (toPct(proposed.end) - toPct(proposed.start)) + '%',
          display: 'flex', alignItems: 'center', padding: '0 8px',
          fontSize: 11, fontWeight: 600, letterSpacing: 0.2,
        }}>Sport 19:00</div>
      </TimelineRow>
      {conflicts.map((cf, i) => {
        const m = D.memberById(cf.memberId);
        const r = D.MEETINGS.find(x => x.id === cf.meetingId);
        const c = D.commissionById(r.commissions[0]);
        const end = addMin(r.start, r.durationMin);
        return (
          <TimelineRow key={i} t={t} label={m.name.split(' ')[0]}>
            <div style={{
              position: 'absolute', height: 20, borderRadius: 6,
              background: c.bg, border: `1px solid ${c.dot}55`,
              left: toPct(r.start) + '%',
              width: (toPct(end) - toPct(r.start)) + '%',
              display: 'flex', alignItems: 'center', padding: '0 6px',
              fontSize: 10, fontWeight: 600, color: c.ink,
            }}>{c.name}</div>
            {/* overlap stripe */}
            <div style={{
              position: 'absolute', top: -2, bottom: -2,
              left: `max(${toPct(cf.overlapStart)}%, ${toPct(r.start)}%)`,
              width: (toPct(cf.overlapEnd) - toPct(cf.overlapStart)) + '%',
              background: `repeating-linear-gradient(135deg, ${t.conflict}30 0 4px, transparent 4px 8px)`,
              borderRadius: 6, pointerEvents: 'none',
            }} />
          </TimelineRow>
        );
      })}
    </div>
  );
}

function TimelineRow({ t, label, children, thick }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
      <div style={{ width: 60, fontSize: 12, color: t.inkMuted, fontWeight: 500, flexShrink: 0 }}>{label}</div>
      <div style={{ flex: 1, height: thick ? 28 : 24, position: 'relative', background: t.bgMuted, borderRadius: 8 }}>
        {children}
      </div>
    </div>
  );
}

// ── Suggestions: free slots ───────────────────────────────────────────────
function ScrSuggestions({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const slots = [
    { date: '2026-05-26', start: '17:30', durationMin: 90, score: 'parfait', conflicts: 0 },
    { date: '2026-05-27', start: '20:30', durationMin: 90, score: 'parfait', conflicts: 0 },
    { date: '2026-05-23', start: '10:00', durationMin: 90, score: 'parfait', conflicts: 0 },
    { date: '2026-05-26', start: '21:00', durationMin: 90, score: 'ok',      conflicts: 1 },
  ];

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('conflicts')} />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 4 }}>
          <ColIcon name="sparkle" size={22} color={t.accent} strokeWidth={2} />
          <span style={{ fontSize: 12, color: t.accent, fontWeight: 600, letterSpacing: 0.5, textTransform: 'uppercase' }}>Suggestions</span>
        </div>
        <h1 style={{
          fontFamily: '"Instrument Serif", Georgia, serif',
          fontSize: 32, fontWeight: 400, lineHeight: 1.05,
          margin: '8px 0 6px', letterSpacing: -0.3,
        }}>
          Créneaux libres<br />pour ta commission
        </h1>
        <p style={{ fontSize: 14, color: t.inkMuted, margin: 0, lineHeight: 1.4 }}>
          Aucun conflit pour tous les membres de Sport.
        </p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginTop: 22 }}>
          {slots.map((s, i) => {
            const d = new Date(s.date + 'T00:00');
            const end = addMin(s.start, s.durationMin);
            const perfect = s.conflicts === 0;
            const picked = state.pickedSlot === i;
            return (
              <button key={i} onClick={() => set({ pickedSlot: i })} style={{
                width: '100%', textAlign: 'left', cursor: 'pointer', fontFamily: 'inherit',
                background: picked ? t.accentMuted : t.bgRaised,
                border: `${picked ? 2 : 1}px solid ${picked ? t.accent : t.borderSubtle}`,
                borderRadius: 16, padding: '14px 16px',
                display: 'flex', alignItems: 'center', gap: 14,
              }}>
                <div style={{
                  width: 50, height: 56, borderRadius: 12, background: perfect ? t.accentMuted : t.bgMuted,
                  display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
                }}>
                  <div style={{ fontSize: 10, fontWeight: 700, color: perfect ? t.accentStrong : t.inkMuted, letterSpacing: 0.5, textTransform: 'uppercase' }}>{MONTHS_FR[d.getMonth()].slice(0, 3)}</div>
                  <div style={{ fontSize: 20, fontWeight: 700, color: perfect ? t.accentStrong : t.ink, lineHeight: 1 }}>{d.getDate()}</div>
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ fontSize: 15, fontWeight: 600, color: t.ink, display: 'flex', alignItems: 'baseline', gap: 6 }}>
                    {DAYS_FR[(d.getDay() + 6) % 7]}. <span style={{ fontFamily: '"Geist Mono", monospace' }}>{s.start} → {end}</span>
                  </div>
                  <div style={{ fontSize: 12, color: perfect ? t.accent : t.inkMuted, marginTop: 4, fontWeight: 500 }}>
                    {perfect ? '✓ Zéro conflit' : `${s.conflicts} membre déjà pris`}
                  </div>
                </div>
                {picked && <ColIcon name="check" size={22} color={t.accent} strokeWidth={2.5} />}
              </button>
            );
          })}
        </div>
      </div>
      <div style={{ padding: '12px 16px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label="Choisir ce créneau" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={state.pickedSlot === undefined}
          onClick={() => nav.go('meeting-created')} />
      </div>
    </div>
  );
}

// ── Meeting created success ───────────────────────────────────────────────
function ScrMeetingCreated({ t, nav, state }) {
  const fromSuggestion = state.pickedSlot !== undefined;
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center', padding: '0 24px', textAlign: 'center' }}>
        <div style={{
          width: 88, height: 88, borderRadius: 28, background: t.accentMuted,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
        }}>
          <ColIcon name="check" size={48} color={t.accent} strokeWidth={2.5} />
        </div>
        <h1 style={{
          fontFamily: '"Instrument Serif", Georgia, serif',
          fontSize: 38, fontWeight: 400, margin: '24px 0 8px',
          lineHeight: 1.05, letterSpacing: -0.4,
        }}>Réunion créée.</h1>
        <p style={{ fontSize: 16, color: t.inkMuted, margin: 0, lineHeight: 1.45, maxWidth: 320 }}>
          {fromSuggestion
            ? 'Tous les membres de Sport reçoivent une notification. Aucun conflit.'
            : 'Les 18 membres concernés reçoivent une notification.'}
        </p>
      </div>
      <div style={{ padding: '12px 16px 24px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <ColButton t={t} label="Retour à l'agenda" full size="lg" variant="accent"
          onClick={() => nav.go('agenda')} />
      </div>
    </div>
  );
}

Object.assign(window, {
  ScrCreateMeeting, ScrConflicts, ScrSuggestions, ScrMeetingCreated,
});
