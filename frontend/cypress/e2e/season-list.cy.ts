describe('Season List', () => {
  beforeEach(() => {
    // Mock champions API with delay to test loading spinner
    cy.intercept('GET', '**/api/v1/champions', {
      fixture: 'f1-api/champions.json',
      delay: 1000
    }).as('getChampions');

    // Mock race winners API
    cy.intercept('GET', '**/api/v1/races/*', {
      fixture: 'f1-api/race-winners.json',
      delay: 500
    }).as('getRaceWinners');

    cy.visit('/');
  });

  it('should display loading spinner initially', () => {
    cy.get('app-spinner').should('be.visible');
    cy.get('.spinner-wrapper').should('be.visible');
    cy.contains('Loading seasons...').should('be.visible');
    cy.wait('@getChampions');
  });

  it('should handle errors gracefully', () => {
    // Intercept API call and force an error
    cy.intercept('GET', '**/api/v1/champions', {
      statusCode: 500,
      body: 'Server error',
      delay: 100
    }).as('getChampionsError');
    
    cy.visit('/');
    cy.wait('@getChampionsError');
    cy.contains('Error loading F1 World Champions').should('be.visible');
  });

  it('should expand a season and show race winners', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').first().within(() => {
      cy.get('.view-races-btn').click();
    });
    cy.wait('@getRaceWinners');
    cy.get('.race-list').should('be.visible');
    cy.get('.race-items').should('be.visible');
  });

  it('should highlight champion races', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').first().within(() => {
      cy.get('.view-races-btn').click();
    });
    cy.wait('@getRaceWinners');
    
    // Get the champion's name from the card
    cy.get('.season-card').first().find('h3').invoke('text').then((championName) => {
      // Find race items where the winner matches the champion
      cy.get('.race-items .grid').each(($raceItem) => {
        const raceWinner = $raceItem.find('.col-span-4 .font-semibold').text().trim();
        if (raceWinner === championName.trim()) {
          cy.wrap($raceItem).should('have.class', 'champion-race');
        }
      });
    });
  });

  it('should display current season card', () => {
    cy.wait('@getChampions');
    cy.get('.current-season').should('be.visible');
    cy.contains('SEASON IN PROGRESS').should('be.visible');
    cy.contains('ONGOING').should('be.visible');
  });

  it('should display past seasons with champions', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').not('.current-season').should('have.length.at.least', 1);
  });

  it('should show race winners when expanding past season', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').not('.current-season').first().within(() => {
      cy.get('.view-races-btn').click();
      cy.wait('@getRaceWinners');
      cy.get('.race-list').should('be.visible');
      cy.get('.race-items').should('be.visible');
    });
  });
}); 