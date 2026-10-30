package io.github.tshepomabs16.flightgeo.zones;

/** What changed for a subject and a zone since its last known position. */
public enum ZoneTransition {
    /** It was outside last time and is inside now. */
    ENTERED,
    /** It was inside last time and is outside now. */
    EXITED,
    /** The first position seen for this subject and zone was inside it. */
    FIRST_SEEN_INSIDE,
    /** The first position seen for this subject and zone was outside it. */
    FIRST_SEEN_OUTSIDE
}
