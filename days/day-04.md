<!--
Understand data access and permission lifecycle
- Explore consent management process model -> https://architecture.eddie.energy/framework/2-integrating/integrating.html
- Grant consent -> Simulation
- Modify permissions -> Simulation
- Set expiry -> Data Need
- Revoke consent -> Simulation
- Terminate consent -> Outbound Connector
- Trace lifecycle using connectionId and permissionId
-->

# Day 4 — Process Integration and Permission Lifecycle

**Goal**:

- Trace one permission from creation to revocation or termination
- Identify permissions using `connectionId` and `permissionId`

**Estimated time**: 1 hour

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-03.zip)

## Permission Lifecycle

We will use the Simulation region connector to create and update permissions.

We will use `curl` and `jq` to interact with the REST API provided by the REST outbound connector.

## Revocation

The customer may revoke their consent at any time through the portal of their permission administrator.
The region connectors are responsible for communicating these changes to EDDIE.

- `REVOKED`

TODO: Simple diagram (~ arch docs)

## Termination

You as the eligible party can terminate a permission by initiating a termination request via an outbound connector.
Region connectors can communicate the termination to the permission administrator so they stop sending data.

- `TERMINATED`
- `REQUIRES_EXTERNAL_TERMINATION`
- `FAILED_TO_TERMINATE`
- `EXTERNALLY_TERMINATED`

TODO: Simple diagram (~ arch docs)

TODO: REST Example
TODO: Kafka Example

## Tracing permissions

Each permission has a `permissionId` that uniquely identifies it across the system.
An arbitrary `connectionId` can be passed to the EDDIE button
allowing you to map permissions to your internal user and request context.
The `dataNeedId` identifies which data need the permission is based on.

When a permission is updated, EDDIE emits outbound messages that include these identifiers.

## Checkpoint

You understand:

- The difference between revocation and termination
- How permissions are identified and traced across the system using `permissionId` and `connectionId`

## What's next

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-04.zip)
