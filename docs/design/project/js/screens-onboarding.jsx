// Onboarding screens: Welcome, Create project, Join via code, Confirm project, Select identity, Pick commissions

// ── Welcome / Splash ───────────────────────────────────────────────────────
function ScrWelcome({ t, nav }) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      {/* Logo + brand */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'flex-end', padding: '0 24px 24px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
          <ColLogo t={t} size={56} />
          <div style={{ marginTop: 28 }}>
            <h1 style={{
              fontFamily: '"Instrument Serif", Georgia, serif',
              fontSize: 56, fontWeight: 400, color: t.ink, margin: 0,
              lineHeight: '1.0', letterSpacing: -1,
            }}>L'agenda<br/>
              <em style={{ fontStyle: 'italic' }}>partagé</em> du<br />
              <span style={{ color: t.accent }}>conseil.</span>
            </h1>
          </div>
          <p style={{ fontSize: 16, color: t.inkMuted, marginTop: 18, marginBottom: 0, lineHeight: 1.45, maxWidth: 320 }}>
            Plus jamais deux réunions sur le même créneau. Colision détecte les conflits avant qu'ils n'arrivent.
          </p>
        </div>
      </div>

      <div style={{ padding: '0 20px 32px', display: 'flex', flexDirection: 'column', gap: 10 }}>
        <ColButton t={t} label="Créer un projet" full size="lg" variant="accent" onClick={() => nav.go('create-project')} />
        <ColButton t={t} label="Rejoindre avec un code" full size="lg" variant="secondary" onClick={() => nav.go('join-code')} />
        <div style={{ textAlign: 'center', fontSize: 12, color: t.inkSubtle, marginTop: 10, letterSpacing: 0.2 }}>
          Pas de compte. Pas d'email. Juste un prénom.
        </div>
      </div>
    </div>
  );
}

// Brand logo — two overlapping squares (collision metaphor)
function ColLogo({ t, size = 40, mono = false }) {
  const s = size;
  const inner = s * 0.66;
  const offset = s * 0.17;
  return (
    <div style={{ width: s, height: s, position: 'relative', display: 'inline-block' }}>
      <div style={{ position: 'absolute', left: 0, top: 0, width: inner, height: inner, borderRadius: s * 0.18,
        background: mono ? t.ink : t.accent, mixBlendMode: mono ? 'normal' : 'normal' }} />
      <div style={{ position: 'absolute', left: offset * 2, top: offset * 2, width: inner, height: inner, borderRadius: s * 0.18,
        background: mono ? t.ink : t.conflict, mixBlendMode: 'multiply' }} />
    </div>
  );
}

// ── Create project (name input) ────────────────────────────────────────────
function ScrCreateProject({ t, nav, state, set }) {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('welcome')} />
      <div style={{ flex: 1, padding: '8px 20px 16px', overflow: 'auto' }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: t.inkMuted, letterSpacing: 0.5, textTransform: 'uppercase' }}>Étape 1 sur 3</div>
        <h1 style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 34, fontWeight: 400, lineHeight: 1.1, margin: '8px 0 8px', letterSpacing: -0.4 }}>
          Nom du projet
        </h1>
        <p style={{ fontSize: 15, color: t.inkMuted, margin: '0 0 24px', lineHeight: 1.45 }}>
          Le nom que verront les membres. Tu pourras le modifier plus tard.
        </p>
        <ColTextInput t={t} value={state.draftProject || ''} onChange={v => set({ draftProject: v })} placeholder="Conseil municipal de Saint-Machin" autoFocus />
        <div style={{ marginTop: 12, fontSize: 13, color: t.inkSubtle, lineHeight: 1.4 }}>
          💡 Exemples : « Conseil municipal de … », « AG du club de tennis », « Bureau du CSE Acme »
        </div>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label="Créer le projet" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={!(state.draftProject && state.draftProject.length > 1)}
          onClick={() => nav.go('project-created')} />
      </div>
    </div>
  );
}

