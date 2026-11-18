import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EquipamentosMenuComponent } from './equipamentos-menu.component';
import { Registro, dia, rotulo } from './equipamentos.service';

@Component({
  selector: 'app-registro-equipamento',
  imports: [RouterLink, EquipamentosMenuComponent],
  templateUrl: './registro-equipamento.component.html',
  styleUrl: './equipamentos.scss',
})
export class RegistroEquipamentoComponent {
  readonly registro = inject(ActivatedRoute).snapshot.data['item'] as Registro;
  readonly dia = dia;
  readonly rotulo = rotulo;
}
