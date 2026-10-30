package io.github.tshepomabs16.flightgeo.conformance;

/**
 * Something worth knowing that doesn't affect the verdict.
 *
 * @param type what kind of note it is
 * @param message the note as a plain-English sentence
 */
public record Note(Type type, String message) {

    /** The kinds of note. */
    public enum Type {
        /** The track ended back at the first waypoint. */
        RETURNED_TO_LAUNCH
    }
}
