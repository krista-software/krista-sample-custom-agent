import { ChangeDetectionStrategy, Component, InjectionToken, Signal, inject } from '@angular/core';

export type GreetingState =
  | { kind: 'ready'; displayName: string; workspaceName: string; sampleData: boolean }
  | { kind: 'waiting' | 'unavailable' };

export const GREETING_STATE = new InjectionToken<Signal<GreetingState>>('Greeting state');

@Component({
  selector: 'hello-greeting',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main>
      <section class="greeting" aria-live="polite" aria-atomic="true">
        @let greeting = state();
        @switch (greeting.kind) {
          @case ('ready') {
            <h1>Hello, {{ greeting.displayName }}</h1>
            <p><strong>Workspace:</strong> {{ greeting.workspaceName }}</p>
            @if (greeting.sampleData) {
              <p class="sample-label">Local sample data</p>
            }
          }
          @case ('waiting') {
            <h1 class="status">Waiting for Krista</h1>
          }
          @case ('unavailable') {
            <h1 class="status">Open this agent from its Krista topic.</h1>
          }
        }
      </section>
    </main>
  `,
})
export class Greeting {
  protected readonly state = inject(GREETING_STATE);
}
