import { Component, input, output } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { criterios, Criterio, Escolhas } from './avaliacoes-professor.service';
@Component({
  selector: 'app-criterios',
  imports: [DecimalPipe],
  styleUrl: './criterios.component.scss',
  template: `
    @for (c of criterios; track c.chave) {
      @if (editavel()) {
        <fieldset [attr.aria-describedby]="erros()[c.chave] ? 'erro-' + c.chave : null">
          <legend>{{ c.nome }} <span aria-hidden="true">*</span></legend>
          @if (c.chave === 'adequacao') {
            <p class="ajuda">
              Quanto maior a nota, mais adequada a dificuldade às suas necessidades.
            </p>
          }
          <div class="notas">
            @for (n of [0, 1, 2, 3, 4, 5]; track n) {
              <label [class.escolhida]="valores()[c.chave] === n">
                <input
                  type="radio"
                  [name]="c.chave"
                  [value]="n"
                  [checked]="valores()[c.chave] === n"
                  [disabled]="ocupado()"
                  required
                  (change)="escolher.emit({ chave: c.chave, nota: n })"
                />
                <span>{{ n }}</span>
              </label>
            }
          </div>
          @if (erros()[c.chave]) {
            <p class="erro" [id]="'erro-' + c.chave">{{ erros()[c.chave] }}</p>
          }
        </fieldset>
      } @else {
        <div class="criterio">
          <div class="rotulo">
            <span>{{ c.nome }}</span
            ><strong>{{
              valores()[c.chave] === null
                ? 'Sem avaliações'
                : (valores()[c.chave] | number: '1.1-1') + ' / 5'
            }}</strong>
          </div>
          @if (valores()[c.chave] !== null) {
            <progress max="5" [value]="valores()[c.chave]" [attr.aria-label]="c.nome"></progress>
          }
        </div>
      }
    }
  `,
})
export class CriteriosComponent {
  readonly criterios = criterios;
  readonly valores = input.required<Escolhas>();
  readonly editavel = input(false);
  readonly ocupado = input(false);
  readonly erros = input<Record<string, string>>({});
  readonly escolher = output<{ chave: Criterio; nota: number }>();
}
