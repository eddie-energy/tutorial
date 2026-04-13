# Concept Note

21-Day Step-by-Step Tutorial to the EDDIE Framework
Learning pathway toward building a Pet-Shop-style sample application

## 1. Background and Purpose

The EDDIE Framework enables standardized access, orchestration, consent handling, and
distribution of energy data across heterogeneous regional infrastructures. While powerful,
its breadth can create a steep learning curve for new solution developers.
This 21-day tutorial series introduces the framework through daily 1–2 hour learning
slots, guiding participants from initial setup to a fully functional reference application
comparable in spirit to the classic Java Pet Shop — a realistic yet pedagogical system that
demonstrates end-to-end platform usage.

The tutorial emphasizes:

- Incremental learning
- Immediate hands-on validation
- Realistic integration scenarios
- Exposure to production-grade operational concerns
- Development of a reference energy application with modern web architecture

IMPORTANT: Each day should be encompassed with a “Baseline Code ZIP-File”
(where do we start from), and end with “Download the result of the workday”, so that
learners can easily debug and find out what did not go so well.

## 2. Target Audience

- Solution developers integrating energy data services
- Architects evaluating the EDDIE Framework
- Implementation partners onboarding customers
- Internal engineering teams requiring structured learning material

Prerequisites:

- Basic Java and web development knowledge
- Familiarity with REST APIs
- Basic understanding of containerized deployments

## 3. Learning Approach

The program follows four progressive phases:

- Foundation: Installation, connectors, data needs
- Integration: Consent lifecycle, outbound channels, process orchestration
- Application Development: Authentication, persistence, UI, visualization
- Advanced & Operational: Real-time data, observability, IoT, packaging

Each day contains:

- Short conceptual introduction
- Guided hands-on tasks
- Observable success outcome
- Suggested reflection / extension task

## 4. 21-Day Learning Plan

### Day 1 — First Contact & Simulated Flow (effort: 5h)

Goal: Validate end-to-end flow using simulated infrastructure

Activities:

- Install EDDIE Framework via Docker Compose
- Checkout reference repository
- Deploy minimal stack
- Use simulated region connector
- Create data need for:
    - Accounting point master data
    - Validated historical consumption
- Place “EDDIE Connect button” on simple webpage
- Observe data arrival via REST outbound connector

Outcome: First successful data retrieval loop via Simulation Region Connector

### Day 2 — Real Region Connectors (effort 4h)

Goal: Replace simulation with real infrastructure

Activities:

- Configure French connector (pattern account)
- Configure Danish connector
- Configure Austrian connector via Ponton XP Messenger
- Reuse Day-1 data need
- Retrieve real data via REST outbound

Outcome: Multi-region connectivity established

### Day 3 — Outbound Connector Strategy (effort: 5h)

Goal: Understand distribution patterns

Activities:

- Configure REST outbound (review)
- Configure Kafka outbound
- Configure AMQP outbound
- Compare semantics, latency, and usage patterns

Outcome: Informed outbound connector selection capability and state information
handling

### Day 4 — Process Integration & Consent Lifecycle (effort: 3h)

Goal: Understand governance of data access

Activities:

- Explore consent management process model
- Grant consent
- Modify permissions
- Set expiry
- Revoke consent / Terminate consent
- Trace lifecycle using connectionId and permissionId

Outcome: Operational understanding of permission lifecycle

### Day 5 — Application Skeleton (8-12h)

Goal: Establish application foundation

Activities:

- Create Spring Boot backend
- Create Angular frontend
- Deploy Keycloak
- Configure self-registration
- Add EDDIE Connect button component

Outcome: Authenticated application shell

### Day 6 — User-Bound Connections (effort: 3-4h)

Goal: Link EDDIE Connectivity to user identity

Activities:

- Map connection ID to user
- Implement onboarding redirect
- Request:
    - Accounting point master data
    - Validated historical data
- Validate user-specific flows
- Customise EDDIE Connect button and popup:
  https://architecture.eddie.energy/framework/1-running/eddie-button/eddie-button.html#customize-colors-and-content

Outcome: Multi-user onboarding journey

### Day 7 — Persistence & Raw Data Exploration (effort: 6-8h)

Goal: Understand incoming data variability

Activities:

- Store processed data and link with user account
- Analyse and develop an understanding for raw payloads
- Compare formats across connectors
- Explore metadata diversity
- Understand log information

Outcome: Familiarity with underlying heterogeneity

Commented [GS1]: unclear what metadata is meant

### Day 8 — Future & Incremental Data Handling (effort: 6h)

Goal: Handle ongoing delivery patterns

Activities:

- Process future validated consumption
- Detect gaps
- Implement retry / reconciliation strategy
- Introduce time-series continuity concepts

Outcome: Robust temporal data handling

Commented [RM2]: No way to consistently test -> explain that and how it works

