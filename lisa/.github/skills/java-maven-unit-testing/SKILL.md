---
name: java-maven-unit-testing
description: 'Use when working on Java Maven projects that need unit-test analysis, test planning, JUnit 5 test creation, Maven test execution, iterative test-failure fixes, or JaCoCo coverage evaluation.'
argument-hint: '[class, package, or behavior to test]'
user-invocable: true
---

# Java Maven Unit Testing

## Purpose

Create focused unit tests for Java Maven projects without requiring an extra approval step from the user. Generate tests only for the production classes explicitly selected by the user or directly required to validate that selection.

Existing tests are protected: do not modify, delete, rename, or reformat existing test files. If an existing test fails, report the failure separately instead of changing that test unless the user explicitly asks for existing tests to be repaired.

## Selection Rule

- Start only from the classes, packages, or behaviors explicitly named by the user.
- Do not generate tests for unrelated classes, sibling packages, DTOs, records, generated code, or trivial accessors.
- If the user names a package, generate tests only for production classes in that package that are directly relevant to the requested behavior.
- If the user names a single class, do not infer or expand to additional classes without a direct dependency or explicit requirement.
- If a class is not clearly selected, ask for the exact class or package before writing tests.

## Workflow

### 1. Identify the Java Maven project

- Locate the project root by finding `pom.xml`.
- Read the relevant `pom.xml` before editing.
- Confirm the Java version, Spring/JUnit/Mockito dependencies, test plugins, and whether JaCoCo is already configured.
- Use the existing project conventions for package names, test locations, naming, and formatting.

### 2. Analyze the Java code

- Read the target class and its directly relevant collaborators.
- Check that the current implementation, project configuration, dependencies, and stated requirements are up to date before planning tests. Do not rely on stale test names, APIs, DTO fields, or earlier assumptions.
- Identify public behavior, branches, validation, exceptions, side effects, persistence calls, and external boundaries.
- Check existing tests before adding new ones.
- Prefer testing observable behavior over private implementation details.
- Keep the review bound to the user-selected classes only. Do not expand scope to additional production classes unless a direct dependency is required to exercise the selected behavior.
- Exclude DTOs, records, value objects, generated code, and trivial data containers unless they contain meaningful behavior that is explicitly in scope.

### 3. Plan test cases

Create a compact test matrix covering only the requested behavior for the selected classes:

- successful or valid input
- invalid input and rejected access
- null, empty, missing, or boundary values where relevant
- collaborator failure or not-found behavior
- important branch and error responses

Do not generate tests for classes outside the explicitly selected scope, even if they appear useful for coverage.

Choose the narrowest appropriate test type:

- plain JUnit 5 plus Mockito for isolated classes
- `@WebMvcTest` and `MockMvc` for Spring MVC routing, binding, validation, headers, and HTTP responses
- integration tests only when the behavior depends on real Spring wiring, a database, or multiple infrastructure components

Do not change existing tests to make the plan fit. Create new test classes or new test files only where coverage is needed for the selected production classes.

### 4. Generate JUnit 5 tests

- Put tests under the matching package in `src/test/java`.
- Make directories and files if they do not exist.
- Create new tests only; preserve all existing test files unchanged.
- Generate tests only for the user-selected classes and their directly required collaborators; do not add broader coverage work.
- Use descriptive method names that state the behavior and outcome.
- Use `@InjectMocks` for the class under test when Mockito constructor injection is appropriate, together with `@Mock` collaborators. Do not mock the class under test. If `@InjectMocks` cannot represent the required construction, use an explicit constructor and state why.
- Mock collaborators at the boundary; do not mock the class under test.
- Avoid stubbing calls that the test does not exercise.
- Match functional arguments with Mockito matchers when lambdas or independently created objects are passed, for example `any(RowMapper.class)` with `eq(...)` for scalar arguments.
- Test the actual public API of the class.
- Keep each test focused on one behavior.
- Do not ask for a separate approval step before writing these tests; the user selection itself is the authorization boundary.

### 5. Run Maven tests

From the Maven project root, run the narrowest useful command first. The command is based on the user-selected class or package scope, not on the entire repository unless the user explicitly selected that broader scope.

```bash
mvn -Dtest=TargetTest test
```

Run this focused test immediately after creating new tests, before running the full suite or making unrelated test changes. If it fails, fix only the newly created tests or the requested production behavior, then rerun the same focused command.

Then run the relevant package or full suite when the focused tests pass:

```bash
mvn test
```

Never claim tests pass without observing the command result.

### 6. Return failures to the model and fix them

For every failure:

1. Read the complete Maven/JUnit/Mockito error.
2. Identify whether the cause is production code, test setup, an incorrect expectation, or an environment/dependency issue.
3. Make the smallest root-cause fix.
4. Rerun the same focused test command.
5. Repeat until the focused test passes, or report the concrete blocker.

Pay special attention to:

- `PotentialStubbingProblem`: Mockito arguments do not match the actual invocation.
- `UnnecessaryStubbingException`: remove unused stubbing instead of weakening strictness.
- `NullPointerException` in constructors: configure required mocked collaborators or pass values that avoid optional initialization.
- compilation errors from mismatched records, request DTOs, imports, or extra braces.
- tests that accidentally stub the class under test rather than exercising it.

### 7. Evaluate JaCoCo coverage

JaCoCo is configured in this project. Run the full verification lifecycle to execute tests and create the report:

```bash
mvn verify
```

Inspect the generated report under `target/site/jacoco/` and evaluate coverage for the selected production classes, especially missed branches and error paths. Identify classes with meaningfully low coverage and add targeted tests only for those classes when they are required by the request. Do not chase a percentage by testing trivial getters, DTOs, or implementation details.

No VS Code extension is required. Maven downloads and runs JaCoCo, and the HTML report can be opened directly from `api/target/site/jacoco/index.html`.

## Completion checklist

- [ ] Maven project and Java version identified
- [ ] Current implementation, dependencies, and requirements checked for freshness
- [ ] Target code and existing tests analyzed
- [ ] Only user-selected production classes included; DTOs and trivial data containers excluded
- [ ] No additional classes or packages generated outside the selection scope
- [ ] Relevant test cases planned
- [ ] New JUnit 5 tests created in the matching package
- [ ] Existing tests left unchanged
- [ ] Focused Maven tests executed
- [ ] Failures fixed and the focused command rerun
- [ ] Broader tests executed when appropriate
- [ ] Low JaCoCo coverage classes evaluated and targeted only when relevant
- [ ] Tested and untested production classes listed separately
- [ ] Final response names changed files and actual validation results
