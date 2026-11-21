import { signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';

import { GREETING_STATE, Greeting, GreetingState } from './greeting';

const state = signal<GreetingState>({
  kind: 'ready',
  displayName: 'Alex',
  workspaceName: 'Sample Space',
  roles: ['Agent Builder', 'Workspace Member'],
  attributes: [
    { label: 'Primary email', value: 'alex@example.com' },
    { label: 'Department', value: 'Customer Success' },
    { label: 'Region', value: 'North America' },
  ],
  sampleData: true,
});

bootstrapApplication(Greeting, {
  providers: [{ provide: GREETING_STATE, useValue: state.asReadonly() }],
}).catch(console.error);
