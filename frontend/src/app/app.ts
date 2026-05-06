import { Component, CUSTOM_ELEMENTS_SCHEMA, OnInit, signal } from '@angular/core';
import { keycloak } from '../main';

@Component({
  selector: 'app-root',
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  name = signal('stranger');
  userId = signal('');
  connections = signal<{ id: string; permissionId: string; status: string }[]>([]);
  latestReadings = signal<Map<string, string>>(new Map());

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
        void this.updateLatestReadings();
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

  async updateLatestReadings() {
    const response = await fetch('http://localhost:8082/api/readings/latest', {
      headers: {
        Authorization: `Bearer ${keycloak.token}`,
      },
    });

    const readings = await response.json();
    const mapped = new Map();

    for (const { permissionId, quantity, timestamp } of readings) {
      mapped.set(
        permissionId,
        `Latest reading on ${new Date(timestamp).toLocaleString()}: ${quantity} Wh`,
      );
    }

    this.latestReadings.set(mapped);
  }
}
