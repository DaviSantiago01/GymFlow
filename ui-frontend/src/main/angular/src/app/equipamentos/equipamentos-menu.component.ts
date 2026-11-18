import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-equipamentos-menu',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './equipamentos-menu.component.html',
  styleUrl: './equipamentos.scss',
})
export class EquipamentosMenuComponent {}
