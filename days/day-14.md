<!--
Goal: Introduce streaming acquisition
Activities:
- Create AIIDA outbound data need
- Explore available real-time connectors
    - Austrian energy adapter
    - Smart gateway adapter
    - Linky
- Understand hosting models (cloud vs on-prem)
Outcome: Real-time acquisition capability
-->

### Day 14 — Real-Time Data via AIIDA

**Goal**:

- Set up an AIIDA cloud instance for your users
- Allow users to connect near real-time data using your AIIDA instance
- Understand AIIDA hosting models and how the communicate with EDDIE

**Estimated time**: 2h

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-13.zip)

## Step 1 — What is AIIDA?

The Administrative Interface for In-house Data Access (AIIDA) connects to various metering devices such as smart meters and home automation systems,
to stream near real-time energy data and other data to consumers like the EDDIE framework.

While EDDIE and AIIDA are closely related and even share the same repository they are not usually deployed on the same network or server.

![AIIDA and EDDIE](../resources/day-14/aiida-simple.drawio.svg)

For this tutorial we will assume a role in which we operate both the EDDIE framework and an AIIDA cloud instance.

## Step 2 — Running AIIDA

To better differentiate the infrastructure code of our AIIDA instance from our existing EDDIE setup
we will create a new `aiida` folder to place all our AIIDA configuration in.
In this folder, we will create a new `docker-compose.yml` file just for our AIIDA services.

AIIDA needs three things to run: a Timescale database, an MQTT broker, and an authentication service.
We can reuse the same Keycloak instance our application is using.
For the database, the broker, and AIIDA, we will add new services to our `aiida/docker-compose.yml`.

```yaml [aiida/docker-compose.yml
name: eddie-tutorial
services:
```

### Database

