import '@vaadin/tooltip/src/vaadin-tooltip.js';
import '@vaadin/polymer-legacy-adapter/style-modules.js';
import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/button/src/vaadin-button.js';
import 'Frontend/generated/jar-resources/buttonFunctions.js';
import '@vaadin/password-field/src/vaadin-password-field.js';
import '@vaadin/notification/src/vaadin-notification.js';
import 'Frontend/generated/jar-resources/flow-component-renderer.js';
import '@vaadin/vertical-layout/src/vaadin-vertical-layout.js';
import '@vaadin/common-frontend/ConnectionIndicator.js';
import '@vaadin/vaadin-lumo-styles/color-global.js';
import '@vaadin/vaadin-lumo-styles/typography-global.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/style.js';
import '@vaadin/vaadin-lumo-styles/vaadin-iconset.js';

const loadOnDemand = (key) => {
  const pending = [];
  if (key === '03004fc23b81a34c7ba5528a329221bb9d05b35574c41c22bfaf170736636f90') {
    pending.push(import('./chunks/chunk-9d5b12d1f1568e3db233ead76407cd3871801462aeb8470355213b5155797297.js'));
  }
  if (key === 'f1d9b6e58fc8377e43a9f2088017ee4f7806fe19b7f0b6f38ccb5bfbdb7b09ee') {
    pending.push(import('./chunks/chunk-765707e316a4fca0963d2662e17d89e1ccdcb93c859d9b9d5c0b40548d09ef39.js'));
  }
  if (key === 'e1eafa5de6b028b090dbd0ed3d52ce58d7488856dd9b09a839f209746471e76d') {
    pending.push(import('./chunks/chunk-4d4b57885b5f51a8906be74d09e4f7183da61cfdc27430918d9ed6056d515957.js'));
  }
  if (key === '8804098e89065201dbad744554cb48bbe6fa2c97de84db599dc156048c6741a0') {
    pending.push(import('./chunks/chunk-2b85016f1cabb717e4158dae3c8677ae41f9291654ee67baa2145b62651a1f1d.js'));
  }
  return Promise.all(pending);
}

window.Vaadin = window.Vaadin || {};
window.Vaadin.Flow = window.Vaadin.Flow || {};
window.Vaadin.Flow.loadOnDemand = loadOnDemand;