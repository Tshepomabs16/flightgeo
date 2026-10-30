# flightgeo

[![Maven Central](https://img.shields.io/maven-central/v/io.github.tshepomabs16/flightgeo-zones?label=Maven%20Central)](https://central.sonatype.com/namespace/io.github.tshepomabs16)
[![CI](https://github.com/Tshepomabs16/flightgeo/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Tshepomabs16/flightgeo/actions/workflows/ci.yml)
[![Licence](https://img.shields.io/github/license/Tshepomabs16/flightgeo?label=licence)](LICENSE)

Small, dependency-free Java libraries for the geometry of aircraft and vehicle routes.

| Module | What it does |
|---|---|
| **flightgeo-zones** | Is this point, leg or route inside a zone? Polygon and circle zones with optional floor and ceiling heights, validation, and a monitor that reports entries and exits. |
| **flightgeo-conformance** | Was the route flown as planned? Compares planned waypoints with a recorded track: which waypoints were reached and when, the height flown, the totals, and one verdict with its reasons. |

Both need Java 17 or later and have no runtime dependencies. `flightgeo-conformance` uses `flightgeo-zones` for its geometry.

## Install

Maven:

```xml
<dependency>
  <groupId>io.github.tshepomabs16</groupId>
  <artifactId>flightgeo-zones</artifactId>
  <version>0.1.0</version>
</dependency>
<dependency>
  <groupId>io.github.tshepomabs16</groupId>
  <artifactId>flightgeo-conformance</artifactId>
  <version>0.1.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("io.github.tshepomabs16:flightgeo-zones:0.1.0")
implementation("io.github.tshepomabs16:flightgeo-conformance:0.1.0")
```

## Zones

```java
import io.github.tshepomabs16.flightgeo.zones.*;

Zone airfield = Zone.circle("airfield", GeoPoint.of(47.4647, 8.5492), 3_000, AltitudeBand.upTo(150));
Zone reserve = Zone.polygon("reserve", List.of(
        GeoPoint.of(47.40, 8.50), GeoPoint.of(47.40, 8.56),
        GeoPoint.of(47.44, 8.56), GeoPoint.of(47.44, 8.50)));

ZoneChecker checker = ZoneChecker.defaults();
checker.contains(airfield, GeoPoint.of(47.47, 8.55, 120));   // true: inside, under the 150 m ceiling
checker.contains(airfield, GeoPoint.of(47.47, 8.55, 200));   // false: above it

List<GeoPoint> route = List.of(GeoPoint.of(47.38, 8.53, 60), GeoPoint.of(47.46, 8.53, 60));
for (RouteConflict c : checker.routeConflicts(List.of(airfield, reserve), route)) {
    System.out.println(c.zone().id() + ": " + c.kind() + " at " + c.index());
}
// airfield: LEG_CROSSES at 0   (the leg ends 1.5 km from the airfield, at 60 m, under its ceiling)
// reserve: LEG_CROSSES at 0
```

Check a zone before you rely on it:

```java
List<String> problems = ZoneValidator.problems(zone);          // crossed edges, upside-down band, a corner at 0, 0...
List<String> withLimit = ZoneValidator.problems(zone, 120.0);  // also: a floor no permitted flight could reach
```

Follow moving things and hear only about changes:

```java
ZoneMonitor<String> monitor = new ZoneMonitor<>();
for (ZoneEvent<String> e : monitor.update("aircraft-7", position, zones)) {
    if (e.transition() == ZoneTransition.ENTERED) {
        alert(e.subject() + " entered " + e.zone().id());
    }
}
```

### How the zone checks behave

- **Edges count as inside.** A point within half a metre of a polygon's edge is in the zone (`withEdgeTolerance` changes this). Band edges are inclusive too.
- **Unknown altitude.** When a position has no altitude and the zone has a floor or ceiling, the default treats it as within the band, the cautious choice for zones to avoid. Use `withUnknownAltitude(UnknownAltitude.OUTSIDE_BAND)` for zones that must be stayed inside.
- **Heights are compared as given.** Keep one datum throughout (above take-off, or above sea level).
- **Accuracy.** Distances are great-circle. Polygon and leg tests use a flat projection around the point being tested, accurate for zones and legs up to a few tens of kilometres. Zones may cross the 180° meridian; zones around the poles are not supported.
- **Legs between waypoints are straight lines** on that projection; a climb between two waypoints is taken as covering every height between them.

## Conformance

```java
import io.github.tshepomabs16.flightgeo.conformance.*;

List<Waypoint> plan = List.of(
        Waypoint.at(47.00, 8.00, 100),
        Waypoint.at(47.01, 8.00, 100).holding(30),   // 30 s planned at waypoint 2
        Waypoint.at(47.01, 8.01, 100));

List<TrackFix> track = TrackCleaner.clean(rawFixes).fixes();   // time order, no 0,0 fixes or impossible jumps

ConformanceReport report = new ConformanceChecker().check(plan, track,
        FlightContext.completedFlight().withPlannedSpeed(12).withAltitudeLimit(120));

report.verdict();      // AS_PLANNED, WITH_DEVIATIONS, PARTLY_COMPLETED, NOT_COMPLETED or NOT_VERIFIED
report.reasons();      // e.g. ["Flown more than 10 m off the planned height at waypoint 2"]
for (WaypointResult w : report.waypoints()) {
    System.out.println(w.sequence() + " reached=" + w.reached() + " arrival=" + w.actualArrivalSeconds() + "s");
}
report.comparison(Measure.DISTANCE);   // planned against actual metres
```

### The verdict

| Verdict | When |
|---|---|
| `NOT_COMPLETED` | The flight was aborted (`FlightContext.abortedFlight()`). |
| `NOT_VERIFIED` | No track was recorded, so nothing can be checked. |
| `PARTLY_COMPLETED` | At least one waypoint was never reached. |
| `WITH_DEVIATIONS` | Every waypoint was reached, but out of order, off its planned height, through a restricted zone, or above the altitude limit. |
| `AS_PLANNED` | None of the above. |

Each reason is also a structured `Finding` with a type and the waypoint numbers it concerns, so you can build your own wording. A `Note` records a return to the launch point.

### What counts as what

- **Reached:** the track, or the straight line between two consecutive readings, comes within the reached radius of the waypoint.
- **Flown height:** the altitude when the aircraft left the waypoint on its first visit. At a take-off point this is the height it climbed to, not the first metre of the climb.
- **Arrival:** seconds from take-off (the first moving reading) to the first reading within the radius.
- **Airborne time:** steps that start moving, so a landing in the middle of a flight is left out while a short drop in signal is not. Pass `withActiveWindows` to count only the stretches the flight was active.

### Options

`ConformanceOptions.defaults()` suits small aircraft on routes of a few kilometres. Change any value with its `with` method.

| Option | Default | Meaning |
|---|---|---|
| `reachedRadiusMetres` | 10 | A waypoint is reached when the track comes this close. |
| `altitudeToleranceMetres` | 10 | A reached waypoint flown further than this off its planned height is a deviation. |
| `returnRadiusMetres` | 25 | Ending this close to the first waypoint counts as returning to launch. |
| `maxGap` | 5 minutes | Readings further apart are not joined when measuring or matching the track. |
| `movingSpeedMs`, `movingAltitudeMetres` | 1.0, 2.0 | A reading at or above either is moving. |
| `defaultSpeedMs` | 10 | Planned speed when the flight gives none; the report says it was assumed. |
| `plannedReturnToStart` | true | The planned route ends with a leg back to the first waypoint. |

### Planning

```java
double metres = RoutePlanner.distanceMetres(plan, true);
RoutePlanner.RouteEstimate estimate = RoutePlanner.estimate(plan, 12, true);   // distance and flight seconds
int batteries = RoutePlanner.batteriesNeeded(metres, 8_000, 20);              // 8 km per battery, 20% reserve
```

## Versioning

flightgeo follows [Semantic Versioning](https://semver.org). While the version is 0.x, minor releases may change the API; each change is listed in [CHANGELOG.md](CHANGELOG.md).

## Building

```bash
./mvnw verify
```

## Contributing

Issues and pull requests are welcome. Please add a test for any change in behaviour; see [CONTRIBUTING.md](CONTRIBUTING.md).

## Licence

[Apache License 2.0](LICENSE).
