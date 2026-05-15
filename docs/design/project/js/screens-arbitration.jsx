// Arbitration & settings screens.

// ── Arbitration (Sophie's view: she's been pushed-notified about Marc's conflicting meeting) ──
function ScrArbitration({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  // Two meetings:
  //  - Marc's "Réunion Sport — point urgent" 2026-05-26 19:00 → 20:30
  //  - Sophie's existing "Commission Jeunesse" 2026-05-26 19:00 → 20:30
  const meetingA = {
    title: 'Réunion Sport — point urgent',
    date: '2026-05-26', start: '19:00', end: '20:30',
    commission: D.commissionById('c2'),
    organizer: D.memberById('m3'),
    membersGoing: 7,
  };
  const meetingB = {
    title: 'Commission Jeunesse — budget 2026',
    date: '2026-05-26', start: '19:00', end: '20:30',
    commission: D.commissionById('c1'),
    organizer: D.memberById('m6'),
    membersGoing: 5,
  };
  const choice = state.arbitrationChoice;
  const setChoice = (c) => set({ arbitrationChoice: c });

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} right={
        <button onClick={() => nav.go('agenda')} style={{ width: 40, height: 40, borderRadius: 999, background: 'transparent', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <ColIcon name="x" size={22} color={t.ink} />
        </button>
      } />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 20px 16px' }}>
        <div>
          <div style={{ fontSize: 12, fontWeight: 600, color: t.conflict, letterSpacing: 0.5, textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 6 }}>
            <ColIcon name="warn" size={14} color={t.conflict} strokeWidth={2.5} /> Conflit à arbitrer
          </div>
          <h1 style={{
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 30, fontWeight: 400, lineHeight: 1.1, margin: '8px 0 4px', letterSpacing: -0.3,
          }}>Tu es attendue<br />à deux endroits.</h1>
          <p style={{ fontSize: 14, color: t.inkMuted, margin: '8px 0 0', lineHeight: 1.45 }}>
            Mardi 26 mai · 19h–20h30. Choisis où tu vas. Les deux organisateurs seront prévenus.
          </p>
        </div>

        <div style={{ marginTop: 22, display: 'flex', flexDirection: 'column', gap: 12 }}>
          <ArbitrationOption t={t} meeting={meetingA} selected={choice === 'A'} onSelect={() => setChoice('A')} />
          <div style={{
            display: 'flex', alignItems: 'center', gap: 12, margin: '2px 0',
          }}>
            <div style={{ flex: 1, height: 1, background: t.borderSubtle }} />
            <span style={{
              fontFamily: '"Instrument Serif", Georgia, serif', fontStyle: 'italic',
              fontSize: 14, color: t.inkMuted, letterSpacing: 0.4,
            }}>ou</span>
            <div style={{ flex: 1, height: 1, background: t.borderSubtle }} />
          </div>
          <ArbitrationOption t={t} meeting={meetingB} selected={choice === 'B'} onSelect={() => setChoice('B')} />
        </div>
      </div>
      <div style={{ padding: '12px 16px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <ColButton t={t} label="Valider mon choix" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={!choice} onClick={() => { set({ pendingArbitration: false, arbitrationChoice: undefined }); nav.go('agenda'); }} />
        <ColButton t={t} label="Je trancherai plus tard" full size="md" variant="ghost" onClick={() => nav.go('agenda')} />
      </div>
    </div>
  );
}

function ArbitrationOption({ t, meeting, selected, onSelect }) {
  const c = meeting.commission;
  return (
    <button onClick={onSelect} style={{
      width: '100%', textAlign: 'left', cursor: 'pointer', fontFamily: 'inherit',
      background: selected ? c.bg : t.bgRaised,
      border: `${selected ? 2 : 1}px solid ${selected ? c.dot : t.borderSubtle}`,
      borderRadius: 18, padding: '16px 16px 14px', position: 'relative',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
        <ColCommissionChip commission={c} size="sm" t={t} />
        {selected && (
          <span style={{
            marginLeft: 'auto', width: 24, height: 24, borderRadius: '50%',
            background: c.dot, color: '#fff',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}><ColIcon name="check" size={16} color="#fff" strokeWidth={2.5} /></span>
        )}
      </div>
      <div style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 22, fontWeight: 400, color: t.ink, letterSpacing: -0.2, lineHeight: 1.15 }}>{meeting.title}</div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginTop: 8, fontSize: 13, color: t.inkMuted }}>
        <span style={{ fontFamily: '"Geist Mono", monospace', fontWeight: 600, color: t.ink }}>{meeting.start} → {meeting.end}</span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10, fontSize: 12, color: t.inkMuted }}>
        <ColAvatar t={t} name={meeting.organizer.name} size={20} />
        <span>Organisé par <strong style={{ color: t.ink }}>{meeting.organizer.name.split(' ')[0]}</strong></span>
        <span>·</span>
        <span>{meeting.membersGoing} y vont</span>
      </div>
    </button>
  );
}