AIIDA saves its data to a [Timescale](https://github.com/timescale/timescaledb) database.
Timescale is a PostgreSQL extension specifically designed for time series data.

```yaml [aiida/docker-compose.yml
  aiida-db:
    image: timescale/timescaledb:latest-pg17
    ports:
      - "5433:5432"
    environment:
      POSTGRES_USER: aiida
      POSTGRES_PASSWORD: aiida
      POSTGRES_DB: aiida
    volumes:
      - ./db.sql:/docker-entrypoint-initdb.d/db.sql:ro
    restart: always
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U aiida" ]
      interval: 10s
      timeout: 3s
      retries: 3
```

We will create a separate user with limited privileges for our message broker to store authentication information.
You can find more information on this setup in our [AIIDA documentation](https://architecture.eddie.energy/aiida/1-running/database.html#emqx-user).
For this tutorial, we will simply add a new user in the `aiida/db.sql` file that we mount as a volume in our services.

```sql [aiida/db.sql]
CREATE USER emqx WITH ENCRYPTED PASSWORD 'aiida';
```

### MQTT Broker

```yaml [aiida/docker-compose.yml]
  aiida-emqx:
    image: emqx/emqx:5.8.6
    depends_on:
      aiida-db:
        condition: service_healthy
    ports:
      - "1884:1883"
      - "8884:8883"
      - "18084:18083"
    environment:
      EMQX_AUTHENTICATION__1__DATABASE: aiida
      EMQX_AUTHENTICATION__1__SERVER: aiida-db:5432
      EMQX_AUTHENTICATION__1__PASSWORD: aiida
      EMQX_AUTHORIZATION__SOURCES__1__DATABASE: aiida
      EMQX_AUTHORIZATION__SOURCES__1__SERVER: aiida-db:5432
      EMQX_AUTHORIZATION__SOURCES__1__PASSWORD: aiida
    restart: always
    volumes:
      - ./emqx.hocon:/opt/emqx/etc/base.hocon:ro
      - ./init-user.json:/opt/emqx/data/init-user.json:ro
    healthcheck:
      test: [ "CMD", "/opt/emqx/bin/emqx", "ctl", "status" ]
      interval: 5s
      timeout: 25s
      retries: 5
```

Download from the EDDIE repository into your aiida folder:

- [emqx.hacon](https://github.com/eddie-energy/eddie/blob/main/aiida/docker/emqx/emqx.hocon)
- [init-user.json](https://github.com/eddie-energy/eddie/blob/main/aiida/docker/emqx/init-user.json)

Adjust the password inside the `init-user.json` file to `aiida`

```json [aiida/init-user.json]
[
  {
    "user_id": "aiida",
    "password": "aiida",
    "is_superuser": true
  }
]
```

For detailed information on how to set up an EMQX broker in production,
you may again refer to the [AIIDA documentation](https://architecture.eddie.energy/aiida/1-running/emqx.html).

### AIIDA

With its dependencies set up we now define AIIDA container.

```yaml [aiida/docker-compose.yml]
  aiida:
    image: ghcr.io/eddie-energy/aiida:latest
    depends_on:
      aiida-db:
        condition: service_healthy
      aiida-emqx:
        condition: service_healthy
    env_file:
      - .env
    ports:
      - "8081:8080"
```

Similar to our EDDIE instance, we will keep a separate `aiida/.env` file for its configuration.

```shell
AIIDA_EXTERNAL_HOST=http://localhost:8081

SPRING_DATASOURCE_URL=jdbc:postgresql://aiida-db:5432/aiida
SPRING_DATASOURCE_USERNAME=aiida
SPRING_DATASOURCE_PASSWORD=aiida

MQTT_INTERNAL_HOST=tcp://aiida-emqx:1883
MQTT_EXTERNAL_HOST=tcp://localhost:1884
MQTT_PASSWORD=aiida

KEYCLOAK_INTERNAL_HOST=http://keycloak:8080
KEYCLOAK_EXTERNAL_HOST=http://localhost:8888
KEYCLOAK_REALM=tutorial-realm
KEYCLOAK_CLIENT=tutorial-client
```

### Keycloak

We also need to adapt our Keycloak realm to allow the AIIDA UI at http://localhost:8081 as origin and for redirects.
This can be done via the [Keycloak UI](http://localhost:8888/admin/master/console/#/tutorial-realm/clients) or by adjusting our configuration.

```json [keycloak.json]
{
  "realm": "tutorial-realm",
  "clients": [
    {
      "redirectUris": [
        "http://localhost:4200/*",
        "http://localhost:8081/*"
      ],
      "webOrigins": [
        "http://localhost:4200",
        "http://localhost:8081"
      ],
      ...
```

![Allow AIIDA UI in Keycloak tutorial client](../resources/day-14/aiida-keycloak.png)

### Include compose file

Finally, we will include our `aiida/docker-compose.yml` in our root `docker-compose.yml`.
That way we can have EDDIE and AIIDA in the same network and manage them from the same compose environment.

```yaml [docker-compose.yml]
name: eddie-tutorial
include:
  - aiida/docker-compose.yml
services:
  ...
```

Before we continue with the EDDIE connection, we will check our AIIDA setup in isolation.
For this you will need to start Keycloak in addition to our AIIDA services:

```shell
docker compose up -d keycloak aiida-db aiida-emqx aiida
```

Once all containers are running visit http://localhost:8081/ for the AIIDA UI.

## Step 3 — Adding AIIDA data sources

Before we can exchange any data, we need to connect a measuring device.
AIIDA supports various devices as "Data Sources".
For this tutorial, we will again use a simulation data source to generate test data.
We will however explore some actual real-time connectors to showcase AIIDA capabilities.

![Screenshot of a simulation data source](../resources/day-14/simulation-data-source.png)

## Step 4 — Connecting AIIDA and EDDIE

In EDDIE, AIIDA instances are connected by an AIIDA region connector.

The AIIDA region connector also requires an MQTT broker.

```shell
  eddie-emqx:
    image: emqx/emqx:5.8.6
    ports:
      - "1883:1883"
      - "8883:8883"
      - "18083:18083"
    environment:
      EMQX_AUTHENTICATION__1__DATABASE: eddie
      EMQX_AUTHENTICATION__1__SERVER: db:5432
      EMQX_AUTHENTICATION__1__PASSWORD: eddie
      EMQX_AUTHORIZATION__SOURCES__1__DATABASE: eddie
      EMQX_AUTHORIZATION__SOURCES__1__SERVER: db:5432
      EMQX_AUTHORIZATION__SOURCES__1__PASSWORD: eddie
    volumes:
      - ./emqx.hocon:/opt/emqx/etc/base.hocon
      - ./init-user.json:/tmp/init-user.json
```

Similar to AIIDA's MQTT broker, EDDIE's instance also adds some configuration for authentication.
You can again download the configuration files from the EDDIE repository and place them in the root folder.

- [emqx.hocon](https://github.com/eddie-energy/eddie/blob/main/env/emqx/emqx.hocon)
- [init-user.json](https://github.com/eddie-energy/eddie/blob/main/env/emqx/init-user.json)

For the `init-user.json` we again adjust the password:

```json [init-user.json]
[
  {
    "user_id": "eddie",
    "password": "eddie",
    "is_superuser": true
  }
]
```

Now in the EDDIE database, we also want to add a new `emqx` user with limited access.
In our root `db.sql` file we will add one line of SQL.
If you want to keep your existing data you can just run the SQL on the database.

```sql [db.sql]
CREATE USER emqx WITH ENCRYPTED PASSWORD 'eddie';
```

The region connector itself is configured via environment variables.
Inside EDDIE's `.env` file in the root folder:

```dotenv [.env]
# AIIDA region connector
REGION_CONNECTOR_AIIDA_ENABLED=true
REGION_CONNECTOR_AIIDA_CUSTOMER_ID=tutorial
REGION_CONNECTOR_AIIDA_BCRYPT_STRENGTH=10
REGION_CONNECTOR_AIIDA_MQTT_SERVER_URI=tcp://eddie-emqx:1883
REGION_CONNECTOR_AIIDA_MQTT_USERNAME=eddie
REGION_CONNECTOR_AIIDA_MQTT_PASSWORD=eddie
```

A full list of configuration options and instructions for production deployments are found in the [EDDIE framework documentation](https://architecture.eddie.energy/framework/1-running/region-connectors/region-connector-aiida.html).

A common pitfall in local setups is that AIIDA will not be able to reach EDDIE via localhost.

```yaml [aiida/docker-compose.yml]
services:
  aiida:
    extra_hosts:
      - "localhost:host-gateway"
```

## Checkpoint

- First key takeaway
- Second key takeaway

## What's next

Next we will cover...

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-14.zip)