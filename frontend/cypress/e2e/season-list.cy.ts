describe('Season List', () => {
  beforeEach(() => {
    // Mock race winners API
    cy.intercept('GET', '**/api/v1/races/*', {
      fixture: 'f1-api/race-winners.json',
      delay: 500
    }).as('getRaceWinners');

    cy.visit('/');
  });

  it('should expand a season and show race winners', () => {
    cy.get('.season-card').first().within(() => {
      cy.get('.view-races-btn').click();
    });
    cy.wait('@getRaceWinners');
    cy.get('.race-list').should('be.visible');
    cy.get('.race-items').should('be.visible');
  });

  it('should highlight champion races', () => {
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
    cy.get('.current-season').should('be.visible');
    cy.contains('SEASON IN PROGRESS').should('be.visible');
    cy.contains('ONGOING').should('be.visible');
  });

  it('should display past seasons with champions', () => {
    cy.get('.season-card').not('.current-season').should('have.length.at.least', 1);
  });

  it('should show race winners when expanding past season', () => {
    cy.get('.season-card').not('.current-season').first().within(() => {
      cy.get('.view-races-btn').click();
      cy.wait('@getRaceWinners');
      cy.get('.race-list').should('be.visible');
      cy.get('.race-items').should('be.visible');
    });
  });
}); 