import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import ApexCharts from 'apexcharts';
import { keycloak } from '../main';

type MeterReadingSeries = {
  name: string;
  data: [string, number][];
};

type RangeKey = 'day' | 'week' | 'month';

const REFRESH_INTERVAL_MS = 60_000;

@Component({
  selector: 'app-chart',
  standalone: true,
  templateUrl: './chart.html',
})
export class Chart implements OnInit, AfterViewInit, OnDestroy {
  selectedRange = signal<RangeKey>('week');
  loading = signal(true);
  error = signal('');
  series = signal<MeterReadingSeries[]>([]);

  @ViewChild('chart')
  private readonly chartElement!: ElementRef;

  private chart?: ApexCharts;
  private refreshInterval?: number;

  ngOnInit() {
    void this.loadReadings();

    this.refreshInterval = globalThis.setInterval(() => {
      void this.loadReadings();
    }, REFRESH_INTERVAL_MS);
  }

  ngAfterViewInit() {
    this.chart = new ApexCharts(this.chartElement.nativeElement, {
      chart: {
        type: 'line',
        height: 350,
      },
      series: this.series().map(({ name, data }) => ({ name, data })),
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

  ngOnDestroy() {
    if (this.refreshInterval !== undefined) {
      globalThis.clearInterval(this.refreshInterval);
    }

    this.chart?.destroy();
  }

  onRangeChange(event: Event) {
    const target = event.target;

    if (!(target instanceof HTMLSelectElement)) {
      return;
    }

    if (target.value === 'day' || target.value === 'week' || target.value === 'month') {
      this.selectedRange.set(target.value);
      void this.loadReadings();
    }
  }

  async loadReadings() {
    this.loading.set(true);
    this.error.set('');

    const to = new Date();
    const from = new Date(to);
    let interval = '1 hour';

    if (this.selectedRange() === 'day') {
      from.setDate(from.getDate() - 1);
      interval = '15 minutes';
    }

    if (this.selectedRange() === 'week') {
      from.setDate(from.getDate() - 7);
      interval = '1 hour';
    }

    if (this.selectedRange() === 'month') {
      from.setDate(from.getDate() - 30);
      interval = '1 day';
    }

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

      const series = (await response.json()) as MeterReadingSeries[];

      this.series.set(series);

      await this.chart?.updateSeries(
        series.map(({ name, data }) => ({ name, data })),
        true,
      );
    } catch (err) {
      console.error(err);
      this.error.set('Unable to load readings right now.');
    } finally {
      this.loading.set(false);
    }
  }
}
