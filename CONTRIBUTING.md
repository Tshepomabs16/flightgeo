# Contributing

Thanks for helping. A few things keep the libraries small and dependable:

- **No runtime dependencies.** Tests may use JUnit and AssertJ; the published jars use only the JDK.
- **Java 17.** Don't use language features from later versions.
- **A test for every change in behaviour.** Name tests after what they prove, for example
  `aLegCrossingAPolygonTouchesIt_thoughNeitherEndIsInside`.
- **Keep comments short**, two lines at most. Longer explanations belong in the README or the pull request.

Build and test everything with:

```bash
./mvnw verify
```

Open an issue first for larger changes, so we can agree the shape of the API before you write it.
