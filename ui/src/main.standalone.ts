import { signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';

import { GREETING_STATE, Greeting, GreetingState } from './greeting';

const state = signal<GreetingState>({
  kind: 'ready',
  displayName: 'Alex',
  workspaceName: 'Sample Space',
  sampleData: true,
});

bootstrapApplication(Greeting, {
  providers: [{ provide: GREETING_STATE, useValue: state.asReadonly() }],
}).catch(console.error);
