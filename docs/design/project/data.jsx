// data.jsx — Mock data for the prototype

const PROJECT = {
  name: 'Conseil municipal de Saint-Machin',
  code: 'KQ7H2P',
  membersCount: 19,
  createdBy: 'Antoine',
};

const COMMISSIONS = [
  { id: 'c1', name: 'Jeunesse',  emoji: '🎒', color: '#E5D9F9', accent: '#7B3FE4' },
  { id: 'c2', name: 'Sport',     emoji: '⚽', color: '#DBE5FA', accent: '#3F5BD9' },
  { id: 'c3', name: 'Travaux',   emoji: '🔧', color: '#F8E6CC', accent: '#C77F1A' },
  { id: 'c4', name: 'Culture',   emoji: '🎭', color: '#FCE0E8', accent: '#C73E70' },
  { id: 'c5', name: 'École',     emoji: '🏫', color: '#D0EAEE', accent: '#0F7C8A' },
  { id: 'c6', name: 'Finances',  emoji: '💼', color: '#E5EBD3', accent: '#5C7A18' },
  { id: 'c7', name: 'Conseil plénier', emoji: '🏛️', color: '#E8E2D3', accent: '#5A4A2C' },
];

const MEMBERS = [
  { id: 'm1',  name: 'Antoine Roy',      commissions: ['c3','c6'] },
  { id: 'm2',  name: 'Sophie Picquet',   commissions: ['c1','c2','c5'], me: true },
  { id: 'm3',  name: 'Marc Bernard',     commissions: ['c2','c7'] },
  { id: 'm4',  name: 'Pierre Garnier',   commissions: ['c3','c2'] },
  { id: 'm5',  name: 'Léa Dubois',       commissions: ['c4','c7'] },
  { id: 'm6',  name: 'Camille Morel',    commissions: ['c1','c4'] },
  { id: 'm7',  name: 'Julien Vidal',     commissions: ['c5','c6'] },
  { id: 'm8',  name: 'Hélène Faure',     commissions: ['c1','c5','c7'] },
  { id: 'm9',  name: 'Thomas Lefèvre',   commissions: ['c3','c6','c7'] },
  { id: 'm10', name: 'Nadia Benali',     commissions: ['c4','c5'] },
  { id: 'm11', name: 'Olivier Caron',    commissions: ['c2','c3'] },
  { id: 'm12', name: 'Élise Tanguy',     commissions: ['c1','c6'] },
  { id: 'm13', name: 'Bruno Lemoine',    commissions: ['c7'] },
  { id: 'm14', name: 'Inès Martel',      commissions: ['c4','c7'] },
  { id: 'm15', name: 'Vincent Aubry',    commissions: ['c2','c5'] },
  { id: 'm16', name: 'Karine Joly',      commissions: ['c1','c3'] },
  { id: 'm17', name: 'Romain Pasquier',  commissions: ['c6'] },
  { id: 'm18', name: 'Florence Aubin',   commissions: ['c4','c5','c7'] },
  { id: 'm19', name: 'David Rolland',    commissions: ['c2','c6'] },
];

// Today = wed 20 may 2026 — meetings around this date
// time in 24h HH:mm, duration in minutes
const MEETINGS = [
  { id: 'r1', title: 'Budget jeunesse 2026', date: '2026-05-20', time: '19:00', duration: 90, commissions: ['c1'], creator: 'm6' },
  { id: 'r2', title: 'Visite chantier école', date: '2026-05-21', time: '17:30', duration: 60, commissions: ['c3','c5'], creator: 'm1' },
  { id: 'r3', title: 'Tournoi inter-quartiers', date: '2026-05-21', time: '20:00', duration: 90, commissions: ['c2'], creator: 'm3', hasConflict: true },
  { id: 'r4', title: 'Festival communal', date: '2026-05-22', time: '19:00', duration: 75, commissions: ['c4'], creator: 'm5' },
  { id: 'r5', title: 'Préparation kermesse', date: '2026-05-25', time: '18:30', duration: 60, commissions: ['c5','c1'], creator: 'm8' },
  { id: 'r6', title: 'Conseil plénier — délibérations', date: '2026-05-28', time: '20:00', duration: 120, commissions: ['c7'], creator: 'm1', important: true },
  { id: 'r7', title: 'Point voirie urgent', date: '2026-05-23', time: '10:00', duration: 60, commissions: ['c3'], creator: 'm4' },
  { id: 'r8', title: 'Coordination Sport-Travaux', date: '2026-05-26', time: '19:00', duration: 60, commissions: ['c2','c3'], creator: 'm3', conflicted: true },
];

const PENDING_ARBITRATIONS = [
  {
    id: 'arb1',
    note: "Marc t'a invité à une réunion qui chevauche un autre engagement.",
    meetingA: { id: 'r3', title: 'Tournoi inter-quartiers', commission: 'c2', date: '2026-05-21', time: '20:00', duration: 90, organizer: 'Marc Bernard', membersGoing: 6 },
    meetingB: { id: 'r1b', title: 'Budget jeunesse 2026', commission: 'c1', date: '2026-05-21', time: '20:00', duration: 90, organizer: 'Camille Morel', membersGoing: 5 },
  },
];

// Free slot suggestions when creating a conflicting meeting
const SUGGESTED_SLOTS = [
  { date: '2026-05-19', time: '19:00', duration: 90, free: 'all', label: 'Mardi 19 mai' },
  { date: '2026-05-20', time: '20:30', duration: 90, free: 'all', label: 'Mercredi 20 mai' },
  { date: '2026-05-24', time: '10:00', duration: 90, free: 'all', label: 'Samedi 24 mai' },
];

const MONTHS_FR = ['janvier','février','mars','avril','mai','juin','juillet','août','septembre','octobre','novembre','décembre'];
const DAYS_FR = ['lun.','mar.','mer.','jeu.','ven.','sam.','dim.'];
const DAYS_FR_FULL = ['lundi','mardi','mercredi','jeudi','vendredi','samedi','dimanche'];

function fmtDate(iso) {
  const [y, m, d] = iso.split('-').map(Number);
  const dt = new Date(y, m - 1, d);
  const dow = (dt.getDay() + 6) % 7; // mon=0
  return { dow, day: d, month: m - 1, year: y, weekday: DAYS_FR[dow], weekdayFull: DAYS_FR_FULL[dow], monthName: MONTHS_FR[m - 1] };
}

function commissionById(id) { return COMMISSIONS.find(c => c.id === id); }
function memberById(id) { return MEMBERS.find(m => m.id === id); }

// Members in conflict for a hypothetical meeting being created (used in screens)
const NEW_MEETING_CONFLICTS = [
  { member: 'Sophie Picquet', commission: 'c1', meeting: 'Budget jeunesse 2026', time: '20:00 - 21:30' },
  { member: 'Pierre Garnier', commission: 'c3', meeting: 'Point voirie urgent (décalé)', time: '19:30 - 21:00' },
  { member: 'Léa Dubois', commission: 'c7', meeting: 'Conseil plénier — délibérations', time: '20:00 - 22:00' },
];

Object.assign(window, {
  PROJECT, COMMISSIONS, MEMBERS, MEETINGS, PENDING_ARBITRATIONS, SUGGESTED_SLOTS,
  NEW_MEETING_CONFLICTS, MONTHS_FR, DAYS_FR, DAYS_FR_FULL,
  fmtDate, commissionById, memberById,
});
