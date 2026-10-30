# Changelog

All notable changes are listed here. The format follows [Keep a Changelog](https://keepachangelog.com),
and versions follow [Semantic Versioning](https://semver.org).

## [Unreleased]

## [0.1.0]

### Added
- `flightgeo-zones`: polygon and circle zones with optional floor and ceiling heights; point, leg and route
  checks with an edge tolerance and an unknown-altitude policy; zone validation; `ZoneMonitor` for entries and exits.
- `flightgeo-conformance`: `ConformanceChecker` comparing planned waypoints with a recorded track, with a verdict,
  findings and route-wide comparisons; `TrackCleaner`, `TrackStats` and `RoutePlanner`.
