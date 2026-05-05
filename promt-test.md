

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


