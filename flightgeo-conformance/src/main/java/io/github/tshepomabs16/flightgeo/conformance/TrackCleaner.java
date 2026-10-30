package io.github.tshepomabs16.flightgeo.conformance;

import io.github.tshepomabs16.flightgeo.zones.Coordinates;
import io.github.tshepomabs16.flightgeo.zones.Geodesy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Puts a raw track in time order and drops readings that can't be real: missing fixes and impossible jumps. */
public final class TrackCleaner {

    /** A jump implying more than this many metres per second from the last kept reading is dropped. */
    public static final double DEFAULT_MAX_SPEED_MS = 40;

    private TrackCleaner() {}

    /** The kept readings, and how many were dropped for each reason. */
    public record Result(List<TrackFix> fixes, int droppedMissing, int droppedJumps) {

        public Result {
            fixes = List.copyOf(fixes);
        }
    }

    public static Result clean(List<TrackFix> track) {
        return clean(track, DEFAULT_MAX_SPEED_MS);
    }

    /** Sorted by time, without 0, 0 fixes or jumps faster than {@code maxSpeedMs}. The first usable reading is trusted. */
    public static Result clean(List<TrackFix> track, double maxSpeedMs) {
        if (!Double.isFinite(maxSpeedMs) || maxSpeedMs <= 0) {
            throw new IllegalArgumentException("maxSpeedMs must be greater than zero");
        }
        List<TrackFix> sorted = new ArrayList<>(track);
        sorted.sort(Comparator.comparing(TrackFix::time));
        List<TrackFix> kept = new ArrayList<>();
        int missing = 0;
        int jumps = 0;
        for (TrackFix f : sorted) {
            if (!Coordinates.isUsable(f.position().latitude(), f.position().longitude())) {
                missing++;
                continue;
            }
            if (!kept.isEmpty()) {
                TrackFix last = kept.get(kept.size() - 1);
                double seconds = (f.time().toEpochMilli() - last.time().toEpochMilli()) / 1000.0;
                if (seconds > 0 && Geodesy.distanceMetres(last.position(), f.position()) / seconds > maxSpeedMs) {
                    jumps++;
                    continue;
                }
            }
            kept.add(f);
        }
        return new Result(kept, missing, jumps);
    }
}
