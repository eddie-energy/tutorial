<!--
Goal: User value realization
Activities:
- Integrate ApexCharts in Angular
- Display consumption curves
- Implement time range selection
- Implement dynamic refresh
Outcome: Interactive visualization layer
-->

# Day 12 — Time-Series Visualization

**Goal**:

- Query meter readings efficiently for visualisation
- Display readings per permission as line charts in the frontend
- Let the user change the displayed time range

**Estimated time**: 2h

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-11.zip)

## Step 1 — Query time series data

On Day 7 we added a query for the latest reading per permission to confirm our application logic.
The actual goal is to display meter readings in a chart.

For a chart we need many data points over a selected period.
We could load all rows into Angular and group them there,
but that would move unnecessary work to the browser and send more data than needed.
Instead, we will prepare chart-ready series in PostgreSQL and return them in one request.

Inside the `MeterReadingRepository` add a new query method.
It filters by user and optional date range, groups readings into buckets using `date_bin`,
aggregates the quantity per bucket, and finally returns JSON in the shape ApexCharts expects.
By passing the interval as a request parameter, the same query can aggregate views of different granularities.

```java [MeterReadingRepository.java]
@Query(value = """
        WITH
        filtered AS (
          SELECT
            mr.permission_id,
            mr.timestamp,
            mr.quantity
          FROM meter_reading mr
          WHERE mr.user_id = :userId
            AND (CAST(:from AS timestamp) IS NULL OR mr.timestamp >= :from)
            AND (CAST(:to   AS timestamp) IS NULL OR mr.timestamp <  :to)
        ),
        bucketed AS (
          SELECT
            permission_id,
            date_bin(
              CAST(:interval as interval),
              (timestamp AT TIME ZONE 'UTC'),
              TIMESTAMP '1970-01-01 00:00:00'
            ) AS bucket,
            quantity
          FROM filtered
        ),
        aggregated AS (
          SELECT
            permission_id,
            bucket,
            SUM(quantity) AS total
          FROM bucketed
          GROUP BY permission_id, bucket
        ),
        grouped AS (
          SELECT
            permission_id,
            jsonb_build_object(
              'name', permission_id,
              'data', jsonb_agg(
                jsonb_build_array(bucket::text || 'Z', total)
                ORDER BY bucket
              )
            ) AS series
          FROM aggregated
          GROUP BY permission_id
        )
        SELECT COALESCE(jsonb_agg(series ORDER BY (series ->> 'name')), '[]'::jsonb)::text AS series
        FROM grouped
        """, nativeQuery = true)
String findByUserId(
        @Param("userId") String userId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("interval") String interval);
```

The response of this query looks like this:

```json
[
  {
    "name": "2947c1ec-20d1-4c3e-84a0-0e37e83dd864",
    "data": [
      [
        "2024-12-30T09:00:00",
        10.00
      ]
    ]
  }
]
```

Each item becomes one series in the chart.
The series name is the permission id and the data array contains pairs of timestamp and aggregated quantity.

## Step 2 — Exposing chart-ready readings in the backend

The repository can now build the data, so next we expose it through the service and controller.

Inside `MeterReadingService` add a new method forwarding the query to the repository.

```java [MeterReadingService.java]
String findByUserId(String userId, Instant from, Instant to, String interval) {
    return repository.findByUserId(userId, from, to, interval);
}
```

Inside `MeterReadingController` add a new endpoint.
It uses the authenticated user id from Keycloak, accepts `from`, `to`, and
`interval` as request parameters, and returns JSON directly.

```java [MeterReadingController.java]
@GetMapping(value = "/api/readings", produces = MediaType.APPLICATION_JSON_VALUE)
String readings(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(defaultValue = "1 hour") String interval) {
    return meterReadingService.findByUserId(jwt.getSubject(), from, to, interval);
}
```

At this point the backend can return all chart series for a user in one response.

## Step 3 — Installing ApexCharts in Angular

To render the time series in the frontend we will use ApexCharts.
Install it in the `frontend` folder:

```shell
cd frontend
npm install apexcharts ng-apexcharts
```

## Step 4 — Creating a chart component

We will keep the chart logic in its own Angular component.
Create the file `chart.ts`.

```ts [chart.ts]
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
```

Now add the template in `chart.html`.
It renders **From**, **To**, and **Interval** controls, loading and error messages,
and the container where ApexCharts mounts itself.

```html [chart.html]
<h3>Consumption</h3>

<label>
    From
    <input type="datetime-local" [(ngModel)]="from" (change)="loadReadings()" />
</label>

<label>
    To
    <input type="datetime-local" [(ngModel)]="to" (change)="loadReadings()" />
</label>

<label>
    Interval
    <select [(ngModel)]="interval" (change)="loadReadings()">
        <option value="5 seconds">5 seconds</option>
        <option value="1 minute">1 minute</option>
        <option value="15 minutes">15 minutes</option>
        <option value="1 hour">1 hour</option>
        <option value="1 day">1 day</option>
    </select>
</label>

@if (loading) {
<p>Loading readings...</p>
}

@if (error) {
<p>{{ error }}</p>
}

<apx-chart
        [series]="series"
        [chart]="chart"
        [xaxis]="xaxis"
        [yaxis]="yaxis"
        [tooltip]="tooltip"
        [noData]="noData"
/>
```

This component now does four things:

1. It selects a time window using **From** and **To**.
2. It selects an aggregation interval.
3. It requests aggregated readings from the backend.
4. It lets the user zoom into the loaded data directly in the chart.
5. It refreshes automatically every ten seconds.

## Step 5 — Rendering the chart in the app

To use the new component, import it in `app.ts`.

```ts [app.ts]
import {Component, CUSTOM_ELEMENTS_SCHEMA, OnInit, signal} from '@angular/core';
import {Chart} from './chart';
import {keycloak} from '../main';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [Chart],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    templateUrl: './app.html',
    styleUrl: './app.css',
})
export class App implements OnInit {
```

Then render it near the top of `app.html`.

```html [app.html]
<h1>Tutorial App</h1>

<h2>Hello, {{ name() }}!</h2>

<app-chart />
```

When you reload the frontend, and you have meter readings stored for the current user,
the **Readings** section should now show one line per permission.
Changing **From**, **To**, or **Interval** should request a different aggregation window from the backend.
For a closer inspection of the loaded result, you can also drag inside the chart to zoom.

## Checkpoint

- Your backend returns chart-ready readings in one request
- Your frontend renders readings per permission using ApexCharts
- The user can change **From**, **To**, and **Interval**, including fine intervals such as one minute or five seconds
- The chart supports zooming and refreshes automatically

## What's next

Next we will extend the application beyond historical data and connect near real-time data flows.

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-12.zip)
