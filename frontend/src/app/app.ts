import { Component, CUSTOM_ELEMENTS_SCHEMA, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { keycloak } from '../main';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  name = signal('stranger');
  userId = signal('');
  connections = signal<{ id: string; permissionId: string; status: string }[]>([]);

  ngOnInit() {
    fetch('http://localhost:8082/api/me', {
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
      },
    })
      .then((response) => response.json())
      .then((data) => {
        this.name.set(data.name);
        this.userId.set(data.id);

        void this.updateConnections();
      })
      .catch((err) => console.error(err));
  }

  async updateConnections() {
    const response = await fetch('http://localhost:8082/api/connections', {
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
      },
    });

    const data = await response.json();

    this.connections.set(data);
  }
}
