import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

// routes are defined in a separate file to keep the app configuration clean and organized.
import { routes } from './app.routes';
import { provideHttpClient } from '@angular/common/http';

// this is the global configuration for the app.
// it is used to provide services and other providers that are needed for the app to run.
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),
  ]
};
