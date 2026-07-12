# Beta QA Record

## Candidate

- Version: 0.9.0-beta.1
- Date: 2026-07-12
- Branch: semantic-bank-search-implementation
- RuneLite build dependency: latest.release at candidate build time
- Java: 11

## Automated Results

- Clean test and jar build: passed
- Tests: 172 passed, 0 failed, 0 errors
- Native beta query pack: passed
- 1,000-item ranking workload: passed
- Runtime safety scan: clean
- Git whitespace check: clean
- Candidate jar: semantic-bank-search-0.9.0-beta.1.jar

## Client Startup

- Development RuneLite client launched successfully.
- RuneLite reached the login screen without a plugin startup error.
- Account login and credentials were not automated.

## Manual In-Game QA

Status: pending user-authenticated bank session.

Complete every step in [Beta Release Checklist](BETA_RELEASE_CHECKLIST.md), recording:

- RuneLite version
- Operating system
- Approximate bank size
- Compatibility plugins enabled
- Failed query, expected item, actual item, and screenshot when applicable

## Release Decision

Do not update the Plugin Hub marker to this candidate until native bank filtering and Bank Tags compatibility have been exercised in an authenticated bank session.
