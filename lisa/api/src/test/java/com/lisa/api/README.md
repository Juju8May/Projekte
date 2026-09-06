# API tests

Add Java tests below this directory. Maven discovers tests automatically from `src/test/java`.

Suggested packages:

- `com.lisa.api.chat` for controller and repository tests
- `com.lisa.api.security` for API-key, CORS, headers, and rate-limit tests
- `com.lisa.api.web` for validation and error-handler tests

Useful test commands from `api/`:

```bash
mvn test
mvn -Dtest=ClassName test
```

Use `src/test/resources/application-test.yml` for test-only configuration and never place real API keys or database passwords in test files.
