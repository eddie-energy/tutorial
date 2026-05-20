import { Component, OnInit } from '@angular/core';
import { keycloak } from '../main';
import { FormsModule } from '@angular/forms';
import { ApexChart, ApexXAxis, NgApexchartsModule } from 'ng-apexcharts';

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [FormsModule, NgApexchartsModule],
  templateUrl: './chart.html',
})
export class Chart implements OnInit {
  loading = true;
  error = '';
  series = [];
  from = '';
  to = '';
  interval = '1 hour';

  chart: ApexChart = {
    type: 'line',
    height: 350,
    zoom: {
      type: 'x',
      enabled: true,
      autoScaleYaxis: true,
    },
    animations: {
      enabled: false,
    },
  };
  xaxis: ApexXAxis = {
    type: 'datetime',
  };
  yaxis = {
    title: {
      text: 'Wh',
    },
  };
  tooltip: any = {
    x: {
      format: 'dd MMM yyyy HH:mm',
    },
    y: {
      formatter(value: number) {
        return `${value} Wh`;
      },
    },
  };
  noData = {
    text: 'No consumption data found.',
  };

  ngOnInit() {
    const to = new Date();
    const from = new Date(to);

    from.setDate(from.getDate() - 7);

    this.from = from.toISOString().slice(0, 16);
    this.to = to.toISOString().slice(0, 16);

    void this.loadReadings();

    globalThis.setInterval(() => this.loadReadings(), 10000);
  }

  async loadReadings() {
    this.error = '';

    const params = new URLSearchParams({
      from: new Date(this.from).toISOString(),
      to: new Date(this.to).toISOString(),
      interval: this.interval,
    });

    try {
      const response = await fetch(`http://localhost:8082/api/readings?${params}`, {
        headers: {
          Authorization: `Bearer ${keycloak.token}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Readings request failed with status ${response.status}`);
      }

      this.series = await response.json();
    } catch (err) {
      console.error(err);
      this.error = 'Unable to load readings right now.';
    } finally {
      this.loading = false;
    }
  }
}
