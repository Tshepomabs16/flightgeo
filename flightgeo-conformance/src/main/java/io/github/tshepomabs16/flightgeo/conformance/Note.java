package io.github.tshepomabs16.flightgeo.conformance;

/** Something worth knowing that doesn't affect the verdict. */
public record Note(Type type, String message) {

    public enum Type { RETURNED_TO_LAUNCH }
}
