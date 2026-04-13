# Day 1 — First Contact & Simulated Flow

**Goals**

- Run EDDIE locally with Docker Compose
- Configure data needs to retrieve master data and historical data
- Place the EDDIE button on a website
- See your first data arrive via the REST outbound connector

**Estimated time**: 2 hours

## Prerequisites

- [Docker](https://docs.docker.com/get-docker/) with [Docker Compose](https://docs.docker.com/compose/)
- `curl` or any HTTP client

## Step 1 — Create your project directory

Create a directory for your energy application.
You will grow it incrementally over the next 21 days.

```shell
mkdir my-energy-app && cd my-energy-app
```

## Step 2 — Write the Compose file

The standard way of running the EDDIE framework is as a Docker container.

```yaml [docker-compose.yml]
name: eddie-tutorial
services:
  db:
    image: postgres:17-bookworm
    environment:
      POSTGRES_USER: eddie
      POSTGRES_PASSWORD: eddie
      POSTGRES_DB: eddie
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U eddie" ]
      interval: 10s
      timeout: 3s
      retries: 5

  eddie:
    image: ghcr.io/eddie-energy/eddie:latest
    ports:
      - "8080:8080"
      - "9090:9090"
    volumes:
      - ./data-needs.json:/opt/core/config/data-needs.json
    env_file: .env
    depends_on:
      db:
        condition: service_healthy
```

## Step 3 — Write the environment file

First, we will configure our database.

```dotenv
JDBC_URL=jdbc:postgresql://db:5432/eddie
JDBC_USER=eddie
JDBC_PASSWORD=eddie
```

Next, EDDIE requires a secret for signing authentication tokens.
In a real deployment it is important to keep this secret hidden and persistent which is why it intentionally does not have a default value.
You can easily generate a secret string using the following command.

```shell
openssl rand -base64 32
```

```dotenv
EDDIE_JWT_HMAC_SECRET=EZawglo19bseUT94HDE7sQDy2sqbX3IhkU+Mveruj5w=
```

Next we will enable some additional features while disabling one feature that we will only use later.

```dotenv
# We will use the Simulation region connector to generate test data
REGION_CONNECTOR_SIM_ENABLED=true
# We will use the REST outbound connector to confirm the permission process
OUTBOUND_CONNECTOR_REST_ENABLED=true
# The demo button helps us test our region connectors
EDDIE_DEMO_BUTTON_ENABLED=true
# Disable Kafka connector which is otherwise enabled by default
OUTBOUND_CONNECTOR_KAFKA_ENABLED=false
```

Finally, we will pass the path to our config file that will soon hold our data needs.

```dotenv
EDDIE_DATA_NEEDS_CONFIG_FILE=./config/data-needs.json
```

Our full `.env` file should now look like this.

```dotenv [.env]
JDBC_URL=jdbc:postgresql://db:5432/eddie
JDBC_USER=eddie
JDBC_PASSWORD=eddie

EDDIE_JWT_HMAC_SECRET=EZawglo19bseUT94HDE7sQDy2sqbX3IhkU+Mveruj5w=

# We will use the Simulation region connector to generate test data
REGION_CONNECTOR_SIM_ENABLED=true
# We will use the REST outbound connector to confirm the permission process
OUTBOUND_CONNECTOR_REST_ENABLED=true
# The demo button helps us test our region connectors
EDDIE_DEMO_BUTTON_ENABLED=true

# Disable Kafka connector which is otherwise enabled by default
OUTBOUND_CONNECTOR_KAFKA_ENABLED=false

EDDIE_DATA_NEEDS_CONFIG_FILE=./config/data-needs.json
```

## Step 4 — Define your data needs

A **data need** describes what kind of data you want to request from a customer.
It acts as the template for a permissions request — the customer sees its description and purpose in the EDDIE dialog before agreeing.

```json
[
  {
    "type": "account",
    "id": "00000001-0000-0000-0000-000000000001",
    "name": "Accounting point master data",
    "description": "Information about your metering point and contract",
    "purpose": "Retrieve metering point details for energy services",
    "policyLink": "https://example.com/privacy"
  },
  {
    "type": "validated",
    "id": "00000001-0000-0000-0000-000000000002",
    "name": "Last 3 months daily",
    "description": "Your electricity consumption for the past 3 months, aggregated daily",
    "purpose": "Retrieve historical consumption data for analytics and cost reporting",
    "policyLink": "https://example.com/privacy",
    "duration": {
      "type": "relativeDuration",
      "start": "-P3M",
      "end": "P0D"
    },
    "energyType": "ELECTRICITY",
    "minGranularity": "P1D",
    "maxGranularity": "P1D"
  }
]
```

You are configuring both data needs now so their IDs stay stable across all 21 days.
The Simulation connector supports the `validated` type.
Accounting point data from real connectors is covered on Day 2.

Full reference: [Data Needs](https://architecture.eddie.energy/framework/2-integrating/data-needs.html)

## Step 5 — Start EDDIE

```shell
docker compose up -d
docker compose logs -f eddie
```

The container may require some time to boot.
If it starts without errors you should be able to open http://localhost:8080/demo to verify EDDIE is running.
You should see the built-in demo page with a Connect button.

<!-- TODO: screenshot of demo page -->

## Step 6 — Embed the EDDIE Connect button

The EDDIE button is a standard HTML custom element served directly from your EDDIE instance.
This allows you to easily embed it into any website just like the demo page.

```html [index.html]
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>My Energy App</title>
    <script src="http://localhost:8080/lib/eddie-components.js" type="module"></script>
</head>
<body>
<h1>My Energy App</h1>

<eddie-connect-button connection-id="1" data-need-id="1"></eddie-connect-button>
</body>
</html>
```

Let's take a look at the attributes of the EDDIE button.

- `connection-id` links the button instance to the created permission and the data that EDDIE delivers for it.
  This could be a session or user id.
- `data-need-id` specifies the data need used for permissions created via the EDDIE button.

There are many more attributes for customizing the EDDIE button that we will explore on later days.

## Step 7 — Walk through the permission flow

On the [demo page](http://localhost:8080/demo) or your own website, click the **Connect with EDDIE** button.

1. Confirm the data need information
2. Select **Simulation** as your country and continue
3. Click **Launch Simulation** to open the simulation UI

The UI of the simulation connector allows you to generate permission events and test data.

<!-- TODO: Screenshot of simulation UI -->

## Step 8 — Trigger the simulation and observe data

First, we will simulate the creation of a permission for our connection.
Find the **Connection Status** container and select the **CREATED** status.
Press **Submit** to confirm the connection status.

Now we will use the REST outbound connector to track our connection status:
http://localhost:9090/outbound-connectors/rest/agnostic/connection-status-messages.
You can simply open the API endpoint in your browser, or use an HTTP client like `curl`.

> [!NOTE]
> Browsers will usually request the data as XML, while HTTP clients will often use JSON. This tutorial will use JSON for brevity.

The result should include a message like this,
indicating that the permission has transitioned into the **CREATED** state.

```json
{
  "connectionId": "1",
  "permissionId": "pm::00000000-0000-0000-0000-000000000001::1",
  "dataNeedId": "00000000-0000-0000-0000-000000000001",
  "dataSourceInformation": {
    "countryCode": "DE",
    "meteredDataAdministratorId": "sim",
    "permissionAdministratorId": "sim",
    "regionConnectorId": "sim"
  },
  "timestamp": "2026-04-02T11:08:06.330945148Z",
  "status": "CREATED",
  "message": "CREATED",
  "additionalInformation": null
}
```

Back at the simulation UI we will now find the **Meter Readings** container.
Click the **Randomize** button and then the **Submit** button to publish some test data to our outbound connector.

The REST endpoint for retrieving validated historical data is
http://localhost:9090/outbound-connectors/rest/cim_0_82/validated-historical-data-md

Upon inspection, you should find a message like this.

```json
{
  "MessageDocumentHeader": {
    "creationDateTime": "2026-04-02T11:32:18Z",
    "MessageDocumentHeader_MetaInformation": {
      "connectionid": "1",
      "permissionid": "pm::00000000-0000-0000-0000-000000000001::1",
      "dataNeedid": "00000000-0000-0000-0000-000000000001",
      "dataType": "validated-historical-data-market-document",
      "MessageDocumentHeader_Region": {
        "connector": "sim",
        "country": "NDE"
      }
    }
  },
  "ValidatedHistoricalData_MarketDocument": {
    "TimeSeriesList": {
      "TimeSeries": [
        {
          "Series_PeriodList": {
            "Series_Period": [
              {
                "timeInterval": {
                  "start": "2026-04-01T22:00Z",
                  "end": "2026-04-02T22:00Z"
                },
                "resolution": "PT1H",
                "PointList": {
                  "Point": [
```

You will notice that on line 5 these messages reference your connection ID as `"connectionId": "1"`.
This is how you will match incoming data to a specific user on Day 6.

## Checkpoint

- [ ] `docker compose ps` shows `db` and `eddie` as healthy/running
- [ ] The EDDIE button is visible on the demo page and shows the Simulation connector
- [ ] `curl .../connection-status-messages` returns at least one message with connection ID `1`
- [ ] `curl .../validated-historical-data-md` returns simulated consumption data

## What's next

On day 2 you will configure some real region connectors (Denmark, France, Austria) and observe how the same button and data needs work across countries.

- [Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-01.zip)