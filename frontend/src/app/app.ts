import { Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { keycloak } from '../main';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  name = signal('stranger');

  ngOnInit() {
    fetch('http://localhost:8082/api/me', {
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
      },
    })
      .then((response) => response.json())
      .then((data) => this.name.set(data.name))
      .catch((err) => console.error(err));
  }
}
