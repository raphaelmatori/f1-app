// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//

declare global {
  namespace Cypress {
    interface Chainable {
      // Add custom commands here
      // login(email: string, password: string): Chainable<void>
      // drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
      // dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
      tab(options?: { shift?: boolean }): Chainable<JQuery<HTMLElement>>
    }
  }
}

// Add tab command
Cypress.Commands.add('tab', { prevSubject: 'optional' }, (subject: JQuery<HTMLElement> | undefined, options = {}) => {
  const focusableElements = 'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])';
  const focusable = subject ? subject.find(focusableElements) : Cypress.$(focusableElements);
  const firstFocusable = focusable.first();
  firstFocusable.focus();
  return firstFocusable;
});

// Prevent TypeScript from reading file as legacy script
export {}; 