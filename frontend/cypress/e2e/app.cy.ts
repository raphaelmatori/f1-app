describe('F1 App E2E', () => {
  beforeEach(() => {
    // Mock champions API with delay to test loading spinner
    cy.intercept('GET', '**/api/v1/champions', {
      fixture: 'f1-api/champions.json',
      delay: 500
    }).as('getChampions');

    // Mock race winners API
    cy.intercept('GET', '**/api/v1/races/*', {
      fixture: 'f1-api/race-winners.json'
    }).as('getRaceWinners');

    cy.visit('/');
  });

  it('should load the home page and show header/footer', () => {
    cy.get('app-header').should('exist');
    cy.get('app-footer').should('exist');
    cy.wait('@getChampions');
    cy.contains('Loading seasons...').should('not.exist');
  });

  it('should list seasons and champions', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').should('have.length.at.least', 2);
    cy.get('.season-card').first().within(() => {
      cy.get('h2').should('exist');
      cy.get('h3').should('exist');
      cy.get('button').contains('View Race Winners').should('exist');
    });
  });

  it('should expand a season and show race winners', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').contains('2023').parents('.season-card').within(() => {
      cy.get('button').contains('View Race Winners').click();
    });
    cy.wait(100);
    cy.wait('@getRaceWinners');
    cy.get('.race-list-container.active').should('exist');
    cy.get('.race-list').should('exist');
    cy.get('.race-items').should('exist');
    cy.get('.grid.grid-cols-12').should('exist');
  });

  it('should highlight champion races', () => {
    cy.wait('@getChampions');
    cy.get('.season-card').contains('2023').parents('.season-card').within(() => {
      cy.get('button').contains('View Race Winners').click();
    });
    cy.wait(100);
    cy.wait('@getRaceWinners');
    cy.get('.champion-row').should('exist');
  });

  it('should show error message on API failure', () => {
    cy.intercept('GET', '**/api/v1/champions', {
      statusCode: 500,
      body: { error: 'Internal Server Error' }
    }).as('getChampionsError');
    cy.reload();
    cy.wait('@getChampionsError');
    cy.contains('Error loading F1 World Champions').should('exist');
  });

  it('should be accessible', () => {
    cy.wait('@getChampions');
    // Check if buttons are focusable
    cy.get('button').first().focus().should('be.visible');
    // Check if text is readable
    cy.get('.season-card').contains('2023').parents('.season-card').should('be.visible').within(() => {
      cy.get('h2').should('be.visible');
      cy.get('h3').should('be.visible');
    });
  });
}); 