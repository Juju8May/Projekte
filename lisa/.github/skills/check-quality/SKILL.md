---
name: check-quality
description: 'Use when reviewing newly written Java classes in this Maven project for implementation quality, requirement alignment, unit-test quality, mutation testing, SonarQube findings, compilation, regressions, and JaCoCo coverage.'
argument-hint: '[class or package to check]'
user-invocable: true
---

# Check Quality

## Purpose

Evaluate newly written or changed Java classes against the current project requirements and quality standards. Keep the review focused on the requested classes and their direct behavior.

## Scope Rules

- Check only the classes named by the user or required directly to verify their behavior.
- Exclude DTOs, records, generated code, configuration-only data containers, and trivial getters/setters unless they contain meaningful behavior in scope.
- Do not modify, delete, rename, reformat, or repair existing test files.
- Do not create, modify, delete, rename, or reformat tests. This skill only evaluates tests that already exist.
- Do not change production code unless the user explicitly requests implementation fixes. Report production defects with file and line references instead.
- Check the current implementation, `pom.xml`, dependencies, configuration, and stated requirements. Do not rely on stale assumptions or old APIs.
- Detect whether the project configures Benji, PIT, or another mutation-testing framework before running mutation analysis. Do not silently treat line coverage as a substitute for mutation coverage.
- Check SonarQube-for-IDE findings for the reviewed Java classes when the extension is available in the workspace.
- Include SonarQube in the assessment only when usable IDE findings or a configured server analysis are actually accessible. Otherwise omit it from findings and the final report.

## Quality Checks

### 1. Requirements and design

- Read the changed class and its direct collaborators.
- Compare the implementation with the current request, project documentation, public API, and security requirements.
- Check naming, package placement, visibility, constructor dependencies, and consistency with nearby code.
- Identify unreachable code, duplicated logic, accidental API changes, missing validation, unsafe defaults, and unclear error behavior.
- Check null, empty, invalid, boundary, and failure paths where relevant.

### 2. Test coverage

- Inspect existing tests to understand current coverage, but leave them unchanged.
- Determine whether the existing tests use the narrowest suitable test type: JUnit 5 and Mockito for isolated logic, `@WebMvcTest` for HTTP behavior, or integration testing only when real Spring/database wiring is required.
- Check whether existing tests use `@InjectMocks` for the class under test with `@Mock` collaborators when constructor injection is appropriate.
- Check that existing tests do not mock the class under test.
- Evaluate existing tests only for required behavior of the reviewed classes. Do not create tests for DTOs or trivial data containers.
- Cover success, invalid input, missing data, authorization, exceptions, and important branches as applicable.
- Avoid unnecessary stubbing and verify observable behavior rather than private implementation details.

### 3. Focused validation

After identifying the existing focused tests, run them before running a broader suite:

```bash
cd api
mvn -Dtest=TargetTest test
```

If the focused test fails:

1. Read the complete compiler, JUnit, or Mockito failure.
2. Decide whether it is caused by an existing test, the reviewed class, or an environment problem.
3. Do not change tests. Report the failure and, only when explicitly requested, propose a production-code fix.
4. Rerun the same focused command.

Pay special attention to:

- `PotentialStubbingProblem`: Mockito arguments do not match the actual invocation.
- `UnnecessaryStubbingException`: remove unused stubbing.
- `InvalidUseOfMatchersException`: use matchers consistently for all arguments in one invocation.
- `NullPointerException`: verify constructor setup and optional initialization.
- compilation errors from stale method names, record components, imports, packages, or duplicate braces.

### 4. Full validation

After focused tests pass, run the complete suite:

```bash
cd api
mvn verify
```

Do not claim success without checking the actual Maven result. Existing test failures must be reported; do not alter existing tests to hide them.

### 5. JaCoCo analysis

Use the generated report at `api/target/site/jacoco/jacoco.csv` and `api/target/site/jacoco/index.html`.

- Evaluate coverage for the reviewed classes, especially missed branches and error paths.
- Identify classes with meaningfully low coverage.
- Mark low-coverage classes as quality gaps when they are relevant to the request.
- Do not chase coverage percentages by testing DTOs, records, generated code, or trivial accessors.
- Do not add tests after coverage analysis. Report low-coverage classes and missing test cases as quality gaps.

### 6. Mutation and unit-test quality

- Assess whether the existing unit tests detect behavior changes, rather than only executing lines. Check assertions for meaningful outcomes, boundary values, rejected input, exceptions, collaborator failures, and important branches.
- Inspect the Maven configuration for an already configured Benji, PIT, or other mutation-testing plugin. Use the project's configured framework and its documented command when available.
- If no mutation framework is configured, use PIT for the focused Java classes when the dependency can be resolved without modifying project files. A typical Maven command is:

```bash
mvn org.pitest:pitest-maven:mutationCoverage \
	-DtargetClasses='com.lisa.api.<package>.*' \
	-DtargetTests='com.lisa.api.<package>.*Test'
```

- Keep mutation runs focused on the reviewed production classes and their direct unit tests. Exclude DTOs, records, generated code, configuration-only classes, and infrastructure that cannot be meaningfully tested as units.
- Inspect the mutation report, normally under `target/pit-reports/`, or the report directory produced by the configured framework. Record the mutation score, killed/survived/no-covered mutants, and the surviving mutants relevant to the reviewed behavior.
- Treat surviving mutants in validation, authorization, error handling, boundary conditions, and business rules as test-quality gaps. Do not demand artificial tests for equivalent mutants, generated code, logging-only changes, or framework boilerplate; explain exclusions.
- If the mutation command cannot run because the framework is not configured, dependencies are unavailable, or the selected classes are incompatible, report `not available` with the exact reason and continue with the other checks.

