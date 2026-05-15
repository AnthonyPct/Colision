// Mock data for Colision — "Conseil municipal de Saint-Machin"

window.COLISION_DATA = (function () {
  const PROJECT = {
    name: 'Conseil municipal de Saint-Machin',
    code: 'KQ7H2P',
    createdAt: '2026-01-12',
  };

  // Curated palette mapping. Same order as commissions for stable assignment.
  const COMMISSION_PALETTE = [
    { bg: '#E6F0EC', ink: '#0E5446', dot: '#0E7C66' }, // teal
    { bg: '#FBE9DF', ink: '#7E3614', dot: '#C9582B' }, // rust
    { bg: '#E8DDF2', ink: '#42235E', dot: '#7B3FE4' }, // purple
    { bg: '#F2E7BE', ink: '#5C4A12', dot: '#B8956A' }, // wheat
    { bg: '#E0EEDC', ink: '#2D4523', dot: '#5C8049' }, // sage
    { bg: '#DCE8F5', ink: '#1F3C66', dot: '#3B5F8F' }, // sky
    { bg: '#F5D6D8', ink: '#6E1F26', dot: '#A8323F' }, // rose
    { bg: '#EBEDDC', ink: '#4A4D24', dot: '#7E8439' }, // olive
  ];

  const COMMISSIONS = [
    { id: 'c1', name: 'Jeunesse',  ...COMMISSION_PALETTE[1] },
    { id: 'c2', name: 'Sport',     ...COMMISSION_PALETTE[0] },
    { id: 'c3', name: 'Travaux',   ...COMMISSION_PALETTE[2] },
    { id: 'c4', name: 'Culture',   ...COMMISSION_PALETTE[3] },
    { id: 'c5', name: 'École',     ...COMMISSION_PALETTE[4] },
    { id: 'c6', name: 'Finances',  ...COMMISSION_PALETTE[5] },
    { id: 'c7', name: 'Plénier',   ...COMMISSION_PALETTE[6] },
  ];

  // Members (light list — enough to feel real)
  const MEMBERS = [
    { id: 'm1',  name: 'Antoine Berger',     commissions: ['c3', 'c6']            },
    { id: 'm2',  name: 'Sophie Picquet',     commissions: ['c1', 'c2', 'c5']      },
    { id: 'm3',  name: 'Marc Lemoine',       commissions: ['c2', 'c7']            },
    { id: 'm4',  name: 'Pierre Garnier',     commissions: ['c2', 'c3']            },
    { id: 'm5',  name: 'Léa Dubois',         commissions: ['c2', 'c7', 'c4']      },
    { id: 'm6',  name: 'Camille Roux',       commissions: ['c1', 'c5']            },
    { id: 'm7',  name: 'Julien Mercier',     commissions: ['c4', 'c6']            },
    { id: 'm8',  name: 'Nadia Benali',       commissions: ['c5', 'c6', 'c7']      },
    { id: 'm9',  name: 'Thomas Vidal',       commissions: ['c3', 'c2']            },
    { id: 'm10', name: 'Claire Fournier',    commissions: ['c1', 'c4']            },
    { id: 'm11', name: 'Hugo Lefèvre',       commissions: ['c2', 'c7']            },
    { id: 'm12', name: 'Aïcha Khelifa',      commissions: ['c6', 'c7']            },
    { id: 'm13', name: 'Patrick Blanc',      commissions: ['c3', 'c5']            },
    { id: 'm14', name: 'Élodie Martel',      commissions: ['c1', 'c4', 'c5']      },
    { id: 'm15', name: 'Bastien Carré',      commissions: ['c2', 'c3', 'c7']      },
    { id: 'm16', name: 'Sandrine Petit',     commissions: ['c6']                  },
    { id: 'm17', name: 'Yann Olivier',       commissions: ['c4', 'c7']            },
    { id: 'm18', name: 'Manon Girard',       commissions: ['c5', 'c1']            },
  ];

  // ISO week starting Mon 18 May 2026. Use 2026 dates that match PRD (mai 2026).
  const MEETINGS = [
    // PAST + this week
    { id: 'r1', title: 'Budget jeunesse 2026',      commissions: ['c1'],       date: '2026-05-18', start: '18:30', durationMin: 90,  createdBy: 'm6' },
    { id: 'r2', title: 'Préparation kermesse',      commissions: ['c5'],       date: '2026-05-19', start: '19:00', durationMin: 60,  createdBy: 'm14' },
    { id: 'r3', title: 'Coordination tournoi',      commissions: ['c2'],       date: '2026-05-21', start: '20:00', durationMin: 90,  createdBy: 'm3' },
    { id: 'r4', title: 'Conseil plénier — vote PLU',commissions: ['c7'],       date: '2026-05-28', start: '19:00', durationMin: 180, createdBy: 'm1' },
    { id: 'r5', title: 'Travaux rue de l\u2019\u00c9glise', commissions: ['c3'], date: '2026-05-20', start: '18:00', durationMin: 75, createdBy: 'm1' },
    { id: 'r6', title: 'Saison culturelle 2026-27', commissions: ['c4'],       date: '2026-05-22', start: '20:30', durationMin: 60,  createdBy: 'm7' },
    { id: 'r7', title: 'Budget école — cantine',    commissions: ['c5', 'c6'], date: '2026-05-25', start: '18:30', durationMin: 90,  createdBy: 'm8' },

    // NEXT week — used to demo conflict on May 26 19h00 (Sport vs Jeunesse)
    { id: 'r8', title: 'Commission Jeunesse',       commissions: ['c1'],       date: '2026-05-26', start: '19:00', durationMin: 90,  createdBy: 'm6' },
    { id: 'r9', title: 'Conseil plénier — bilan',   commissions: ['c7'],       date: '2026-06-04', start: '19:30', durationMin: 120, createdBy: 'm1' },
  ];

  // Pending arbitration on Sophie's side
  const PENDING_ARBITRATIONS = [
    {
      id: 'a1',
      conflictMeetingId: 'r10', // the one Marc creates in the demo flow
      againstMeetingId: 'r8',
      memberId: 'm2', // Sophie
    },
  ];

  return {
    PROJECT,
    COMMISSIONS,
    MEMBERS,
    MEETINGS,
    PENDING_ARBITRATIONS,
    paletteFor: function (commissionId) {
      const c = COMMISSIONS.find(x => x.id === commissionId);
      return c || COMMISSION_PALETTE[0];
    },
    memberById: function (id) { return MEMBERS.find(m => m.id === id); },
    commissionById: function (id) { return COMMISSIONS.find(c => c.id === id); },
    membersOfCommission: function (commissionId) {
      return MEMBERS.filter(m => m.commissions.includes(commissionId));
    },
  };
})();