// ── Project created — show code ────────────────────────────────────────────
function ScrProjectCreated({ t, nav, state }) {
  const code = 'KQ7H2P';
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('create-project')} />
      <div style={{ flex: 1, padding: '8px 20px 16px', overflow: 'auto', display: 'flex', flexDirection: 'column' }}>
        <div style={{ fontSize: 12, fontWeight: 600, color: t.accent, letterSpacing: 0.5, textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: 6 }}>
          <ColIcon name="check" size={14} color={t.accent} strokeWidth={2.5} /> Projet créé
        </div>
        <h1 style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 30, fontWeight: 400, lineHeight: 1.1, margin: '8px 0 6px', letterSpacing: -0.3 }}>
          {state.draftProject || 'Conseil municipal de Saint-Machin'}
        </h1>
        <p style={{ fontSize: 15, color: t.inkMuted, margin: '0 0 22px', lineHeight: 1.45 }}>
          Partage ce code avec les membres. Ils l'utiliseront pour rejoindre.
        </p>

        {/* Code card */}
        <div style={{
          background: t.bgRaised, border: `1px solid ${t.borderSubtle}`, borderRadius: 20,
          padding: 22, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 14,
          boxShadow: t.shadow2,
        }}>
          <div style={{ fontSize: 11, color: t.inkMuted, letterSpacing: 1.5, textTransform: 'uppercase', fontWeight: 600 }}>
            Code de partage
          </div>
          <div style={{
            display: 'flex', gap: 6,
            fontFamily: '"Geist Mono", monospace', fontSize: 42, fontWeight: 600,
            letterSpacing: 4, color: t.ink,
          }}>{code.split('').map((c, i) => (
            <span key={i} style={{
              width: 38, height: 56, display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              background: t.accentMuted, borderRadius: 10, color: t.accentStrong,
            }}>{c}</span>
          ))}</div>
          <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
            <ColButton t={t} label="Copier" icon="copy" variant="secondary" size="sm" onClick={() => {}} />
            <ColButton t={t} label="Partager" icon="share" variant="secondary" size="sm" onClick={() => {}} />
          </div>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginTop: 26 }}>
          <div style={{ fontSize: 13, color: t.inkMuted, fontWeight: 600, letterSpacing: 0.4, textTransform: 'uppercase' }}>Ensuite</div>
          {[
            { n: 1, label: 'Ajoute tes commissions', sub: 'Jeunesse, Sport, Travaux…' },
            { n: 2, label: 'Liste les membres du conseil', sub: 'Juste leur prénom suffit' },
            { n: 3, label: 'Envoie le code aux autres', sub: 'WhatsApp, SMS, comme tu veux' },
          ].map(s => (
            <div key={s.n} style={{ display: 'flex', gap: 14, alignItems: 'flex-start', padding: '6px 0' }}>
              <div style={{
                width: 26, height: 26, borderRadius: 8, background: t.bgMuted, color: t.ink,
                display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 600, fontSize: 13,
                border: `1px solid ${t.borderSubtle}`, flexShrink: 0,
              }}>{s.n}</div>
              <div>
                <div style={{ fontSize: 15, color: t.ink, fontWeight: 500 }}>{s.label}</div>
                <div style={{ fontSize: 13, color: t.inkMuted }}>{s.sub}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label="Commencer" full size="lg" variant="accent" iconRight="arrow-right"
          onClick={() => nav.go('agenda')} />
      </div>
    </div>
  );
}

// ── Join via code ──────────────────────────────────────────────────────────
function ScrJoinCode({ t, nav, state, set }) {
  const code = state.joinCode || '';
  const setCode = v => set({ joinCode: v.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 6) });
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('welcome')} />
      <div style={{ flex: 1, padding: '8px 20px 16px', overflow: 'auto' }}>
        <h1 style={{ fontFamily: '"Instrument Serif", Georgia, serif', fontSize: 34, fontWeight: 400, lineHeight: 1.1, margin: '8px 0 8px', letterSpacing: -0.4 }}>
          Code à 6 caractères
        </h1>
        <p style={{ fontSize: 15, color: t.inkMuted, margin: '0 0 24px', lineHeight: 1.45 }}>
          Demande-le à la personne qui a créé le projet.
        </p>

        {/* 6 slots */}
        <div style={{ display: 'flex', gap: 8, justifyContent: 'center', padding: '12px 0' }}>
          {Array.from({ length: 6 }).map((_, i) => {
            const ch = code[i] || '';
            const filled = !!ch;
            const focused = i === code.length;
            return (
              <div key={i} style={{
                width: 44, height: 60, borderRadius: 12,
                background: filled ? t.accentMuted : t.bgRaised,
                border: `${focused ? 2 : 1}px solid ${focused ? t.accent : t.borderStrong}`,
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontFamily: '"Geist Mono", monospace', fontSize: 28, fontWeight: 600,
                color: filled ? t.accentStrong : t.inkSubtle,
              }}>{ch || (focused ? '·' : '')}</div>
            );
          })}
        </div>

        {/* Demo keypad */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 8, marginTop: 28, padding: '0 4px' }}>
          {['K','Q','7','H','2','P','⌫'].slice(0, 7).map((k, i) => (
            <button key={i} onClick={() => {
              if (k === '⌫') setCode(code.slice(0, -1));
              else setCode(code + k);
            }} style={{
              height: 56, borderRadius: 14,
              background: t.bgRaised, color: t.ink,
              border: `1px solid ${t.borderSubtle}`,
              fontFamily: '"Geist Mono", monospace', fontSize: 22, fontWeight: 600,
              cursor: 'pointer',
            }}>{k}</button>
          ))}
        </div>
        <div style={{ textAlign: 'center', marginTop: 16, fontSize: 13, color: t.inkSubtle }}>
          Démo — tape <span style={{ color: t.accent, fontFamily: '"Geist Mono", monospace', fontWeight: 600 }}>KQ7H2P</span>
        </div>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label="Continuer" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={code.length !== 6}
          onClick={() => nav.go('confirm-project')} />
      </div>
    </div>
  );
}

