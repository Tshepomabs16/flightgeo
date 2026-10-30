package io.github.tshepomabs16.flightgeo.zones;

/** What to assume when a position has no altitude but a zone has a floor or a ceiling. */
public enum UnknownAltitude {
    /** Treat it as within the band: the cautious choice for zones that must be avoided. */
    WITHIN_BAND,
    /** Treat it as outside the band: the cautious choice for zones that must be stayed inside. */
    OUTSIDE_BAND
}