// ── More / Settings tab ───────────────────────────────────────────────────
function ScrMore({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const me = D.memberById(state.selfMemberId) || D.memberById('m2');
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title="Plus" large />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 100px' }}>
        {/* Profile card */}
        <div style={{
          background: t.bgRaised, borderRadius: 18, border: `1px solid ${t.borderSubtle}`,
          padding: 16, display: 'flex', alignItems: 'center', gap: 14, marginBottom: 16,
        }}>
          <ColAvatar t={t} name={me.name} size={52} />
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 17, fontWeight: 600, color: t.ink }}>{me.name}</div>
            <div style={{ fontSize: 12, color: t.inkMuted, marginTop: 2 }}>{me.commissions.length} commissions</div>
          </div>
          <button style={{ padding: '8px 14px', borderRadius: 999, background: t.bgMuted, color: t.ink, border: `1px solid ${t.borderSubtle}`, cursor: 'pointer', fontFamily: 'inherit', fontSize: 13, fontWeight: 500 }}>
            Modifier
          </button>
        </div>

        {/* Project info */}
        <ColSectionLabel t={t}>Projet</ColSectionLabel>
        <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, overflow: 'hidden', marginTop: 4 }}>
          <RowItem t={t} icon="home" title={D.PROJECT.name} subtitle={`${D.MEMBERS.length} membres · ${D.COMMISSIONS.length} commissions`} />
          <Sep t={t} />
          <RowItem t={t} icon="share" title="Code de partage" subtitle={D.PROJECT.code} mono />
          <Sep t={t} />
          <RowItem t={t} icon="users" title="Membres" subtitle="Voir, ajouter, modifier" onClick={() => nav.go('members')} />
        </div>

        <ColSectionLabel t={t} action={null}>App</ColSectionLabel>
        <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, overflow: 'hidden', marginTop: 4 }}>
          <RowItem t={t} icon="bell" title="Notifications" right={<Switch t={t} on />} />
          <Sep t={t} />
          <RowItem t={t} icon="settings" title="Préférences" />
        </div>

        <ColSectionLabel t={t}>Quitter</ColSectionLabel>
        <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, overflow: 'hidden', marginTop: 4 }}>
          <RowItem t={t} icon="logout" title="Quitter ce projet" danger />
        </div>

        <div style={{ textAlign: 'center', marginTop: 24, fontSize: 11, color: t.inkSubtle, letterSpacing: 0.4 }}>
          Colision · v1.0 · Made with care
        </div>
      </div>
      <ColBottomNav t={t} tab="more" onTab={tab => set({ tab })} />
    </div>
  );
}

function RowItem({ t, icon, title, subtitle, right, danger, mono, onClick }) {
  return (
    <button onClick={onClick} style={{
      display: 'flex', alignItems: 'center', gap: 14, width: '100%',
      padding: '14px 16px', background: 'transparent', border: 'none',
      cursor: onClick ? 'pointer' : 'default', textAlign: 'left',
      fontFamily: 'inherit',
    }}>
      <div style={{
        width: 32, height: 32, borderRadius: 10, flexShrink: 0,
        background: danger ? t.conflictBg : t.bgMuted,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
      }}>
        <ColIcon name={icon} size={18} color={danger ? t.conflict : t.ink} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontSize: 15, fontWeight: 500, color: danger ? t.conflict : t.ink }}>{title}</div>
        {subtitle && <div style={{
          fontSize: 12, color: t.inkMuted, marginTop: 1,
          fontFamily: mono ? '"Geist Mono", monospace' : 'inherit',
          letterSpacing: mono ? 2 : 0,
        }}>{subtitle}</div>}
      </div>
      {right ? right : <ColIcon name="chevron-right" size={18} color={t.inkSubtle} />}
    </button>
  );
}
function Sep({ t }) { return <div style={{ height: 1, background: t.border2, marginLeft: 62 }} />; }
function Switch({ t, on }) {
  return (
    <div style={{
      width: 44, height: 26, borderRadius: 999,
      background: on ? t.accent : t.borderStrong, position: 'relative',
      transition: 'background 120ms',
    }}>
      <div style={{
        position: 'absolute', top: 2, left: on ? 20 : 2,
        width: 22, height: 22, borderRadius: '50%', background: '#fff',
        boxShadow: '0 1px 3px rgba(0,0,0,0.15)', transition: 'left 120ms',
      }} />
    </div>
  );
}

