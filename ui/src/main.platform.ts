import { DestroyRef, inject, signal } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';

import { GREETING_STATE, Greeting, GreetingState, ProfileAttribute } from './greeting';

const clientByExtensionOrigin = new Map([
  ['https://extension.solution-labs.eng.krista.app', 'https://solution-labs.eng.krista.app'],
  ['https://extension.us.krista.app', 'https://us.krista.app'],
  ['https://extension.poc.krista.app', 'https://poc.krista.app'],
]);

function isRole(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}

function isProfileAttribute(value: unknown): value is ProfileAttribute {
  return typeof value === 'object' && value !== null &&
    'label' in value && typeof value.label === 'string' && value.label.trim().length > 0 &&
    'value' in value && typeof value.value === 'string' && value.value.trim().length > 0;
}

bootstrapApplication(Greeting, {
  providers: [{
    provide: GREETING_STATE,
    useFactory: () => {
      const state = signal<GreetingState>({ kind: 'unavailable' });
      const parentOrigin = clientByExtensionOrigin.get(location.origin);
      if (window.parent === window || !parentOrigin ||
          !/^\/extension\/api\/[A-Za-z0-9_-]+\/static\/app\/index\.html$/.test(location.pathname)) {
        return state.asReadonly();
      }

      const route = new URL('../../rest/greeting', location.href);
      state.set({ kind: 'waiting' });
      const request = new AbortController();
      const receive = async (event: MessageEvent) => {
        if (event.source !== window.parent || event.origin !== parentOrigin) {
          return;
        }
        const data = event.data;
        if (!data || !Object.hasOwn(data, 'accessToken') || typeof data.accessToken !== 'string' ||
            !/^[\w-]+\.[\w-]+\.[\w-]+$/.test(data.accessToken)) {
          return;
        }

        stopWaiting();
        const requestTimeout = window.setTimeout(() => request.abort(), 12_000);
        try {
          const response = await fetch(route, {
            headers: {
              Authorization: `Bearer ${data.accessToken}`,
              'X-Hello-Client-Origin': parentOrigin,
            },
            mode: 'same-origin',
            credentials: 'omit',
            redirect: 'error',
            cache: 'no-store',
            signal: request.signal,
          });
          if (!response.ok) {
            state.set({ kind: 'failed' });
            return;
          }
          const result = await response.json();
          if (typeof result?.displayName !== 'string' || !result.displayName.trim() ||
              typeof result?.workspaceName !== 'string' || !result.workspaceName.trim()) {
            state.set({ kind: 'failed' });
            return;
          }
          state.set({
            kind: 'ready',
            displayName: result.displayName,
            workspaceName: result.workspaceName,
            roles: Array.isArray(result.roles) ? result.roles.filter(isRole) : [],
            attributes: Array.isArray(result.attributes) ? result.attributes.filter(isProfileAttribute) : [],
            sampleData: false,
          });
        } catch {
          state.set({ kind: 'failed' });
        } finally {
          window.clearTimeout(requestTimeout);
        }
      };

      const announceReady = () => window.parent.postMessage({ type: 'KLAG_IFRAME_READY' }, parentOrigin);
      const ping = window.setInterval(announceReady, 250);
      const timeout = window.setTimeout(() => {
        stopWaiting();
        state.set({ kind: 'unavailable' });
      }, 15_000);
      const stopWaiting = () => {
        window.clearInterval(ping);
        window.clearTimeout(timeout);
        window.removeEventListener('message', receive);
      };

      window.addEventListener('message', receive);
      inject(DestroyRef).onDestroy(() => {
        stopWaiting();
        request.abort();
      });
      announceReady();
      return state.asReadonly();
    },
  }],
}).catch(console.error);
