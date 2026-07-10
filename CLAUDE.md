Here are coding instructions:
1)don't edit generated files inside build/

Build notes (Claude Code on the web):
- Build with the pre-installed `gradle` (`/opt/gradle`), not `./gradlew`. The
  wrapper's distribution download (`gradle-9.5.1-bin.zip`) redirects to GitHub,
  which is blocked by the web environment's egress policy, so `./gradlew` fails.
- The build's Gradle toolchain requires JDK 25, which a fresh web-session
  container doesn't ship with. Install it via the environment's Setup script
  (`apt-get install -y openjdk-25-jdk-headless`), not from this repo.