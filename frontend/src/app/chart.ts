import { AfterViewInit, Component, ElementRef, OnInit, signal, ViewChild } from '@angular/core';
import ApexCharts from 'apexcharts';
import { keycloak } from '../main';
import { FormsModule } from '@angular/forms';

type Range = 'day' | 'week' | 'month' | 'year';
type Readings = { name: string; data: [string, number][] }[];

const RANGES = {
  day: { days: 1, interval: '15 minutes' },
  week: { days: 7, interval: '1 hour' },
  month: { days: 30, interval: '1 day' },
  year: { days: 365, interval: '1 day' },
};

@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chart.html',
})
export class Chart implements OnInit, AfterViewInit {
  loading = signal(true);
  error = signal('');
  series = signal<Readings>([]);

  range: Range = 'week';

  @ViewChild('chart')
  private readonly chartElement!: ElementRef;

  private chart?: ApexCharts;

  ngOnInit() {
    void this.loadReadings();

    globalThis.setInterval(() => this.loadReadings(), 10000);
  }

  ngAfterViewInit() {
    this.chart = new ApexCharts(this.chartElement.nativeElement, {
      chart: {
        type: 'line',
        height: 350,
      },
      series: this.series(),
      xaxis: {
        type: 'datetime',
      },
      yaxis: {
        title: {
          text: 'Wh',
        },
      },
      noData: {
        text: 'No readings found.',
      },
    });

    this.chart.render();
  }

  async loadReadings() {
    this.loading.set(true);
    this.error.set('');

    const to = new Date();
    const from = new Date(to);

    const { days, interval } = RANGES[this.range];

    from.setDate(from.getDate() - days);

    const params = new URLSearchParams({
      from: from.toISOString(),
      to: to.toISOString(),
      interval,
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

      const series = await response.json();
      this.series.set(series);

      await this.chart?.updateSeries(series, true);
    } catch (err) {
      console.error(err);
      this.error.set('Unable to load readings right now.');
    } finally {
      this.loading.set(false);
    }
  }
}