### 7. SonarQube quality analysis

- SonarQube is optional. If neither IDE findings nor a configured server analysis is accessible, skip this section completely: do not report it as `not available`, an unresolved issue, or a quality gap.
- Inspect `api/pom.xml`, `sonar-project.properties`, Maven profiles, and repository configuration for SonarQube settings before running a server analysis.
- When accessible, use SonarQube-for-IDE to inspect the reviewed Java classes for bugs, vulnerabilities, security hotspots, code smells, and relevant quality-rule findings. Keep the review limited to the requested classes and direct behavior.
- Treat confirmed SonarQube findings in production code as review findings, ordered by severity. Do not report informational or unrelated findings as defects.
- When a SonarQube server, project key, and authentication are configured, run the scanner from the Maven project directory:

```bash
cd api
mvn verify sonar:sonar
```

- Preserve the existing `verify` lifecycle so JaCoCo is generated before SonarQube reads coverage data. Use `-Dsonar.host.url=...` and the configured secure token mechanism only when required by the local setup; never print or commit tokens.
- If only IDE analysis is accessible, record its findings. Do not claim a server quality gate passed without an actual scanner result.
- When SonarQube is included, record the scanner command, project key, dashboard/report location, quality-gate result, and relevant findings. If access is missing, omit SonarQube instead of reporting an unavailable analysis.

### 8. Cross-analyze quality results

- Analyze JaCoCo, PIT, and Benji results together for the reviewed production classes. Include SonarQube only when usable findings were obtained. Do not report tool output as a quality conclusion without interpreting its relevance to the requested behavior.
- Correlate low line or branch coverage with surviving PIT or Benji mutants. Treat a covered line with surviving behavior-changing mutants as evidence that tests execute code without sufficiently checking its outcome.
- Correlate SonarQube bugs, vulnerabilities, security hotspots, and code smells with mutation results and existing test cases. Prioritize findings that affect validation, authorization, error handling, persistence, security, or business rules.
- Distinguish clearly between:
	- production defects confirmed by implementation review or SonarQube;
	- unit-test weaknesses indicated by surviving PIT or Benji mutants;
	- coverage gaps shown by JaCoCo;
	- equivalent, generated, framework, or otherwise excluded mutants;
	- analyses that could not be measured.
- When PIT and Benji both ran, compare their mutation scores and surviving mutants. Explain differences caused by mutation operators, class filters, test filters, excluded code, or tool configuration instead of combining scores into one number.
- When only one mutation framework ran, state which framework produced the authoritative mutation result and why the other was not measured or not available.
- For every high- or medium-risk finding, name the affected production class, the evidence source, the relevant test or missing test behavior, and the recommended next action. Do not create or modify tests during this review.

## Final Report

Report findings before the summary, ordered by severity:

- critical or high-risk defects
- medium-risk defects
- low-risk issues and quality gaps
- assumptions or unresolved environment problems

Then provide:

- existing test files inspected
- existing tests left unchanged
- focused test result
- full-suite result
- JaCoCo coverage for the reviewed classes
- mutation-testing framework used, command, report location, mutation score, and surviving relevant mutants
- SonarQube-for-IDE findings and, when accessible, scanner command, project key, and quality-gate result; omit this item entirely when SonarQube is inaccessible
- cross-analysis of JaCoCo, PIT, and Benji; include SonarQube only when its findings are accessible
- tested production classes listed separately
- untested production classes listed separately
- commands that were actually run

Use `not measured` or `not available` rather than estimating any result. Distinguish line/branch coverage from mutation coverage.

## Completion Checklist

- [ ] Current requirements and project configuration checked
- [ ] Only requested/relevant classes reviewed
- [ ] DTOs and trivial data containers excluded
- [ ] Existing tests preserved unchanged
- [ ] No tests created or modified
- [ ] Existing `@InjectMocks` usage checked where appropriate
- [ ] Existing focused tests run before the full suite
- [ ] Full Maven suite run after focused success
- [ ] JaCoCo report evaluated
- [ ] Unit-test assertions and important behavior branches evaluated
- [ ] Benji, PIT, or another mutation-testing framework checked and run when available
- [ ] Mutation report and surviving relevant mutants evaluated, or the concrete unavailability reason reported
- [ ] SonarQube-for-IDE findings evaluated for the reviewed classes
- [ ] SonarQube scanner and quality gate run when server configuration is available, or unavailability reported
- [ ] JaCoCo, SonarQube, PIT, and Benji results cross-analyzed per reviewed class
- [ ] Surviving mutants, SonarQube findings, coverage gaps, exclusions, and unavailable analyses distinguished
- [ ] Tested and untested production classes listed separately
- [ ] Findings and actual validation results reported
- [ ] SonarQube evaluated only when IDE findings or server analysis are accessible; otherwise omitted from the assessment
- [ ] SonarQube quality gate reported only when the scanner actually ran
- [ ] JaCoCo, PIT, and Benji results cross-analyzed; include SonarQube only when accessible
