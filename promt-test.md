

Backend:
// Run only repository tests
mvn test -Dtest=*RepositoryDataJpaTest

// Run test k6
docker compose run --rm k6

// Run all tests
mvn clean install

// Run only tests
mvn test

// Run only file
mvn  -Dtest=ClassTest test

Frontend:
// Run all tests
npm run test:ui

// Run only file
npm run test:ui -- path/to/file.test.tsx


// Generate coverage report
npx vitest run --coverage


// Run Playwright tests
npx playwright test:ui


// Run Playwright tests in parallel
npx playwright test --workers=auto



