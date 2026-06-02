// app.jsx — Root App component, navigation, and the side-by-side host

const NAV_ITEMS = [
  { key: 'home', label: 'Agenda', icon: <Icon.Calendar size={22}/> },
  { key: 'commissions', label: 'Commissions', icon: <Icon.Folder size={22}/> },
  { key: 'members', label: 'Membres', icon: <Icon.Users size={22}/> },
  { key: 'project', label: 'Projet', icon: <Icon.User size={22}/> },
];

// Map nav keys to bottom tab keys when reachable
function tabFromScreen(s) {
  if (['home','meeting-detail','arbitration','organizer-status','create-meeting','conflicts','suggestions'].includes(s)) return 'home';
  if (['commissions','commission-detail'].includes(s)) return 'commissions';
  if (s === 'members') return 'members';
  if (s === 'project') return 'project';
  return null;
}

// Screens that don't show the bottom nav
const FULLSCREEN = new Set([
  'welcome','create-project','join-code','join-confirm',
  'join-identity','join-commissions','notification-perm',
  'arbitration','create-meeting','conflicts','suggestions',
]);

// ────────────────────────────────────────────────────────────
// App — shared between iOS and Android frames
// ────────────────────────────────────────────────────────────
function App({ theme, state, setState, safeTop = 0, safeBottom = 0 }) {
  const { screen, params, agendaView, showPush, fakePush } = state;

  const nav = (next, p = {}) => setState(s => ({ ...s, screen: next, params: p, showPush: false }));
  const setAgendaView = (v) => setState(s => ({ ...s, agendaView: v }));

  const screens = {
    'welcome': <WelcomeScreen theme={theme} nav={nav}/>,
    'create-project': <WelcomeScreen theme={theme} nav={nav}/>,
    'join-code': <JoinCodeScreen theme={theme} nav={nav}/>,
    'join-confirm': <JoinConfirmScreen theme={theme} nav={nav}/>,
    'join-identity': <JoinIdentityScreen theme={theme} nav={nav}/>,
    'join-commissions': <JoinCommissionsScreen theme={theme} nav={nav}/>,
    'notification-perm': <NotificationPermScreen theme={theme} nav={nav}/>,
    'home': <HomeScreen theme={theme} nav={nav} view={agendaView} setView={setAgendaView} onCreate={() => nav('create-meeting')}/>,
    'commissions': <CommissionsScreen theme={theme} nav={nav}/>,
    'commission-detail': <CommissionDetailScreen theme={theme} nav={nav} params={params}/>,
    'create-meeting': <CreateMeetingScreen theme={theme} nav={nav}/>,
    'conflicts': <ConflictsScreen theme={theme} nav={nav}/>,
    'suggestions': <SuggestionsScreen theme={theme} nav={nav}/>,
    'meeting-detail': <MeetingDetailScreen theme={theme} nav={nav} params={params}/>,
    'arbitration': <ArbitrationScreen theme={theme} nav={nav}/>,
    'organizer-status': <OrganizerStatusScreen theme={theme} nav={nav}/>,
    'members': <MembersScreen theme={theme} nav={nav}/>,
    'project': <ProjectScreen theme={theme} nav={nav}/>,
  };

  const tab = tabFromScreen(screen);
  const showNav = !FULLSCREEN.has(screen) && tab;

  return (
    <div style={{
      flex: 1, display: 'flex', flexDirection: 'column',
      background: theme.background, position: 'relative', overflow: 'hidden',
    }}>
      {safeTop > 0 && <div style={{ height: safeTop, flexShrink: 0, background: theme.background }}/>}
      <div style={{ flex: 1, minHeight: 0, display: 'flex', flexDirection: 'column' }}>
        {screens[screen] || screens['welcome']}
      </div>
      {showNav && (
        <div style={{ paddingBottom: safeBottom, background: theme.surface }}>
          <BottomNav theme={theme} items={NAV_ITEMS} active={tab}
            onChange={(k) => nav(k)}/>
        </div>
      )}
      {showPush && (
        <PushNotification theme={theme}
          title="Conflit détecté sur ton agenda"
          body="Marc a créé « Tournoi inter-quartiers » qui chevauche ta réunion Jeunesse."
          onTap={() => setState(s => ({ ...s, screen: 'arbitration', showPush: false }))}/>
      )}
    </div>
  );
}

Object.assign(window, { App, NAV_ITEMS });
