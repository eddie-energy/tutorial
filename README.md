<!--
- What is EDDIE?
- What are we building?
- What do you need?
- Where do I start?
-->

# A step-by-step tutorial to the EDDIE Framework

This repository contains a 21-day tutorial for developers who want to build an energy data application with the [EDDIE Framework](https://eddie.energy).
You follow one day at a time, keep a working checkpoint after each step, and end with a small but realistic application built with Spring, Angular, Keycloak, PostgreSQL, and later Kafka.

## What is EDDIE?

[EDDIE](https://eddie.energy) gives your application a unified way to request and receive energy data from different Metered Data Administrators (MDAs) across the European Union.

Instead of integrating each regional process yourself, you configure EDDIE with:

- one or more **region connectors** implementing the specific process of a data provider
- one or more **outbound connectors** implementing the way your application wants to receive data (REST, Kafka, AMQP)
- one or more **data needs** describing the data you want to receive

On the user-facing side, your application embeds the **EDDIE Connect button**.
When a customer grants permission, EDDIE handles the underlying consent and connector workflow and delivers permission updates and energy data back to your application.

If you want the full platform reference while working through the tutorial, use the public documentation:

- [EDDIE Operation Manual](https://architecture.eddie.energy/framework/)
- [AIIDA Operation Manual](https://architecture.eddie.energy/aiida/)
- [Architecture Documentation](https://architecture.eddie.energy/architecture/)

## What are we building?

Over the course of the tutorial, you build a minimal eligible party application that gradually grows from a simple EDDIE integration into a more complete reference solution.

The planned end result is:

- **Backend**: Java with Spring Boot
- **Frontend**: Angular
- **Authentication**: Keycloak
- **Database**: PostgreSQL
- **Messaging**: Kafka

By Day 21, you should understand how to:

- run EDDIE locally with Docker Compose
- define and use data needs
- guide a user through the permission lifecycle
- retrieve historical and real-time energy data
- receive data through REST first and Kafka later
- connect energy data flows to authenticated users
- persist and visualise data in your application
- explore operational topics such as observability, security, and packaging

## How the tutorial is organised

The tutorial is split into daily chapters under [`days/`](days).
Each day is designed to be self-contained:

- it states a clear goal
- it links to a starting checkpoint
- it walks you through the required steps
- it ends with a checkpoint you can verify

The repository is also designed around checkpoint branches such as `day-01`, `day-02`, and so on.
Those branches represent the expected code state at the end of a day.

Current published day guides:

| Day                     | Topic                                                                     |
|-------------------------|---------------------------------------------------------------------------|
| [Day 1](days/day-01.md) | First contact with EDDIE, simulation connector, first REST flow           |
| [Day 2](days/day-02.md) | Real region connectors and onboarding expectations                        |
| [Day 3](days/day-03.md) | Outbound connector strategy: REST, Kafka, and AMQP                        |
| [Day 4](days/day-04.md) | Permission lifecycle, revocation, termination, and tracing                |
| [Day 5](days/day-05.md) | Application skeleton with Spring, Angular, Keycloak, and the EDDIE button |

The broader learning path follows four phases:

1. **Foundation**: local setup, connectors, and data needs
2. **Integration**: consent lifecycle, outbound channels, and process understanding
3. **Application development**: authentication, persistence, UI, and visualisation
4. **Advanced and operational topics**: real-time data, observability, multi-tenancy, security, and deployment

## Prerequisites

The tutorial expects you to have:

- Basic Java and web development knowledge
- Familiarity with REST APIs
- Basic understanding of containerized deployments

You will need the following software installed on your machine:

- [Docker](https://docs.docker.com/get-docker/) with [Docker Compose](https://docs.docker.com/compose/)
- [Node.js](https://nodejs.org/en/download/) 20 or higher for running the frontend
- [JDK](https://www.oracle.com/de/java/technologies/downloads/) 17 or higher for running the backend

We will also be using the following tools in this tutorial but any HTTP client will do:

- [curl](https://curl.se/) for making HTTP requests to the REST APIs
- [jq](https://jqlang.org/) for parsing JSON results in the command line

## Where do I start?

You can start right here with [Day 1](days/day-01.md).

## Why this repository exists

The goal is not just to document EDDIE features in isolation.
The goal is to help you build confidence by applying them inside a small but coherent application that resembles a real eligible party integration.

That means the tutorial aims to be:

- **incremental** — each day adds one clear concept
- **practical** — every day ends with an observable outcome
- **minimal** — code and configuration should stay as small as possible
- **reusable** — the final result should be close to a useful starter for your own solution