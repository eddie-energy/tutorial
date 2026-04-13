<!--
Most region connectors require some form of registration or manual approval that can take several days.

Region connector candidates:
- ❌ AT -> Requires registration and licensed Ponton Messenger
- ❌ BE -> Requires a Belgian company
- ❌ DK -> Registration requires Danish company
- ❌ ES -> Easy setup but no public test users
- ✔ FR -> Easy setup and sandbox but needs registration that can take days
- ❌ NL -> Has sandbox and test users but needs Dutch company
- CDS and Green Button can do test data without registration but are not EU

Additionally, we cannot provide real metering points or accounts to test with actual data.
Instead, we want to inform readers towards being able to set up a connector on their own for the region they operate in.
We show how to set up our most popular region connectors and then disable them to not conflict with the rest of this tutorial.
-->

# Day 2 — Real Region Connectors

**Goal**:

- Understand how EDDIE integrates with real energy data authorities
- See what registration and configuration each connector requires
- Know how to follow the Operation Manual for your own region

**Estimated time**: 1 hour

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-01.zip)

> [!NOTE]
> If you operate in a supported region and want to connect real meter data, follow the full setup in the [Operation Manual](https://architecture.eddie.energy/framework/) for your region. The simulation connector from Day 1 will remain active for the rest of this tutorial.

## Why region connectors differ

The simulation connector generates data inside EDDIE itself — no external systems involved.
Real region connectors integrate with a Metered Data Administrator (MDA), the regional authority that holds actual meter readings. Each MDA has its own authentication protocol and onboarding process.

EDDIE abstracts these differences behind a common interface. From your application's perspective, every connector produces the same outbound messages regardless of region. What changes is what EDDIE needs from you and your customers to establish that connection.

## Spain — Datadis

TODO

## France — Enedis

The French connector uses OAuth 2.0. When a customer clicks Connect, EDDIE redirects them to the Enedis authorization page. After they approve, EDDIE receives and manages an access token. Your application never handles credentials directly.

The customer-facing flow is seamless. The EP-side setup requires registering with Enedis, including a manual approval step that can take several days.

### Registration

1. Create a user account at [mon-compte.enedis.fr](https://mon-compte.enedis.fr).
2. Log in to [datahub-enedis.fr](https://datahub-enedis.fr), go to **Mon compte → Nouvelle entité**, and sign the _DATA
   CONNECT_ contract. Approval may take a few days.
3. Once approved, go to **Mon compte → Mes applications** and create an application in the **sandbox** environment.
4. Edit the application and switch it to **production**. You will need to provide:
    - A callback URL pointing to your EDDIE instance:
      `https://<your-domain>/region-connectors/fr-enedis/authorization-callback`
    - A URL where Enedis can verify a running Connect button
5. Wait for production approval, then note the **Client ID** and **Client Secret**.

### Configuration

```shell
REGION_CONNECTOR_FR_ENEDIS_ENABLED=true
REGION_CONNECTOR_FR_ENEDIS_BASEPATH=https://gw.ext.prod.api.enedis.fr
REGION_CONNECTOR_FR_ENEDIS_CLIENT_ID=<your-client-id>
REGION_CONNECTOR_FR_ENEDIS_CLIENT_SECRET=<your-client-secret>
# Enedis is rate-limited — do not poll more than once per day
REGION_CONNECTOR_FR_ENEDIS_POLLING=0 0 17 * * *
```

## Austria — EDA

Austria uses the EDA market protocol: an ebXML message exchange routed through a **PontonXP Messenger
** instance which is a licensed commercial middleware.
The permission flow is asynchronous.
Permission requests and responses are delivered as XML Market Messages through the messenger infrastructure rather than via a direct REST call.

### Registration and setup

1. Register as a service provider at [ebutilities.at](https://www.ebutilities.at/registrierung) to obtain a company identification number (e.g.
   `EP100129`).
2. License and install the [PontonXP Messenger](https://www.ponton.de/ponton-x-p-licensing-download). Ponton provides setup support as part of the license.
3. In `config/messenger.xml`, change `<DefaultAdapterId>` from
   `TestAdapter` to something that identifies your adapter, e.g. `Eddie`.
4. Expose the messenger so EDDIE can reach it on port `2600`.

### Configuration

```shell
REGION_CONNECTOR_AT_EDA_ENABLED=true
REGION_CONNECTOR_AT_EDA_ELIGIBLEPARTY_ID=EP123456
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_ENABLED=true
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_ADAPTER_ID=Eddie
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_ADAPTER_VERSION=1.0.0
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_HOSTNAME=<pontonxp-host>
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_PORT=2600
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_API_ENDPOINT=<pontonxp-host>/api
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_FOLDER=/opt/pontonxp
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_USERNAME=<username>
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_PASSWORD=<password>
```

### Dev mode

If you want to explore the Austrian flow without a Ponton license, EDDIE includes a drop-in REST replacement that accepts Market Messages directly:

```shell
REGION_CONNECTOR_AT_EDA_ENABLED=true
REGION_CONNECTOR_AT_EDA_ELIGIBLEPARTY_ID=EP123456
REGION_CONNECTOR_AT_EDA_PONTON_MESSENGER_ENABLED=false
```

> [!WARN]
> Never use dev mode in production.
> It bypasses the message-level security required by the Austrian market protocol.

## What all three connectors have in common

Despite their different protocols, every region connector works the same way from your application's perspective:

- The EDDIE button handles the customer-facing flow — OAuth redirect, dialog, or token entry.
- EDDIE manages credentials, polling, and retries internally.
- Your application receives data through the same outbound connector endpoints, regardless of region.

The connector you enable is invisible to your application code. Only the EDDIE configuration changes.

## Checkpoint

You understand:

- That region connectors require different steps to set up
- That all three connectors produce the same outbound messages your application consumes
- Where to find the full setup guide if you are ready to connect real meter data

## What's next

On day 3 you will explore the many ways how EDDIE can deliver data to your application — the REST, Kafka, and AMQP outbound connectors — and choose the right one for your architecture.