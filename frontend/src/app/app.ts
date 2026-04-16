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
  title = signal('stranger');

  ngOnInit() {
    keycloak.loadUserProfile().then((profile) => {
      this.title.set(profile.firstName!);
    });
  }
}
