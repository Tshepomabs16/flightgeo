# Publishing

How a release gets from this repository to Maven Central. The one-time setup takes about half an hour;
after that, a release is a tag.

## One-time setup

1. **Put the code on GitHub.** Create a public repository named `flightgeo` under your account and push to it.
   The `scm` and `url` entries in `pom.xml` already point at `github.com/Tshepomabs16/flightgeo`.

2. **Claim the namespace on Maven Central.** Sign in at <https://central.sonatype.com> with your GitHub account.
   Signing in with GitHub verifies the `io.github.tshepomabs16` namespace automatically; check it shows as
   verified under *Namespaces*.

3. **Make a publishing token.** In the Central Portal, open your account and choose *Generate User Token*.
   It gives a username and a password; keep both.

4. **Make a signing key.** Central needs every file signed.

   ```bash
   gpg --full-generate-key                              # RSA 4096, your name and email
   gpg --list-secret-keys --keyid-format long           # note the key id after "rsa4096/"
   gpg --keyserver keyserver.ubuntu.com --send-keys KEYID
   gpg --armor --export-secret-keys KEYID > private.asc # for the GitHub secret, then delete the file
   ```

5. **Add four secrets to the GitHub repository** (*Settings, Secrets and variables, Actions*):

   | Secret | Value |
   |---|---|
   | `MAVEN_CENTRAL_USERNAME` | the token username from step 3 |
   | `MAVEN_CENTRAL_PASSWORD` | the token password from step 3 |
   | `GPG_PRIVATE_KEY` | the contents of `private.asc` |
   | `GPG_PASSPHRASE` | the key's passphrase |

## Each release

1. Move the entries under *Unreleased* in `CHANGELOG.md` to a new version heading, and update the version in
   the README's install snippets.
2. Commit, then tag and push:

   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```

3. The *Release* workflow sets the version from the tag, builds, signs and uploads both modules.
4. Open *Deployments* in the Central Portal, check the files, and press *Publish*. Releases are not published
   automatically (`autoPublish` is false in `pom.xml`), so a mistake can still be dropped at this point.
   Once published they reach Maven Central within about half an hour, and the Javadoc appears on
   <https://javadoc.io> by itself.
5. Bump the version in the POMs to the next snapshot, for example `0.2.0-SNAPSHOT`:

   ```bash
   ./mvnw versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false
   ```

## Publishing from your own machine instead

Put the token in `~/.m2/settings.xml`:

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>TOKEN-USERNAME</username>
      <password>TOKEN-PASSWORD</password>
    </server>
  </servers>
</settings>
```

Set the release version, then run `./mvnw -P release deploy`. GPG asks for the key's passphrase.
