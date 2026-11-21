import { ChangeDetectionStrategy, Component, InjectionToken, Signal, inject } from '@angular/core';

export interface ProfileAttribute {
  label: string;
  value: string;
}

export type GreetingState =
  | {
      kind: 'ready';
      displayName: string;
      workspaceName: string;
      roles: string[];
      attributes: ProfileAttribute[];
      sampleData: boolean;
    }
  | { kind: 'waiting' | 'unavailable' | 'failed' };

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
            <p class="workspace"><strong>Workspace:</strong> {{ greeting.workspaceName }}</p>
            @if (greeting.sampleData) {
              <p class="sample-label">Local sample data</p>
            }
            <div class="profile">
              <section aria-labelledby="workspace-roles">
                <h2 id="workspace-roles">Workspace roles</h2>
                @if (greeting.roles.length) {
                  <ul class="roles" role="list">
                    @for (role of greeting.roles; track role) {
                      <li>{{ role }}</li>
                    }
                  </ul>
                } @else {
                  <p class="empty">No workspace roles to display.</p>
                }
              </section>
              <section aria-labelledby="personal-attributes">
                <h2 id="personal-attributes">Personal attributes</h2>
                @if (greeting.attributes.length) {
                  <dl class="attributes">
                    @for (attribute of greeting.attributes; track $index) {
                      <div>
                        <dt>{{ attribute.label }}</dt>
                        <dd>{{ attribute.value }}</dd>
                      </div>
                    }
                  </dl>
                } @else {
                  <p class="empty">No personal attributes to display.</p>
                }
              </section>
            </div>
          }
          @case ('waiting') {
            <h1 class="status">Waiting for Krista</h1>
          }
          @case ('failed') {
            <h1 class="status">Couldn’t load your greeting. Reopen the agent from its Krista topic.</h1>
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
