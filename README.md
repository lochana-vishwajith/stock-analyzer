# CSE Stock Analyzer - Backend Architecture Design

## Spring Boot | Colombo Stock Exchange | Automated Analysis & Push Notifications

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Use Case Scenarios](#2-use-case-scenarios)
3. [High-Level System Architecture](#3-high-level-system-architecture)
4. [Microservice Breakdown](#4-microservice-breakdown)
5. [Database Design](#5-database-design)
6. [REST API Design](#6-rest-api-design)
7. [Technical Analysis Engine](#7-technical-analysis-engine)
8. [Report Analysis Agent (AI/LLM)](#8-report-analysis-agent)
9. [Notification System](#9-notification-system)
10. [Data Ingestion Pipeline](#10-data-ingestion-pipeline)
11. [Security & Authentication](#11-security--authentication)
12. [Technology Stack](#12-technology-stack)
13. [Deployment Architecture](#13-deployment-architecture)
14. [Phase-wise Development Roadmap](#14-phase-wise-development-roadmap)

---

## 1. Project Overview

A Spring Boot backend system that:

- **Ingests** near-real-time stock data from the Colombo Stock Exchange (CSE) via their unofficial public API endpoints (`https://www.cse.lk/api/`)
- **Analyzes** stock price patterns using technical indicators (RSI, MACD, SMA, EMA, Bollinger Bands, etc.)
- **Processes** company annual/quarterly reports using an AI agent to extract financial insights
- **Sends push notifications** with actionable trading signals (e.g., "LOLC.N0000 is in a Bullish trend, approaching resistance at LKR 560. Consider buying.")
- Designed for a **passive investor** doing monthly CSE investments who cannot monitor the market in real-time

---

## 2. Use Case Scenarios

### 2.1 User Management

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-01 | User Registration                 | User registers with email/phone, sets investment preferences (sectors, risk tolerance, budget)         |
| UC-02 | User Login / JWT Auth             | Secure login via JWT; optional OAuth2 (Google)                                                        |
| UC-03 | Profile Management                | Update notification preferences, watchlist, investment goals, preferred sectors                        |
| UC-04 | Manage Watchlist                  | Add/remove CSE stock symbols to a personal watchlist for focused monitoring                           |

### 2.2 Market Data & Monitoring

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-05 | View Market Overview              | See ASPI, S&P SL20 index values, market turnover, market status (open/closed)                         |
| UC-06 | View Stock Details                | Get detailed info for a stock: price, volume, market cap, day change, 52-week high/low                |
| UC-07 | View Top Gainers / Losers         | List of top gaining and losing stocks for the day                                                     |
| UC-08 | View Sector Performance           | Breakdown of sector-level index performance                                                           |
| UC-09 | View Historical Price Data        | Retrieve OHLCV (Open, High, Low, Close, Volume) candle data for any timeframe                         |
| UC-10 | Search Stocks                     | Search stocks by symbol, company name, or sector                                                      |

### 2.3 Technical Analysis

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-11 | View Technical Indicators         | See RSI, MACD, SMA(20/50/200), EMA(12/26), Bollinger Bands for any stock                             |
| UC-12 | Trend Detection                   | System identifies Bullish/Bearish/Sideways trends based on moving average crossovers and price action  |
| UC-13 | Support & Resistance Detection    | Automatic identification of key support/resistance price levels                                        |
| UC-14 | Breakout Detection                | Detect when a stock breaks above resistance or below support with volume confirmation                  |
| UC-15 | Pattern Recognition               | Identify chart patterns: Double Top/Bottom, Head & Shoulders, Triangles, Flags, Wedges                |
| UC-16 | Generate Trading Signal           | Automated BUY / SELL / HOLD signal based on composite indicator analysis                               |
| UC-17 | View Signal History               | Historical log of all signals generated for a stock, with accuracy tracking                            |

### 2.4 Report Analysis (AI Agent)

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-18 | Ingest Company Reports            | Automatically fetch or manually upload annual/quarterly reports (PDF) from CSE filings                 |
| UC-19 | AI Report Summarization           | LLM agent extracts: revenue growth, profit margins, debt ratios, management outlook, risk factors      |
| UC-20 | Fundamental Score                 | Generate a fundamental health score (1-100) based on extracted financial metrics                       |
| UC-21 | Compare Reports (YoY)             | Year-over-year comparison of key financial metrics for a company                                       |
| UC-22 | Sector Comparison                 | Compare fundamental scores across companies in the same sector                                         |

### 2.5 Notifications & Alerts

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-23 | Push Notification on Signal       | Send Firebase push notification when a BUY/SELL signal is generated for watchlist stocks               |
| UC-24 | Price Alert                       | User sets a target price; notification fires when stock hits that price                                |
| UC-25 | Breakout Alert                    | Notify when a stock breaks a key support/resistance level                                              |
| UC-26 | Report Published Alert            | Notify when a new annual/quarterly report is available for a watchlist company                          |
| UC-27 | Daily Summary Digest              | End-of-day summary: portfolio performance, signals generated, market overview                          |
| UC-28 | Weekly Analysis Report            | Weekly email/push with top picks, sector analysis, and upcoming events                                 |

### 2.6 Portfolio Tracking

| ID    | Use Case                          | Description                                                                                           |
|-------|-----------------------------------|-------------------------------------------------------------------------------------------------------|
| UC-29 | Add Holdings                      | Record stock purchases: symbol, quantity, buy price, date                                              |
| UC-30 | View Portfolio Performance        | Real-time P&L, total investment vs current value, percentage returns                                   |
| UC-31 | Portfolio Diversification View    | Sector-wise breakdown of portfolio with diversification score                                          |
| UC-32 | Monthly Investment Planner        | Suggest which stocks to buy this month based on signals + available budget                              |

---

## 3. High-Level System Architecture

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT LAYER                                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────────────────────┐    │
│  │  Mobile App   │  │   Web App    │  │  Admin Dashboard                    │    │
│  │  (Flutter/RN) │  │  (React/Next)│  │  (React)                           │    │
│  └──────┬───────┘  └──────┬───────┘  └──────────────┬───────────────────────┘    │
│         │                 │                          │                            │
│         └─────────────────┼──────────────────────────┘                            │
│                           │                                                       │
│                    ┌──────▼───────┐                                               │
│                    │  API Gateway │ (Spring Cloud Gateway)                        │
│                    │  + Rate Limit│                                               │
│                    └──────┬───────┘                                               │
└───────────────────────────┼──────────────────────────────────────────────────────┘
                            │
┌───────────────────────────┼──────────────────────────────────────────────────────┐
│                     BACKEND SERVICES                                             │
│                           │                                                       │
│  ┌────────────────────────┼────────────────────────────────────────────────┐      │
│  │                        ▼                                                │      │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐  │      │
│  │  │  User Service │ │ Market Data  │ │  Analysis    │ │ Notification │  │      │
│  │  │              │ │  Service     │ │  Service     │ │  Service     │  │      │
│  │  │ - Auth/JWT   │ │ - CSE Poller │ │ - Technical  │ │ - Firebase   │  │      │
│  │  │ - Profile    │ │ - Price Store│ │ - Patterns   │ │ - Email      │  │      │
│  │  │ - Watchlist  │ │ - History    │ │ - Signals    │ │ - WebSocket  │  │      │
│  │  │ - Portfolio  │ │ - Sectors    │ │ - AI Reports │ │ - Scheduling │  │      │
│  │  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘  │      │
│  │         │                │                │                │           │      │
│  └─────────┼────────────────┼────────────────┼────────────────┼───────────┘      │
│            │                │                │                │                   │
│            ▼                ▼                ▼                ▼                   │
│  ┌─────────────────────────────────────────────────────────────────────┐          │
│  │                    MESSAGE BROKER (Apache Kafka)                     │          │
│  │                                                                     │          │
│  │  Topics:                                                            │          │
│  │  - market.price.updates    - analysis.signals.generated             │          │
│  │  - market.trade.summary    - analysis.pattern.detected              │          │
│  │  - reports.new.published   - notification.push.send                 │          │
│  │  - reports.analysis.done   - portfolio.rebalance.suggest            │          │
│  └─────────────────────────────────────────────────────────────────────┘          │
│                                                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐          │
│  │                       DATA LAYER                                    │          │
│  │                                                                     │          │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │          │
│  │  │  PostgreSQL   │  │    Redis     │  │  TimescaleDB │              │          │
│  │  │  (Primary DB) │  │   (Cache +   │  │  (Time-series│              │          │
│  │  │  Users, Stocks│  │   Sessions)  │  │   Price Data)│              │          │
│  │  │  Portfolios   │  │              │  │              │              │          │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │          │
│  │                                                                     │          │
│  │  ┌──────────────┐  ┌──────────────┐                                │          │
│  │  │ MinIO / S3    │  │ Elasticsearch│                                │          │
│  │  │ (PDF Reports) │  │ (Search +    │                                │          │
│  │  │              │  │  Log Agg)    │                                │          │
│  │  └──────────────┘  └──────────────┘                                │          │
│  └─────────────────────────────────────────────────────────────────────┘          │
│                                                                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐          │
│  │                    EXTERNAL INTEGRATIONS                            │          │
│  │                                                                     │          │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │          │
│  │  │  CSE API      │  │  OpenAI /    │  │  Firebase    │              │          │
│  │  │  (cse.lk/api) │  │  LLM API     │  │  Cloud Msg   │              │          │
│  │  │              │  │  (Reports)   │  │  (Push)      │              │          │
│  │  └──────────────┘  └──────────────┘  └──────────────┘              │          │
│  └─────────────────────────────────────────────────────────────────────┘          │
└──────────────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Microservice Breakdown

### Option A: Modular Monolith (Recommended for Phase 1)

Start with a single Spring Boot application using well-separated modules. Extract to microservices later when scale demands it.

```
cse-stock-analyzer/
├── src/main/java/com/cseanalyzer/
│   ├── CseStockAnalyzerApplication.java
│   │
│   ├── common/                         # Shared utilities
│   │   ├── config/                     # App configs, Kafka, Redis, Security
│   │   ├── exception/                  # Global exception handling
│   │   ├── dto/                        # Shared DTOs
│   │   └── util/                       # Date, math, formatting utils
│   │
│   ├── user/                           # User Module
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── model/
│   │   └── dto/
│   │
│   ├── market/                         # Market Data Module
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── MarketDataService.java
│   │   │   ├── CseApiClient.java       # HTTP client to CSE API
│   │   │   ├── PricePollerService.java  # Scheduled poller
│   │   │   └── HistoricalDataService.java
│   │   ├── repository/
│   │   ├── model/
│   │   └── dto/
│   │
│   ├── analysis/                       # Technical Analysis Module
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── TechnicalIndicatorService.java
│   │   │   ├── TrendDetectionService.java
│   │   │   ├── PatternRecognitionService.java
│   │   │   ├── SignalGeneratorService.java
│   │   │   └── SupportResistanceService.java
│   │   ├── engine/
│   │   │   ├── indicators/             # RSI, MACD, SMA, EMA, BB calculators
│   │   │   ├── patterns/              # Chart pattern detectors
│   │   │   └── signals/              # Signal composite engine
│   │   ├── repository/
│   │   ├── model/
│   │   └── dto/
│   │
│   ├── reports/                        # AI Report Analysis Module
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── ReportIngestionService.java
│   │   │   ├── PdfParserService.java
│   │   │   ├── LlmAnalysisService.java
│   │   │   └── FundamentalScoreService.java
│   │   ├── repository/
│   │   ├── model/
│   │   └── dto/
│   │
│   ├── notification/                   # Notification Module
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── PushNotificationService.java
│   │   │   ├── EmailService.java
│   │   │   ├── WebSocketService.java
│   │   │   └── NotificationScheduler.java
│   │   ├── repository/
│   │   ├── model/
│   │   └── dto/
│   │
│   └── portfolio/                      # Portfolio Module
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── model/
│       └── dto/
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/                   # Flyway migrations
│
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

### Option B: Microservices (Phase 2+)

When traffic/complexity grows, extract modules into separate Spring Boot services communicating via Kafka and REST.

| Service              | Port  | Responsibility                                    |
|----------------------|-------|---------------------------------------------------|
| api-gateway          | 8080  | Routing, rate limiting, auth verification         |
| user-service         | 8081  | Auth, profiles, watchlists                        |
| market-data-service  | 8082  | CSE polling, price storage, historical data       |
| analysis-service     | 8083  | Technical indicators, patterns, signals           |
| report-service       | 8084  | PDF ingestion, LLM analysis, fundamental scores   |
| notification-service | 8085  | Push, email, WebSocket, scheduling                |
| portfolio-service    | 8086  | Holdings, P&L, rebalancing suggestions            |

---

## 5. Database Design

### 5.1 PostgreSQL - Core Schema

```sql
-- =============================================
-- USER DOMAIN
-- =============================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) UNIQUE NOT NULL,
    phone           VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255),
    risk_tolerance  VARCHAR(20) DEFAULT 'MODERATE',  -- LOW, MODERATE, HIGH
    monthly_budget  DECIMAL(15, 2),
    preferred_sectors TEXT[],                         -- PostgreSQL array
    fcm_token       VARCHAR(512),                    -- Firebase Cloud Messaging
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

CREATE TABLE watchlist (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    symbol      VARCHAR(20) NOT NULL,               -- e.g., "JKH.N0000"
    added_at    TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, symbol)
);

CREATE TABLE notification_preferences (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID REFERENCES users(id) ON DELETE CASCADE,
    push_enabled        BOOLEAN DEFAULT TRUE,
    email_enabled       BOOLEAN DEFAULT TRUE,
    signal_alerts       BOOLEAN DEFAULT TRUE,
    price_alerts        BOOLEAN DEFAULT TRUE,
    breakout_alerts     BOOLEAN DEFAULT TRUE,
    report_alerts       BOOLEAN DEFAULT TRUE,
    daily_digest        BOOLEAN DEFAULT TRUE,
    weekly_report       BOOLEAN DEFAULT FALSE,
    quiet_hours_start   TIME,
    quiet_hours_end     TIME
);

-- =============================================
-- MARKET DATA DOMAIN
-- =============================================

CREATE TABLE stocks (
    symbol          VARCHAR(20) PRIMARY KEY,         -- e.g., "JKH.N0000"
    company_name    VARCHAR(255) NOT NULL,
    sector          VARCHAR(100),
    security_type   VARCHAR(30),                     -- NORMAL_STOCK, UNIT_TRUST, etc.
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW()
);

-- TimescaleDB hypertable for time-series price data
CREATE TABLE stock_prices (
    time            TIMESTAMPTZ NOT NULL,
    symbol          VARCHAR(20) NOT NULL,
    open            DECIMAL(15, 2),
    high            DECIMAL(15, 2),
    low             DECIMAL(15, 2),
    close           DECIMAL(15, 2),
    volume          BIGINT,
    turnover        DECIMAL(20, 2),
    trade_count     INTEGER,
    market_cap      DECIMAL(20, 2),
    change          DECIMAL(10, 2),
    change_pct      DECIMAL(8, 4)
);
-- Convert to hypertable
SELECT create_hypertable('stock_prices', 'time');
CREATE INDEX idx_stock_prices_symbol ON stock_prices (symbol, time DESC);

CREATE TABLE market_indices (
    time            TIMESTAMPTZ NOT NULL,
    index_name      VARCHAR(20) NOT NULL,            -- 'ASPI', 'SNP_SL20'
    value           DECIMAL(15, 4),
    change          DECIMAL(10, 4),
    change_pct      DECIMAL(8, 4),
    turnover        DECIMAL(20, 2),
    volume          BIGINT
);
SELECT create_hypertable('market_indices', 'time');

CREATE TABLE sector_indices (
    time            TIMESTAMPTZ NOT NULL,
    sector_name     VARCHAR(100) NOT NULL,
    value           DECIMAL(15, 4),
    change          DECIMAL(10, 4),
    change_pct      DECIMAL(8, 4)
);
SELECT create_hypertable('sector_indices', 'time');

-- =============================================
-- ANALYSIS DOMAIN
-- =============================================

CREATE TABLE technical_indicators (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL,
    calculated_at   TIMESTAMPTZ NOT NULL,
    indicator_type  VARCHAR(30) NOT NULL,            -- RSI, MACD, SMA_20, EMA_12, etc.
    value           DECIMAL(15, 6),
    signal_value    DECIMAL(15, 6),                  -- For MACD signal line, etc.
    histogram       DECIMAL(15, 6),                  -- For MACD histogram
    metadata        JSONB                            -- Flexible extra data
);
CREATE INDEX idx_tech_ind_symbol ON technical_indicators (symbol, indicator_type, calculated_at DESC);

CREATE TABLE trading_signals (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL,
    signal_type     VARCHAR(10) NOT NULL,            -- BUY, SELL, HOLD
    strength        VARCHAR(10) NOT NULL,            -- STRONG, MODERATE, WEAK
    confidence      DECIMAL(5, 2),                   -- 0-100%
    trigger_reason  TEXT NOT NULL,                   -- e.g., "MACD bullish crossover + RSI oversold recovery"
    entry_price     DECIMAL(15, 2),
    target_price    DECIMAL(15, 2),
    stop_loss       DECIMAL(15, 2),
    support_level   DECIMAL(15, 2),
    resistance_level DECIMAL(15, 2),
    trend           VARCHAR(20),                     -- BULLISH, BEARISH, SIDEWAYS
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    expires_at      TIMESTAMPTZ,
    is_active       BOOLEAN DEFAULT TRUE
);
CREATE INDEX idx_signals_symbol ON trading_signals (symbol, created_at DESC);
CREATE INDEX idx_signals_active ON trading_signals (is_active, signal_type);

CREATE TABLE detected_patterns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL,
    pattern_type    VARCHAR(50) NOT NULL,            -- DOUBLE_BOTTOM, HEAD_SHOULDERS, TRIANGLE, etc.
    direction       VARCHAR(10),                     -- BULLISH, BEARISH
    confidence      DECIMAL(5, 2),
    start_date      DATE,
    detected_at     TIMESTAMPTZ DEFAULT NOW(),
    breakout_price  DECIMAL(15, 2),
    target_price    DECIMAL(15, 2),
    metadata        JSONB
);

CREATE TABLE support_resistance_levels (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL,
    level_type      VARCHAR(15) NOT NULL,            -- SUPPORT, RESISTANCE
    price           DECIMAL(15, 2) NOT NULL,
    strength        INTEGER,                         -- Number of touches
    detected_at     TIMESTAMPTZ DEFAULT NOW(),
    is_active       BOOLEAN DEFAULT TRUE
);

-- =============================================
-- REPORT ANALYSIS DOMAIN
-- =============================================

CREATE TABLE company_reports (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol          VARCHAR(20) NOT NULL,
    report_type     VARCHAR(20) NOT NULL,            -- ANNUAL, QUARTERLY, INTERIM
    fiscal_year     VARCHAR(10),                     -- e.g., "2024/25"
    fiscal_quarter  VARCHAR(5),                      -- Q1, Q2, Q3, Q4
    report_url      TEXT,
    file_path       TEXT,                            -- S3/MinIO path
    published_date  DATE,
    ingested_at     TIMESTAMPTZ DEFAULT NOW(),
    analysis_status VARCHAR(20) DEFAULT 'PENDING'    -- PENDING, PROCESSING, COMPLETED, FAILED
);

CREATE TABLE report_analysis (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id       UUID REFERENCES company_reports(id),
    symbol          VARCHAR(20) NOT NULL,
    revenue         DECIMAL(20, 2),
    net_profit      DECIMAL(20, 2),
    eps             DECIMAL(10, 4),
    pe_ratio        DECIMAL(10, 4),
    debt_to_equity  DECIMAL(10, 4),
    roe             DECIMAL(8, 4),
    roa             DECIMAL(8, 4),
    profit_margin   DECIMAL(8, 4),
    revenue_growth  DECIMAL(8, 4),
    dividend_yield  DECIMAL(8, 4),
    fundamental_score INTEGER,                       -- 1-100
    ai_summary      TEXT,                            -- LLM-generated summary
    risk_factors    TEXT[],
    outlook         VARCHAR(20),                     -- POSITIVE, NEUTRAL, NEGATIVE
    analyzed_at     TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- NOTIFICATION DOMAIN
-- =============================================

CREATE TABLE notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(30) NOT NULL,            -- SIGNAL, PRICE_ALERT, BREAKOUT, REPORT, DIGEST
    title           VARCHAR(255) NOT NULL,
    body            TEXT NOT NULL,
    data            JSONB,                           -- Additional payload (symbol, signal_id, etc.)
    channel         VARCHAR(20) NOT NULL,            -- PUSH, EMAIL, IN_APP
    status          VARCHAR(20) DEFAULT 'PENDING',   -- PENDING, SENT, DELIVERED, FAILED
    sent_at         TIMESTAMPTZ,
    read_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_notifications_user ON notifications (user_id, created_at DESC);

CREATE TABLE price_alerts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    symbol          VARCHAR(20) NOT NULL,
    target_price    DECIMAL(15, 2) NOT NULL,
    condition       VARCHAR(10) NOT NULL,            -- ABOVE, BELOW
    is_triggered    BOOLEAN DEFAULT FALSE,
    triggered_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- =============================================
-- PORTFOLIO DOMAIN
-- =============================================

CREATE TABLE portfolio_holdings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    symbol          VARCHAR(20) NOT NULL,
    quantity        INTEGER NOT NULL,
    buy_price       DECIMAL(15, 2) NOT NULL,
    buy_date        DATE NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_holdings_user ON portfolio_holdings (user_id, symbol);

CREATE TABLE monthly_investment_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    month           DATE NOT NULL,                   -- First day of month
    budget          DECIMAL(15, 2),
    status          VARCHAR(20) DEFAULT 'DRAFT',     -- DRAFT, SUGGESTED, EXECUTED
    suggestions     JSONB,                           -- Array of {symbol, quantity, reason, signal_id}
    created_at      TIMESTAMPTZ DEFAULT NOW()
);
```

### 5.2 Entity Relationship Diagram (Logical)

```
┌──────────┐        ┌────────────┐        ┌──────────────┐
│  users   │──1:N──▶│ watchlist   │        │   stocks     │
│          │        └────────────┘        │              │
│          │──1:N──▶┌────────────────┐    └──────┬───────┘
│          │        │notification_   │           │
│          │        │preferences     │           │ 1
│          │        └────────────────┘           │
│          │──1:N──▶┌────────────────┐           ▼ N
│          │        │price_alerts    │    ┌──────────────┐
│          │        └────────────────┘    │ stock_prices │ (TimescaleDB)
│          │──1:N──▶┌────────────────┐    └──────────────┘
│          │        │portfolio_      │           │
│          │        │holdings        │           ▼ N
│          │        └────────────────┘    ┌──────────────────┐
│          │──1:N──▶┌────────────────┐    │technical_        │
│          │        │notifications   │    │indicators        │
│          │        └────────────────┘    └──────────────────┘
│          │──1:N──▶┌────────────────┐           │
│          │        │monthly_invest_ │           ▼ N
└──────────┘        │plans           │    ┌──────────────────┐
                    └────────────────┘    │trading_signals   │
                                          └──────────────────┘
                                                 │
                                                 ▼ N
                                          ┌──────────────────┐
                                          │detected_patterns │
                                          └──────────────────┘

┌──────────────┐     ┌──────────────────┐
│company_      │──1:N│report_analysis   │
│reports       │     │                  │
└──────────────┘     └──────────────────┘
```

---

## 6. REST API Design

### 6.1 Auth APIs

| Method | Endpoint                    | Description                |
|--------|-----------------------------|----------------------------|
| POST   | `/api/v1/auth/register`     | Register new user          |
| POST   | `/api/v1/auth/login`        | Login, returns JWT         |
| POST   | `/api/v1/auth/refresh`      | Refresh JWT token          |
| POST   | `/api/v1/auth/logout`       | Invalidate token           |

### 6.2 User APIs

| Method | Endpoint                              | Description                          |
|--------|---------------------------------------|--------------------------------------|
| GET    | `/api/v1/users/me`                    | Get current user profile             |
| PUT    | `/api/v1/users/me`                    | Update profile                       |
| GET    | `/api/v1/users/me/watchlist`          | Get watchlist                        |
| POST   | `/api/v1/users/me/watchlist`          | Add stock to watchlist               |
| DELETE | `/api/v1/users/me/watchlist/{symbol}` | Remove from watchlist                |
| GET    | `/api/v1/users/me/preferences`        | Get notification preferences         |
| PUT    | `/api/v1/users/me/preferences`        | Update notification preferences      |

### 6.3 Market Data APIs

| Method | Endpoint                                | Description                          |
|--------|-----------------------------------------|--------------------------------------|
| GET    | `/api/v1/market/status`                 | Market open/closed status            |
| GET    | `/api/v1/market/overview`               | ASPI, S&P SL20, turnover, volume     |
| GET    | `/api/v1/market/top-gainers`            | Today's top gainers                  |
| GET    | `/api/v1/market/top-losers`             | Today's top losers                   |
| GET    | `/api/v1/market/sectors`                | All sector indices                   |
| GET    | `/api/v1/stocks`                        | List all stocks (paginated, filterable) |
| GET    | `/api/v1/stocks/search?q={query}`       | Search stocks by name/symbol         |
| GET    | `/api/v1/stocks/{symbol}`               | Stock detail (price, cap, volume)    |
| GET    | `/api/v1/stocks/{symbol}/history`       | Historical OHLCV data                |
|        |   `?from=&to=&interval=`                | Intervals: 1m, 5m, 1h, 1d, 1w       |

### 6.4 Analysis APIs

| Method | Endpoint                                          | Description                          |
|--------|---------------------------------------------------|--------------------------------------|
| GET    | `/api/v1/analysis/{symbol}/indicators`            | All technical indicators for stock   |
| GET    | `/api/v1/analysis/{symbol}/indicators/{type}`     | Specific indicator (RSI, MACD, etc.) |
| GET    | `/api/v1/analysis/{symbol}/trend`                 | Current trend analysis               |
| GET    | `/api/v1/analysis/{symbol}/support-resistance`    | Support & resistance levels          |
| GET    | `/api/v1/analysis/{symbol}/patterns`              | Detected chart patterns              |
| GET    | `/api/v1/analysis/{symbol}/signal`                | Latest trading signal                |
| GET    | `/api/v1/analysis/{symbol}/signals`               | Signal history                       |
| GET    | `/api/v1/analysis/signals/latest`                 | All latest active signals            |
| GET    | `/api/v1/analysis/screener`                       | Filter stocks by indicator criteria  |
|        |   `?rsi_below=30&trend=BULLISH&signal=BUY`        |                                      |

### 6.5 Report APIs

| Method | Endpoint                                    | Description                          |
|--------|---------------------------------------------|--------------------------------------|
| GET    | `/api/v1/reports/{symbol}`                  | List all reports for a company       |
| GET    | `/api/v1/reports/{symbol}/latest`           | Latest report analysis               |
| GET    | `/api/v1/reports/{symbol}/compare`          | YoY comparison                       |
| GET    | `/api/v1/reports/{symbol}/{reportId}`       | Specific report analysis detail      |
| POST   | `/api/v1/reports/upload`                    | Upload a report PDF manually         |
| GET    | `/api/v1/reports/sector/{sector}/compare`   | Sector-level fundamental comparison  |

### 6.6 Notification APIs

| Method | Endpoint                                   | Description                           |
|--------|-------------------------------------------|---------------------------------------|
| GET    | `/api/v1/notifications`                    | Get user notifications (paginated)    |
| PUT    | `/api/v1/notifications/{id}/read`          | Mark as read                          |
| POST   | `/api/v1/alerts/price`                     | Create price alert                    |
| GET    | `/api/v1/alerts/price`                     | Get user's price alerts               |
| DELETE | `/api/v1/alerts/price/{id}`                | Delete price alert                    |

### 6.7 Portfolio APIs

| Method | Endpoint                                       | Description                          |
|--------|-------------------------------------------------|--------------------------------------|
| GET    | `/api/v1/portfolio`                             | Get portfolio summary + P&L          |
| POST   | `/api/v1/portfolio/holdings`                    | Add a holding                        |
| PUT    | `/api/v1/portfolio/holdings/{id}`               | Update a holding                     |
| DELETE | `/api/v1/portfolio/holdings/{id}`               | Remove a holding                     |
| GET    | `/api/v1/portfolio/diversification`             | Sector breakdown                     |
| GET    | `/api/v1/portfolio/monthly-plan`                | Get this month's investment plan     |
| POST   | `/api/v1/portfolio/monthly-plan/generate`       | Generate AI-powered plan             |

### 6.8 WebSocket Endpoints

| Endpoint                        | Description                                     |
|---------------------------------|-------------------------------------------------|
| `/ws/market`                    | Real-time market data stream                    |
| `/ws/signals`                   | Real-time signal notifications                  |
| `/ws/portfolio/{userId}`        | Real-time portfolio value updates               |

---

## 7. Technical Analysis Engine

### 7.1 Indicator Calculations

```java
// Indicator Engine Architecture
public interface TechnicalIndicator {
    IndicatorResult calculate(List<CandleData> candles, Map<String, Object> params);
    String getName();
}

// Implementations
├── RsiCalculator           // RSI(14) - Relative Strength Index
├── MacdCalculator          // MACD(12, 26, 9)
├── SmaCalculator           // SMA(20), SMA(50), SMA(200)
├── EmaCalculator           // EMA(12), EMA(26)
├── BollingerBandsCalculator // BB(20, 2)
├── StochasticCalculator    // Stochastic %K(14, 3, 3)
├── AtrCalculator           // ATR(14) - Average True Range
├── AdxCalculator           // ADX(14) - Average Directional Index
├── VwapCalculator          // VWAP
└── IchimokuCalculator      // Ichimoku Cloud
```

### 7.2 Signal Generation Logic

```
Signal = f(Trend, Indicators, Patterns, Volume, Support/Resistance)

STRONG_BUY when:
  - Price above SMA(200) AND SMA(50) crossing above SMA(200) (Golden Cross)
  - RSI recovering from below 30 (oversold bounce)
  - MACD bullish crossover (MACD line crosses above signal)
  - Volume surge (> 2x average volume)
  - Price bouncing off strong support level
  - Bullish chart pattern confirmed (e.g., Double Bottom breakout)

STRONG_SELL when:
  - SMA(50) crossing below SMA(200) (Death Cross)
  - RSI above 70 and turning down
  - MACD bearish crossover
  - Price breaking below strong support
  - Bearish pattern confirmed (e.g., Head & Shoulders breakdown)

Confidence Score (0-100):
  - Each confirming indicator adds weight
  - Volume confirmation adds +15
  - Pattern confirmation adds +20
  - Multiple timeframe alignment adds +15
```

### 7.3 Pattern Detection

```java
public interface PatternDetector {
    Optional<DetectedPattern> detect(List<CandleData> candles);
}

// Implementations
├── DoubleTopDetector
├── DoubleBottomDetector
├── HeadAndShouldersDetector
├── InverseHeadAndShouldersDetector
├── AscendingTriangleDetector
├── DescendingTriangleDetector
├── SymmetricTriangleDetector
├── BullFlagDetector
├── BearFlagDetector
├── RisingWedgeDetector
├── FallingWedgeDetector
└── CupAndHandleDetector
```

### 7.4 Scheduled Analysis Jobs (Spring @Scheduled)

| Job                    | Schedule              | Description                                              |
|------------------------|-----------------------|----------------------------------------------------------|
| PricePollerJob         | Every 60s (market hrs)| Poll CSE API for latest prices                           |
| IndicatorCalculationJob| Every 5 min           | Recalculate indicators for active stocks                 |
| TrendAnalysisJob       | Every 15 min          | Run trend detection on all stocks                        |
| PatternScanJob         | Daily at 15:00 SLT    | Full pattern scan after market close                     |
| SignalGenerationJob    | Every 15 min          | Generate/update trading signals                          |
| DailyDigestJob         | Daily at 15:30 SLT    | Send daily summary to all users                          |
| WeeklyReportJob        | Friday 16:00 SLT      | Send weekly analysis report                              |
| ReportCheckJob         | Daily at 06:00 SLT    | Check for new company reports on CSE                     |
| MonthlyPlanJob         | 1st of month, 08:00   | Generate monthly investment suggestions                  |

---

## 8. Report Analysis Agent

### 8.1 Architecture

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ CSE Report  │────▶│  PDF Parser  │────▶│  LLM Agent   │────▶│ Fundamental  │
│ Scraper     │     │  (Apache     │     │  (OpenAI /   │     │ Score Engine │
│             │     │   Tika/      │     │   Gemini)    │     │              │
│ Checks for  │     │  PDFBox)     │     │              │     │ Calculates   │
│ new reports │     │              │     │ Extracts KPIs│     │ 1-100 score  │
│ daily       │     │ Text + Table │     │ + Summary    │     │              │
└─────────────┘     │ extraction   │     └──────────────┘     └──────────────┘
                    └──────────────┘
```

### 8.2 LLM Prompt Template (for report analysis)

```
You are a financial analyst specializing in the Colombo Stock Exchange.
Analyze the following company report and extract:

1. Revenue (current year and previous year)
2. Net Profit / Loss
3. Earnings Per Share (EPS)
4. Debt-to-Equity Ratio
5. Return on Equity (ROE)
6. Return on Assets (ROA)
7. Profit Margin
8. Revenue Growth Rate (YoY)
9. Dividend information
10. Key risk factors (list top 3)
11. Management outlook (POSITIVE / NEUTRAL / NEGATIVE)
12. One-paragraph summary for a retail investor

Company: {company_name}
Symbol: {symbol}
Report Type: {annual/quarterly}
Fiscal Year: {year}

Report Text:
{extracted_text}

Respond in JSON format.
```

### 8.3 Fundamental Score Calculation

```
Fundamental Score (1-100) = Weighted Sum of:
  - Revenue Growth       (20%) → 0-20 points
  - Profit Margin        (15%) → 0-15 points
  - ROE                  (15%) → 0-15 points
  - Debt-to-Equity       (15%) → 0-15 points (lower is better)
  - EPS Growth           (10%) → 0-10 points
  - Dividend Yield       (10%) → 0-10 points
  - Management Outlook   (10%) → 0-10 points
  - Sector Relative Perf  (5%) → 0-5 points
```

---

## 9. Notification System

### 9.1 Architecture

```
┌─────────────────┐
│ Signal Generated│
│ / Price Alert   │─────┐
│ / Report Ready  │     │
└─────────────────┘     │
                        ▼
              ┌─────────────────┐
              │  Kafka Topic:   │
              │  notification.  │
              │  push.send      │
              └────────┬────────┘
                       │
                       ▼
              ┌─────────────────┐     ┌─────────────────┐
              │  Notification   │────▶│  Channel Router  │
              │  Consumer       │     │                  │
              └─────────────────┘     └────────┬────────┘
                                               │
                    ┌──────────────────────────┼──────────────────┐
                    │                          │                  │
                    ▼                          ▼                  ▼
           ┌──────────────┐          ┌──────────────┐   ┌──────────────┐
           │   Firebase   │          │    Email      │   │  WebSocket   │
           │   Cloud Msg  │          │  (SendGrid /  │   │  (STOMP)     │
           │   (Push)     │          │   SES)        │   │              │
           └──────────────┘          └──────────────┘   └──────────────┘
```

### 9.2 Push Notification Payload Example

```json
{
  "notification": {
    "title": "BUY Signal - JKH.N0000",
    "body": "John Keells Holdings is in a BULLISH trend. Price approaching breakout above LKR 215. RSI recovering from oversold. Confidence: 82%"
  },
  "data": {
    "type": "TRADING_SIGNAL",
    "signal_id": "uuid-here",
    "symbol": "JKH.N0000",
    "signal_type": "BUY",
    "strength": "STRONG",
    "entry_price": "210.50",
    "target_price": "235.00",
    "stop_loss": "198.00",
    "confidence": "82",
    "click_action": "OPEN_STOCK_DETAIL"
  }
}
```

### 9.3 Notification Types

| Type              | Trigger                                    | Channel          | Priority |
|-------------------|--------------------------------------------|------------------|----------|
| TRADING_SIGNAL    | New BUY/SELL signal for watchlist stock     | Push + In-App    | HIGH     |
| PRICE_ALERT       | Stock hits user-defined target price        | Push + In-App    | HIGH     |
| BREAKOUT_ALERT    | Stock breaks support/resistance             | Push + In-App    | HIGH     |
| REPORT_PUBLISHED  | New annual/quarterly report available       | Push + Email     | MEDIUM   |
| DAILY_DIGEST      | End of trading day summary                  | Email + In-App   | LOW      |
| WEEKLY_REPORT     | Weekly analysis compilation                 | Email            | LOW      |
| MONTHLY_PLAN      | New month investment suggestions            | Push + Email     | MEDIUM   |

---

## 10. Data Ingestion Pipeline

### 10.1 CSE API Integration

```java
@Service
public class CseApiClient {

    private static final String BASE_URL = "https://www.cse.lk/api/";

    // Available endpoints (POST method, no params unless noted)
    // ─────────────────────────────────────────────────────────
    // marketStatus          → Market open/closed
    // marketSummery         → ASPI, Volume, Turnover
    // todaySharePrice       → All active stock prices
    // companyInfoSummery    → Detail for one stock (param: symbol)
    // tradeSummary          → All trade summaries
    // topGainers            → Top gaining stocks
    // topLooses             → Top losing stocks
    // aspiData              → ASPI index history
    // snpData               → S&P SL20 index history
    // allSectors            → Sector indices

    // Required headers for CSE API:
    // Origin: https://www.cse.lk
    // Referer: https://www.cse.lk/
    // X-Requested-With: XMLHttpRequest
    // Cookie: JSESSIONID=<valid_session>
}
```

### 10.2 Polling Flow

```
Market Hours: 09:30 - 14:30 SLT (UTC+5:30)

┌─────────────────┐
│  @Scheduled     │
│  (fixedRate =   │──── Is market open? ──── No ──── Sleep until next market open
│   60000)        │         │
└─────────────────┘        Yes
                            │
                            ▼
                   ┌─────────────────┐
                   │ Fetch all prices│
                   │ (todaySharePrice│
                   │  + marketSummery│
                   │  + topGainers   │
                   │  + topLooses)   │
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ Normalize &     │
                   │ Store in        │───▶ TimescaleDB (stock_prices)
                   │ TimescaleDB     │───▶ Redis cache (latest prices)
                   └────────┬────────┘
                            │
                            ▼
                   ┌─────────────────┐
                   │ Publish to      │
                   │ Kafka topic:    │───▶ market.price.updates
                   │ price update    │
                   └─────────────────┘
                            │
                    ┌───────┴────────┐
                    ▼                ▼
           ┌──────────────┐ ┌──────────────┐
           │ Price Alert  │ │ Indicator    │
           │ Checker      │ │ Recalculator │
           └──────────────┘ └──────────────┘
```

---

## 11. Security & Authentication

### 11.1 Auth Flow

```
                          ┌──────────────────────┐
                          │   Spring Security     │
                          │   Filter Chain        │
                          │                      │
  Request ───▶           │  1. JwtAuthFilter     │
                          │  2. UsernamePassword  │
                          │  3. SecurityContext   │
                          └──────────┬───────────┘
                                     │
                        ┌────────────┴────────────┐
                        │                         │
                   Authenticated              Unauthenticated
                        │                         │
                        ▼                         ▼
                  Access Resource           401 Unauthorized
```

### 11.2 Security Configuration

```yaml
# application.yml
spring:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400000          # 24 hours
      refresh-expiration: 604800000 # 7 days

# Rate limiting (per user)
rate-limit:
  requests-per-minute: 60
  analysis-requests-per-minute: 20
```

### 11.3 Endpoint Security Matrix

| Endpoint Group      | Auth Required | Roles           |
|---------------------|---------------|-----------------|
| `/api/v1/auth/**`   | No            | PUBLIC          |
| `/api/v1/market/**` | No            | PUBLIC          |
| `/api/v1/stocks/**` | No            | PUBLIC          |
| `/api/v1/analysis/**`| Yes          | USER, PREMIUM   |
| `/api/v1/reports/**` | Yes          | USER, PREMIUM   |
| `/api/v1/portfolio/**`| Yes         | USER            |
| `/api/v1/notifications/**`| Yes    | USER            |
| `/api/v1/alerts/**` | Yes           | USER            |
| `/api/v1/admin/**`  | Yes           | ADMIN           |

---

## 12. Technology Stack

| Layer                | Technology                        | Justification                                    |
|----------------------|-----------------------------------|--------------------------------------------------|
| **Framework**        | Spring Boot 3.x                   | Requested by user; mature, production-ready      |
| **Language**         | Java 21 (LTS)                     | Latest LTS with virtual threads (Project Loom)   |
| **Build**            | Gradle (Kotlin DSL)               | Modern, faster than Maven                        |
| **REST**             | Spring Web MVC                    | Standard REST endpoints                          |
| **WebSocket**        | Spring WebSocket + STOMP          | Real-time price & signal streaming               |
| **Security**         | Spring Security + JWT (jjwt)      | Stateless authentication                         |
| **Database**         | PostgreSQL 16                     | Primary relational store                         |
| **Time-Series**      | TimescaleDB (PG extension)        | Efficient OHLCV storage & time-range queries     |
| **Cache**            | Redis 7                           | Session cache, latest prices, rate limiting      |
| **Message Broker**   | Apache Kafka                      | Event-driven async processing                    |
| **ORM**              | Spring Data JPA + Hibernate       | Standard ORM with query optimization             |
| **Migration**        | Flyway                            | Versioned DB migrations                          |
| **PDF Parsing**      | Apache Tika + PDFBox              | Extract text/tables from company reports         |
| **LLM Integration**  | Spring AI + OpenAI/Gemini API     | Report analysis, summarization                   |
| **Push Notifications**| Firebase Admin SDK                | FCM push notifications to mobile                 |
| **Email**            | Spring Mail + SendGrid            | Transactional emails, digests                    |
| **Object Storage**   | MinIO (self-hosted) or AWS S3     | PDF report storage                               |
| **Search**           | Elasticsearch (optional)          | Full-text stock/report search                    |
| **Scheduling**       | Spring @Scheduled + Quartz        | Cron jobs for polling, analysis, digests         |
| **Monitoring**       | Spring Actuator + Micrometer      | Health checks, metrics                           |
| **Logging**          | SLF4J + Logback                   | Structured logging                               |
| **API Docs**         | SpringDoc OpenAPI (Swagger)       | Auto-generated API documentation                 |
| **Containerization** | Docker + Docker Compose           | Local dev & deployment                           |
| **CI/CD**            | GitHub Actions                    | Build, test, deploy pipeline                     |

### Key Dependencies (pom.xml / build.gradle highlights)

```xml
<!-- Core -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-websocket
spring-boot-starter-mail
spring-boot-starter-actuator
spring-boot-starter-validation

<!-- Database -->
postgresql
timescaledb (via PostgreSQL extension)
flyway-core
spring-boot-starter-data-redis

<!-- Messaging -->
spring-kafka

<!-- AI / LLM -->
spring-ai-openai-spring-boot-starter

<!-- PDF -->
org.apache.tika:tika-core
org.apache.pdfbox:pdfbox

<!-- Push Notifications -->
com.google.firebase:firebase-admin

<!-- JWT -->
io.jsonwebtoken:jjwt-api
io.jsonwebtoken:jjwt-impl
io.jsonwebtoken:jjwt-jackson

<!-- API Docs -->
org.springdoc:springdoc-openapi-starter-webmvc-ui

<!-- Scheduling -->
org.springframework.boot:spring-boot-starter-quartz

<!-- HTTP Client (for CSE API) -->
org.springframework.boot:spring-boot-starter-webflux  (WebClient)
```

---

## 13. Deployment Architecture

### 13.1 Docker Compose (Development)

```yaml
version: '3.8'
services:
  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [postgres, redis, kafka]
    environment:
      SPRING_PROFILES_ACTIVE: dev

  postgres:
    image: timescale/timescaledb:latest-pg16
    ports: ["5432:5432"]
    volumes: [pgdata:/var/lib/postgresql/data]

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    ports: ["2181:2181"]

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports: ["9092:9092"]
    depends_on: [zookeeper]

  minio:
    image: minio/minio
    ports: ["9000:9000", "9001:9001"]
    command: server /data --console-address ":9001"

volumes:
  pgdata:
```

### 13.2 Production Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                     Cloud Provider (AWS / GCP / DigitalOcean)│
│                                                             │
│  ┌─────────────┐     ┌──────────────────────────┐          │
│  │  Nginx /    │────▶│  Spring Boot App          │          │
│  │  Load       │     │  (Docker on ECS/GKE       │          │
│  │  Balancer   │     │   or VPS)                 │          │
│  └─────────────┘     └──────────┬───────────────┘          │
│                                  │                          │
│            ┌────────────────────┼────────────────┐         │
│            │                    │                │         │
│     ┌──────▼──────┐    ┌───────▼─────┐   ┌─────▼──────┐  │
│     │ PostgreSQL  │    │   Redis     │   │   Kafka    │  │
│     │ + Timescale │    │  (Managed)  │   │  (Managed) │  │
│     │  (RDS/      │    │             │   │            │  │
│     │   Managed)  │    └─────────────┘   └────────────┘  │
│     └─────────────┘                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## 14. Phase-wise Development Roadmap

### Phase 1 - MVP (Weeks 1-4)

| Week | Deliverable                                                        |
|------|--------------------------------------------------------------------|
| 1    | Project setup, DB schema, User module (register/login/JWT)         |
| 2    | CSE API integration, price polling, market data APIs               |
| 3    | Basic technical indicators (RSI, MACD, SMA), signal generation     |
| 4    | Firebase push notifications, watchlist, basic price alerts         |

**MVP Output:** Users can register, add stocks to watchlist, view prices, get basic BUY/SELL push notifications based on RSI + MACD signals.

### Phase 2 - Analysis Enhancement (Weeks 5-8)

| Week | Deliverable                                                        |
|------|--------------------------------------------------------------------|
| 5    | Advanced indicators (Bollinger, Stochastic, ADX), support/resistance|
| 6    | Pattern recognition engine (double top/bottom, triangles)          |
| 7    | Composite signal scoring with confidence levels                     |
| 8    | Signal history, accuracy tracking, WebSocket real-time updates     |

### Phase 3 - AI Report Agent (Weeks 9-12)

| Week | Deliverable                                                        |
|------|--------------------------------------------------------------------|
| 9    | Report ingestion pipeline (scraper + PDF parser)                   |
| 10   | LLM integration for report analysis                                |
| 11   | Fundamental scoring engine, YoY comparison                         |
| 12   | Report alerts, sector comparison                                   |

### Phase 4 - Portfolio & Intelligence (Weeks 13-16)

| Week | Deliverable                                                        |
|------|--------------------------------------------------------------------|
| 13   | Portfolio management (holdings, P&L)                               |
| 14   | Monthly investment planner with AI suggestions                     |
| 15   | Daily digest & weekly report generation                            |
| 16   | Production hardening, monitoring, load testing                     |

---

## Appendix A: CSE API Reference

Base URL: `https://www.cse.lk/api/`

| Endpoint            | Method | Params    | Description                          |
|---------------------|--------|-----------|--------------------------------------|
| `marketStatus`      | POST   | -         | Market open/closed status            |
| `marketSummery`     | POST   | -         | ASPI, Volume, Turnover               |
| `todaySharePrice`   | POST   | -         | All active stock prices              |
| `companyInfoSummery`| POST   | `symbol`  | Detail for one stock                 |
| `tradeSummary`      | POST   | -         | All trade summaries                  |
| `topGainers`        | POST   | -         | Top gaining stocks                   |
| `topLooses`         | POST   | -         | Top losing stocks                    |
| `aspiData`          | POST   | -         | ASPI index history                   |
| `snpData`           | POST   | -         | S&P SL20 index history               |
| `allSectors`        | POST   | -         | Sector indices                       |

**Required Headers:**
```
Origin: https://www.cse.lk
Referer: https://www.cse.lk/
X-Requested-With: XMLHttpRequest
Cookie: JSESSIONID=<valid_session>
```

**Note:** CSE does not provide an official API. These are reverse-engineered endpoints from their web portal. Rate limiting (0.4s delay between requests) should be implemented to avoid IP bans.

---

## Appendix B: Sample Notification Messages

| Scenario            | Push Notification Message                                                                                    |
|---------------------|--------------------------------------------------------------------------------------------------------------|
| Bullish Breakout    | "JKH.N0000 - BULLISH breakout above LKR 215 resistance! Volume 3x average. RSI: 58. Target: LKR 235. Consider buying." |
| Oversold Recovery   | "DIAL.N0000 - RSI recovering from oversold (28→35). MACD bullish crossover forming. Entry: LKR 12.50."      |
| Death Cross Warning | "COMB.N0000 - BEARISH signal. SMA(50) crossed below SMA(200). Consider reducing position."                   |
| Report Published    | "New Annual Report: LOLC Holdings (2024/25) published. AI analysis: Revenue +12%, ROE 18%. Score: 78/100."   |
| Monthly Plan        | "May 2026 Investment Plan ready! 3 stocks recommended based on your LKR 50,000 budget. Tap to review."       |

---

*Document Version: 1.0 | Date: 2026-05-03 | Author: Devin (AI Assistant)*