// ── Confirm project ────────────────────────────────────────────────────────
function ScrConfirmProject({ t, nav }) {
  const D = window.COLISION_DATA;
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} onBack={() => nav.go('join-code')} />
      <div style={{ flex: 1, padding: '20px 20px 16px', overflow: 'auto', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
        <div style={{
          background: t.bgRaised, borderRadius: 20, padding: 28,
          border: `1px solid ${t.borderSubtle}`, boxShadow: t.shadow2,
          display: 'flex', flexDirection: 'column', gap: 14, alignItems: 'center', textAlign: 'center',
        }}>
          <div style={{ fontSize: 11, color: t.inkMuted, letterSpacing: 1.2, textTransform: 'uppercase', fontWeight: 600 }}>
            Tu vas rejoindre
          </div>
          <h2 style={{
            fontFamily: '"Instrument Serif", Georgia, serif',
            fontSize: 28, fontWeight: 400, margin: 0, lineHeight: 1.15, letterSpacing: -0.3,
          }}>{D.PROJECT.name}</h2>
          <div style={{ display: 'flex', gap: 18, fontSize: 13, color: t.inkMuted, marginTop: 4 }}>
            <span><strong style={{ color: t.ink }}>{D.MEMBERS.length}</strong> membres</span>
            <span style={{ width: 1, background: t.borderSubtle }} />
            <span><strong style={{ color: t.ink }}>{D.COMMISSIONS.length}</strong> commissions</span>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, justifyContent: 'center', marginTop: 8 }}>
            {D.COMMISSIONS.slice(0, 6).map(c => <ColCommissionChip key={c.id} commission={c} size="sm" t={t} />)}
          </div>
        </div>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised, display: 'flex', flexDirection: 'column', gap: 8 }}>
        <ColButton t={t} label="C'est bien le bon" full size="lg" variant="accent" onClick={() => nav.go('select-identity')} />
        <ColButton t={t} label="Pas le bon projet" full size="md" variant="ghost" onClick={() => nav.go('join-code')} />
      </div>
    </div>
  );
}

