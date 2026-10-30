package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/** Distance, time in the air and altitude figures for a recorded track, in time order. */
public final class TrackStats {

    private TrackStats() {}

    /**
     * Whether a reading is moving or airborne by the options' thresholds.
     *
     * @param fix the reading
     * @param options the thresholds
     * @return true when it is moving
     */
    public static boolean isMoving(TrackFix fix, ConformanceOptions options) {
        return (fix.groundSpeedMs() != null && fix.groundSpeedMs() >= options.movingSpeedMs())
                || (fix.altitude() != null && fix.altitude() >= options.movingAltitudeMetres());
    }

    /**
     * Distance along the track, never bridging a gap longer than the options' maximum.
     *
     * @param track the readings in time order
     * @param options the thresholds
     * @return the distance in metres
     */
    public static double distanceMetres(List<TrackFix> track, ConformanceOptions options) {
        double metres = 0;
        for (int i = 1; i < track.size(); i++) {
            TrackFix a = track.get(i - 1);
            TrackFix b = track.get(i);
            if (gapOk(a, b, options)) {
                metres += Geodesy.distanceMetres(a.position(), b.position());
            }
        }
        return metres;
    }

    /**
     * Time in the air over the whole track.
     *
     * @param track the readings in time order
     * @param options the thresholds
     * @return the seconds airborne; zero when no reading was moving
     */
    public static long airborneSeconds(List<TrackFix> track, ConformanceOptions options) {
        if (track.size() < 2 || track.stream().noneMatch(f -> isMoving(f, options))) {
            return 0;
        }
        TimeWindow whole = new TimeWindow(track.get(0).time(), track.get(track.size() - 1).time());
        return airborneSeconds(track, List.of(whole), options);
    }

    /**
     * Time in the air within the windows: a mid-flight landing is left out, a short signal drop is not.
     *
     * @param track the readings in time order
     * @param windows the stretches to count; with no moving reading, their length stands in
     * @param options the thresholds
     * @return the seconds airborne
     */
    public static long airborneSeconds(List<TrackFix> track, List<TimeWindow> windows, ConformanceOptions options) {
        if (track.stream().noneMatch(f -> isMoving(f, options))) {
            return windows.stream().mapToLong(TimeWindow::seconds).sum();
        }
        long total = 0;
        for (TimeWindow window : windows) {
            List<TrackFix> inside = track.stream().filter(f -> window.contains(f.time())).toList();
            for (int k = 1; k < inside.size(); k++) {
                TrackFix a = inside.get(k - 1);
                TrackFix b = inside.get(k);
                if (!isMoving(a, options)) {
                    continue;
                }
                long gap = Duration.between(a.time(), b.time()).getSeconds();
                if (gap > 0 && (isMoving(b, options) || gap <= options.maxGap().getSeconds())) {
                    total += gap;
                }
            }
        }
        return total;
    }

    /**
     * The highest altitude recorded.
     *
     * @param track the readings
     * @return the highest altitude in metres, or empty when no reading had one
     */
    public static OptionalDouble highestAltitude(List<TrackFix> track) {
        return track.stream().filter(f -> f.altitude() != null).mapToDouble(TrackFix::altitude).max();
    }

    /**
     * Each unbroken stretch above a limit. Readings without an altitude neither start nor end one.
     *
     * @param track the readings in time order
     * @param limitMetres the altitude limit in metres
     * @return the stretches above it, in time order
     */
    public static List<AltitudeExceedance> altitudeExceedances(List<TrackFix> track, double limitMetres) {
        List<AltitudeExceedance> out = new ArrayList<>();
        TrackFix first = null;
        TrackFix last = null;
        double highest = 0;
        for (TrackFix f : track) {
            Double alt = f.altitude();
            if (alt == null) {
                continue;
            }
            if (alt > limitMetres) {
                if (first == null) {
                    first = f;
                    highest = alt;
                }
                last = f;
                highest = Math.max(highest, alt);
            } else if (first != null) {
                out.add(new AltitudeExceedance(first.time(), last.time(), highest));
                first = null;
            }
        }
        if (first != null) {
            out.add(new AltitudeExceedance(first.time(), last.time(), highest));
        }
        return out;
    }

    static boolean gapOk(TrackFix a, TrackFix b, ConformanceOptions options) {
        return Duration.between(a.time(), b.time()).compareTo(options.maxGap()) <= 0;
    }
}
