import { DestroyRef, inject, signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';

import { GREETING_STATE, Greeting, GreetingState } from './greeting';

bootstrapApplication(Greeting, {
  providers: [{
    provide: GREETING_STATE,
    useFactory: () => {
      const state = signal<GreetingState>({ kind: 'unavailable' });
      if (window.parent !== window) {
        state.set({ kind: 'waiting' });
        const timeout = window.setTimeout(() => state.set({ kind: 'unavailable' }), 5000);
        inject(DestroyRef).onDestroy(() => window.clearTimeout(timeout));
      }
      return state.asReadonly();
    },
  }],
}).catch(console.error);
