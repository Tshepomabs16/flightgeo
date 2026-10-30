package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import java.util.ArrayList;
import java.util.List;

/** Distance, timings and battery count for a planned route, before it is flown. */
public final class RoutePlanner {

    private RoutePlanner() {}

    /** Distance in metres and flight time in seconds: every leg at the planned speed, plus every hold. */
    public record RouteEstimate(double distanceMetres, int flightSeconds) {}

    /** Length of the legs in order, plus the leg back to the first waypoint when {@code returnToStart}. */
    public static double distanceMetres(List<Waypoint> route, boolean returnToStart) {
        if (route.size() < 2) {
            return 0;
        }
        double metres = 0;
        for (int i = 1; i < route.size(); i++) {
            metres += leg(route.get(i - 1), route.get(i));
        }
        return returnToStart ? metres + leg(route.get(route.size() - 1), route.get(0)) : metres;
    }

    /** Seconds from the first waypoint to each waypoint at the planned speed, holding at each earlier one. */
    public static List<Integer> plannedArrivals(List<Waypoint> route, double speedMs) {
        requireSpeed(speedMs);
        List<Integer> out = new ArrayList<>();
        double t = 0;
        for (int i = 0; i < route.size(); i++) {
            if (i > 0) {
                t += leg(route.get(i - 1), route.get(i)) / speedMs + route.get(i - 1).holdSeconds();
            }
            out.add((int) Math.round(t));
        }
        return out;
    }

    public static RouteEstimate estimate(List<Waypoint> route, double speedMs, boolean returnToStart) {
        requireSpeed(speedMs);
        double distance = distanceMetres(route, returnToStart);
        int holds = route.stream().mapToInt(Waypoint::holdSeconds).sum();
        return new RouteEstimate(distance, (int) Math.round(distance / speedMs) + holds);
    }

    /** Batteries needed: the distance over the range left after the reserve, rounded up; zero for no distance. */
    public static int batteriesNeeded(double distanceMetres, double rangePerBatteryMetres, double reservePercent) {
        if (!Double.isFinite(rangePerBatteryMetres) || rangePerBatteryMetres <= 0) {
            throw new IllegalArgumentException("Range per battery must be greater than zero");
        }
        if (!Double.isFinite(reservePercent) || reservePercent < 0 || reservePercent >= 100) {
            throw new IllegalArgumentException("Reserve must be from 0 up to (not including) 100 percent");
        }
        if (distanceMetres <= 0) {
            return 0;
        }
        double usable = rangePerBatteryMetres * (1 - reservePercent / 100.0);
        return (int) Math.ceil(distanceMetres / usable);
    }

    private static double leg(Waypoint a, Waypoint b) {
        return Geodesy.distanceMetres(a.position(), b.position());
    }

    private static void requireSpeed(double speedMs) {
        if (!Double.isFinite(speedMs) || speedMs <= 0) {
            throw new IllegalArgumentException("Speed must be greater than zero");
        }
    }
}