### Day 9 — Observability & Logging (effort: 8h)

Goal: Production readiness

Activities:

- Introduce OpenTelemetry
    - Configure tracing
    - Configure structured logging
- Correlate connection ID across services

Commented [RM3]: Maybe delegate to Florian for he implemented the initial version
Commented [RM4]: Example to view error in OpenTelemetry

Outcome: Observable integration pipeline

### Day 10 — Client Libraries & Versioning (effort: 5-6h)

Goal: Efficient developer consumption

Activities:

- Retrieve client libraries via Maven / Gradle
- Understand semantic versioning strategy
- Explore documentation structure
- Implement client usage example

Outcome: Reduced integration friction

See: https://architecture.eddie.energy/framework/2-integrating/messages/cim/clientlibraries.html


### Day 11 — Admin Console Mastery (effort: 4h+3h)

Goal: Operational control

Activities:

- Navigate admin console
- Inspect connections
- Inspect consents
- Trigger troubleshooting scenarios
- Modify configurations safely

Outcome: Operator proficiency

See: https://architecture.eddie.energy/framework/1-running/admin-console.html

### Day 12 — Time-Series Visualization (effort: 4h based on ExampleApp usage)

Goal: User value realization

Activities:

- Integrate ApexCharts in Angular
- Display consumption curves
- Implement time range selection
- Implement dynamic refresh

Outcome: Interactive visualization layer

### Day 13 — Carbon & Mix Enrichment (effort: 16h?)

Goal: Demonstrate ecosystem value creation

Activities:

- Integrate ENTSO-E transparency platform service: https://transparency.entsoe.eu
- Retrieve carbon intensity & electricity mix
- Overlay enriched data in charts
- Demonstrate cross-domain analytics

Outcome: Context-aware visualization

### Day 14 — Real-Time Data via AIIDA (effort: 12-16h)

Goal: Introduce streaming acquisition

Activities:

- Create AIIDA outbound data need
- Explore available real-time connectors
    - Austrian energy adapter
    - Smart gateway adapter
    - Linky
- Understand hosting models (cloud vs on-prem)

Outcome: Real-time acquisition capability

### Day 15 — Real-Time Consumption in Application (effort: 5h)

Goal: Stream processing in UI

Activities:

- Subscribe to AIIDA data
- Store incremental updates
- Visualize near real-time curves
- Analyze adapter semantics
- Build virtual time series

Outcome: Continuous data experience

### Day 16 — IoT Interaction (effort: 16-20h)

Goal: Bi-directional platform usage

Activities:

- Send IoT commands / messages to AIIDA
- Explore actuator scenarios
- Implement command flow example
- Validate acknowledgment patterns

Outcome: Active device interaction understanding

### Day 17 — Event-Driven Architecture Patterns (effort: 8h)

Goal: Architectural maturity

Activities:

- Model domain events
- Implement event listeners
- Compare orchestration vs choreography
- Introduce saga pattern concepts

Outcome: Event-driven design competence

Commented [RM7]: https://microservices.io/patterns/data/saga.html

### Day 18 — Multi-Tenant Considerations (effort: ??)

Goal: SaaS readiness

Activities:

- Tenant isolation patterns
- Keycloak realm strategies
- Connector scoping
- Data partitioning approaches

Outcome: Multi-tenant design awareness

### Day 19 — Security Deep Dive (effort: 8h??)

Goal: Harden integrations

Activities:

- Token propagation patterns
- Least privilege consent modeling
- Secure outbound connectors
- Secrets management patterns

Outcome: Secure deployment mindset

Commented [RM8]: Secure management port vs OAuth over individual connectors

### Day 20 — Packaging & Deployment (effort: 6h)

Goal: Deliverable solution

Activities:

- Containerize application
- Compose full stack
- Prepare environment configuration
- Discuss CI/CD integration

Outcome: Deployable reference solution

### Day 21 — Consolidation & Extension (8h)

Goal: Transition from learning to solution building

Activities:

- Architecture recap
- Identify extension scenarios
- Brainstorm domain features for pet-shop analogue
- Define next steps for production adoption

Outcome: Confidence and forward roadmap

## 5. Expected Outcomes

Participants completing the tutorial will:

- Operate the EDDIE Framework confidently
- Understand consent-driven data orchestration
- Integrate multi-regional connectors
- Build authenticated energy applications
- Visualize historical and real-time data
- Apply operational best practices
- Extend the framework for domain-specific solutions

## 6. Success Criteria

- Successful daily completion checkpoints
- Fully working reference application
- Demonstrated real-time data integration
- Operational observability configured
- Ability to onboard new users independently

## 7. Next Steps

- Produce tutorial artifacts (videos, repos, scripts)
- Create automated environment bootstrap
- Define evaluation exercises
- Prepare facilitator guide
- Establish community support channel