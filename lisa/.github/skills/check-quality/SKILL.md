---
name: check-quality
description: 'Use when reviewing newly written Java classes in this Maven project for implementation quality, requirement alignment, tests, compilation, regressions, and JaCoCo coverage.'
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
- tested production classes listed separately
- untested production classes listed separately
- commands that were actually run

Use `not measured` or `not available` rather than estimating any result.

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
- [ ] Tested and untested production classes listed separately
- [ ] Findings and actual validation results reported
