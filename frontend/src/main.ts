import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import Keycloak from 'keycloak-js';

export const keycloak = new Keycloak({
  url: 'http://localhost:8888',
  realm: 'tutorial-realm',
  clientId: 'tutorial-client',
});

keycloak
  .init({ onLoad: 'login-required' })
  .then(() => bootstrapApplication(App, appConfig))
  .catch((err) => console.error(err));