// ── Select identity ───────────────────────────────────────────────────────
function ScrSelectIdentity({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const selected = state.selfMemberId;
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title="Qui es-tu ?" onBack={() => nav.go('confirm-project')} />
      <div style={{ padding: '0 16px 8px' }}>
        <p style={{ fontSize: 15, color: t.inkMuted, margin: '4px 4px 14px', lineHeight: 1.4 }}>
          Trouve ton nom dans la liste. Tu peux aussi en ajouter un nouveau.
        </p>
      </div>
      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 16px' }}>
        <div style={{
          background: t.bgRaised, borderRadius: 16, border: `1px solid ${t.borderSubtle}`,
          overflow: 'hidden',
        }}>
          {D.MEMBERS.map((m, i) => {
            const isMe = selected === m.id;
            return (
              <button key={m.id} onClick={() => set({ selfMemberId: m.id })} style={{
                display: 'flex', alignItems: 'center', gap: 12, width: '100%',
                padding: '12px 14px', background: isMe ? t.accentMuted : 'transparent',
                border: 'none', borderBottom: i < D.MEMBERS.length - 1 ? `1px solid ${t.border2}` : 'none',
                cursor: 'pointer', textAlign: 'left',
              }}>
                <ColAvatar t={t} name={m.name} size={38} />
                <div style={{ flex: 1, color: t.ink, fontWeight: isMe ? 600 : 500, fontSize: 15 }}>{m.name}</div>
                {isMe && <ColIcon name="check" size={20} color={t.accent} strokeWidth={2.5} />}
              </button>
            );
          })}
        </div>
        <button onClick={() => {}} style={{
          width: '100%', marginTop: 12, padding: '14px 16px', borderRadius: 14,
          background: 'transparent', border: `1px dashed ${t.borderStrong}`,
          color: t.ink, fontSize: 15, fontWeight: 500, cursor: 'pointer',
          display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
          fontFamily: 'inherit',
        }}>
          <ColIcon name="plus" size={18} color={t.ink} /> Ajouter un nouveau membre
        </button>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label="C'est moi" full size="lg" variant="accent" iconRight="arrow-right"
          disabled={!selected}
          onClick={() => nav.go('pick-commissions')} />
      </div>
    </div>
  );
}

// ── Pick commissions ──────────────────────────────────────────────────────
function ScrPickCommissions({ t, nav, state, set }) {
  const D = window.COLISION_DATA;
  const self = D.memberById(state.selfMemberId) || D.memberById('m2');
  const picked = state.selfCommissions || self.commissions;

  const toggle = (id) => {
    const next = picked.includes(id) ? picked.filter(x => x !== id) : [...picked, id];
    set({ selfCommissions: next });
  };

  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: t.bg, color: t.ink, fontFamily: '"Geist", system-ui, sans-serif' }}>
      <ColTopBar t={t} title={self.name.split(' ')[0]} subtitle="Tes commissions" onBack={() => nav.go('select-identity')} />
      <div style={{ padding: '0 20px 6px' }}>
        <p style={{ fontSize: 15, color: t.inkMuted, margin: '4px 0 14px', lineHeight: 1.4 }}>
          Coche celles auxquelles tu participes. Tu pourras changer plus tard.
        </p>
      </div>
      <div style={{ flex: 1, overflow: 'auto', padding: '0 16px 16px' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {D.COMMISSIONS.map(c => {
            const on = picked.includes(c.id);
            const count = D.membersOfCommission(c.id).length;
            return (
              <button key={c.id} onClick={() => toggle(c.id)} style={{
                display: 'flex', alignItems: 'center', gap: 14, padding: '14px 16px',
                borderRadius: 16, background: t.bgRaised,
                border: `${on ? 2 : 1}px solid ${on ? t.accent : t.borderSubtle}`,
                cursor: 'pointer', textAlign: 'left', fontFamily: 'inherit',
              }}>
                <div style={{ width: 36, height: 36, borderRadius: 10, background: c.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                  <div style={{ width: 10, height: 10, borderRadius: '50%', background: c.dot }} />
                </div>
                <div style={{ flex: 1 }}>
                  <div style={{ fontSize: 16, fontWeight: 600, color: t.ink }}>{c.name}</div>
                  <div style={{ fontSize: 12, color: t.inkMuted }}>{count} membres</div>
                </div>
                <div style={{
                  width: 24, height: 24, borderRadius: 8,
                  border: `${on ? 0 : 1.5}px solid ${t.borderStrong}`,
                  background: on ? t.accent : 'transparent',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                }}>
                  {on && <ColIcon name="check" size={16} color="#fff" strokeWidth={2.5} />}
                </div>
              </button>
            );
          })}
        </div>
      </div>
      <div style={{ padding: '12px 20px 24px', borderTop: `1px solid ${t.border2}`, background: t.bgRaised }}>
        <ColButton t={t} label={`Entrer dans l'agenda (${picked.length})`} full size="lg" variant="accent" iconRight="arrow-right"
          onClick={() => nav.go('agenda')} />
      </div>
    </div>
  );
}

Object.assign(window, {
  ScrWelcome, ScrCreateProject, ScrProjectCreated, ScrJoinCode,
  ScrConfirmProject, ScrSelectIdentity, ScrPickCommissions, ColLogo,
});
