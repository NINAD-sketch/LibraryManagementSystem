# Validation notes

## Checks completed in the generation environment

- Environment: OpenJDK 21.0.12.
- Production source: compiled successfully using `javac --release 21 -Xlint:all`.
- Demo: ran successfully and printed a granted loan, an overdue fine of `7.50`, and a paid-fine confirmation.
- Test source: all 13 test methods completed successfully through a temporary local compatibility harness that supplied the small JUnit annotation/assertion API used by these tests. The captured output is in `validation/local-harness-output.txt`.

## Checks not completed here

Maven is not installed in the generation environment, and external dependency downloads were unavailable. Therefore, the project has **not** been run with the official JUnit 5 engine or JaCoCo in this environment, and an actual coverage percentage has not been measured. Do not describe the local compatibility-harness result as an official Maven/JUnit report.

On a machine with Maven 3.9+ and network access to resolve dependencies, run:

```bash
mvn clean verify
```

This executes the configured JUnit 5 suite, creates the JaCoCo HTML report at `target/site/jacoco/index.html`, and enforces an 80% bundle line-coverage minimum. If the threshold is not met, Maven will fail the `verify` phase and the report will show what needs more testing.