// ── Members list ──────────────────────────────────────────────────────────
function ScrMembers({ t, nav, state }) {
  const D = window.COLISION_DATA;
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title="Membres" onBack={() => nav.go('more')} right={
        <button style={{ width: 40, height: 40, borderRadius: 999, background: 'transparent', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <ColIcon name="plus" size={22} color={t.ink} />
        </button>
      } />
      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 16px' }}>
        <div style={{ background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`, overflow: 'hidden' }}>
          {D.MEMBERS.map((m, i) => (
            <div key={m.id} style={{
              display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px',
              borderBottom: i < D.MEMBERS.length - 1 ? `1px solid ${t.border2}` : 'none',
            }}>
              <ColAvatar t={t} name={m.name} size={38} />
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 15, fontWeight: 500, color: t.ink }}>
                  {m.name}
                  {state.selfMemberId === m.id && <span style={{ fontSize: 10, fontWeight: 600, background: t.accentMuted, color: t.accentStrong, padding: '2px 6px', borderRadius: 999, letterSpacing: 0.4, marginLeft: 8 }}>MOI</span>}
                </div>
                <div style={{ display: 'flex', gap: 4, marginTop: 4, flexWrap: 'wrap' }}>
                  {m.commissions.map(cid => {
                    const c = D.commissionById(cid);
                    return <span key={cid} style={{
                      fontSize: 10, padding: '2px 7px', borderRadius: 999,
                      background: c.bg, color: c.ink, fontWeight: 600, letterSpacing: 0.3,
                    }}>{c.name}</span>;
                  })}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

// ── Push notification overlay (demo) ───────────────────────────────────────
function PushNotification({ t, onTap, platform = 'ios' }) {
  if (platform === 'ios') {
    return (
      <div onClick={onTap} style={{
        position: 'absolute', top: 56, left: 12, right: 12, zIndex: 100,
        borderRadius: 22, padding: '12px 14px',
        background: 'rgba(255,255,255,0.78)',
        backdropFilter: 'blur(20px) saturate(180%)',
        WebkitBackdropFilter: 'blur(20px) saturate(180%)',
        boxShadow: '0 8px 32px rgba(0,0,0,0.18), 0 0 0 0.5px rgba(0,0,0,0.08)',
        cursor: 'pointer',
        fontFamily: '-apple-system, system-ui',
        display: 'flex', gap: 10, alignItems: 'flex-start',
        animation: 'colSlideIn 360ms cubic-bezier(.2,.9,.3,1.1)',
      }}>
        <div style={{ width: 38, height: 38, borderRadius: 8, background: t.accent, color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
          <ColLogo t={t} size={24} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#000', letterSpacing: -0.1 }}>COLISION</span>
            <span style={{ fontSize: 12, color: '#666' }}>maintenant</span>
          </div>
          <div style={{ fontSize: 14, fontWeight: 600, color: '#000', marginTop: 2 }}>Conflit sur ton agenda</div>
          <div style={{ fontSize: 13, color: '#333', marginTop: 1, lineHeight: 1.3 }}>
            Marc a créé « Réunion Sport » qui chevauche ton créneau Jeunesse de 19h.
          </div>
        </div>
      </div>
    );
  }
  // Android variant
  return (
    <div onClick={onTap} style={{
      position: 'absolute', top: 12, left: 8, right: 8, zIndex: 100,
      borderRadius: 16, padding: '12px 14px',
      background: '#fff',
      boxShadow: '0 4px 12px rgba(0,0,0,0.18)',
      cursor: 'pointer',
      fontFamily: 'Roboto, system-ui',
      animation: 'colSlideIn 360ms cubic-bezier(.2,.9,.3,1.1)',
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 11, color: '#444', marginBottom: 4 }}>
        <div style={{ width: 14, height: 14, borderRadius: 3, background: t.accent }} />
        <span style={{ fontWeight: 500 }}>Colision</span>
        <span>· maintenant</span>
      </div>
      <div style={{ fontSize: 14, fontWeight: 500, color: '#000', marginBottom: 2 }}>Conflit sur ton agenda</div>
      <div style={{ fontSize: 13, color: '#444', lineHeight: 1.3 }}>
        Marc a créé « Réunion Sport » qui chevauche ton créneau Jeunesse de 19h.
      </div>
    </div>
  );
}

Object.assign(window, {
  ScrArbitration, ScrMore, ScrMembers, PushNotification,
});
