# JPA Advance

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.0/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/3.0.0/maven-plugin/reference/html/#build-image)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#data.sql.jpa-and-spring-data)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.0.0/reference/htmlsingle/#web)

### Guides

The following guides illustrate how to use some features concretely:

* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Query Methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods)
* [Stored Procedures](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.stored-procedures)
* [Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
* [Auditing](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing)
* 
    
### Getting Started
#### 1. Stored Procedures
1. Entity Class
```java
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;

    @Column
    private String model;

    @Column
    private Integer year;

   // standard getters and setters
}
```
2. Stored Procedure Creation
  A stored procedure can have parameters so that we can get different results based on the input. For example, we can create a stored procedure that takes an input parameter of integer type and returns a list of cars:
```sql
CREATE PROCEDURE FIND_CARS_AFTER_YEAR(IN year_in INT)
BEGIN 
    SELECT * FROM car WHERE year >= year_in ORDER BY year;
END
```
A stored procedure can also use output parameters to return data to the calling applications. For example, we can create a stored procedure that takes an input parameter of string type and stores the query result into an output parameter:
```sql
CREATE PROCEDURE GET_TOTAL_CARS_BY_MODEL(IN model_in VARCHAR(50), OUT count_out INT)
BEGIN
    SELECT COUNT(*) into count_out from car WHERE model = model_in;
END
```
- Reference Stored Procedures in Repository
  In Spring Data JPA, repositories are where we provide database operations. We can construct a repository for the database operations on the Car entity, and reference stored procedures in this repository:
```java
@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {
    // ...
}
```
Next, let's add some methods to our repository that call stored procedures:
- - Map a Stored Procedure Name Directly
    We can define a stored procedure method using the @Procedure annotation, and map the stored procedure name directly.

There are four equivalent ways to do that. For example, we can use the stored procedure name directly as the method name:
```java
@Procedure
int GET_TOTAL_CARS_BY_MODEL(String model);
```
If we want to define a different method name, we can put the stored procedure name as the element of the @Procedure annotation:
```java
@Procedure("GET_TOTAL_CARS_BY_MODEL")
int getTotalCarsByModel(String model);
```
- - We can also use the procedureName attribute to map the stored procedure name:
```java
@Procedure(procedureName = "GET_TOTAL_CARS_BY_MODEL")
int getTotalCarsByModelProcedureName(String model);
```
Finally, we can use the value attribute to map the stored procedure name:
```sql
@Procedure(value = "GET_TOTAL_CARS_BY_MODEL")
int getTotalCarsByModelValue(String model);
```
- - Reference a Stored Procedure Defined in Entity
    We can also use the @NamedStoredProcedureQuery annotation to define a stored procedure in the entity class:
```java
@Entity
@NamedStoredProcedureQuery(name = "Car.getTotalCardsbyModelEntity", 
  procedureName = "GET_TOTAL_CARS_BY_MODEL", parameters = {
    @StoredProcedureParameter(mode = ParameterMode.IN, name = "model_in", type = String.class),
    @StoredProcedureParameter(mode = ParameterMode.OUT, name = "count_out", type = Integer.class)})
public class Car {
    // class definition
}
```
Then we can reference this definition in the repository:
```java
@Procedure(name = "Car.getTotalCardsbyModelEntity")
int getTotalCarsByModelEntiy(@Param("model_in") String model);
```
We use the name attribute to reference the stored procedure defined in the entity class. For the repository method, we use @Param to match the input parameter of the stored procedure. We also match the output parameter of the stored procedure to the return value of the repository method.
- - 5.3. Reference a Stored Procedure With the @Query Annotation
    We can also call a stored procedure directly with the @Query annotation:
```java
@Query(value = "CALL FIND_CARS_AFTER_YEAR(:year_in);", nativeQuery = true)
List<Car> findCarsAfterYear(@Param("year_in") Integer year_in);
```
In this method, we use a native query to call the stored procedure. We store the query in the value attribute of the annotation.

Similarly, we use `@Param` to match the input parameter of the stored procedure. We also map the stored procedure output to the list of entity Car objects.
#### 4. JPA Specification
- Search Criteria
```java
@Data
@AllArgsConstructor
public class SearchCriteria {
    private String key;
    private Object value;
    private SearchOperation operation;
}
```
- Search Operation
```java
public enum SearchOperation {
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    NOT_EQUAL,
    EQUAL,
    LIKE,
    LIKE_START,
    LIKE_END,
    IN,
    NOT_IN
}
```
- Entity Definition
```java
@Data
@Entity
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Apple {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long appleId;
    private String appleName;
    private String taste;
    private float price;
}
```
- Repository
```java
@Repository
public interface AppleRepository extends CrudRepository<Apple, Long>, JpaSpecificationExecutor {
}
```
- Specification
```java
@Data
public class AppleSpecification implements Specification<Apple> {
    private List<SearchCriteria> list = new ArrayList<>();

    public void add(SearchCriteria criteria) {
        list.add(criteria);
    }

    @Override
    public Predicate toPredicate(Root<Apple> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        for (SearchCriteria criteria : list) {
            switch (criteria.getOperation()) {
                case GREATER_THAN:
                    predicates.add(builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case LESS_THAN:
                    predicates.add(builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case GREATER_THAN_EQUAL:
                    predicates.add(builder.greaterThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case LESS_THAN_EQUAL:
                    predicates.add(builder.lessThanOrEqualTo(root.get(criteria.getKey()), criteria.getValue().toString()));
                    break;
                case NOT_EQUAL:
                    predicates.add(builder.notEqual(root.get(criteria.getKey()), criteria.getValue()));
                    break;
                case EQUAL:
                    predicates.add(builder.equal(root.get(criteria.getKey()), criteria.getValue()));
                    break;
                case LIKE:
                    predicates.add(builder.like(builder.lower(root.get(criteria.getKey())), "%" + criteria.getValue().toString().toLowerCase() + "%"));
                    break;
                case LIKE_END:
                    predicates.add(builder.like(builder.lower(root.get(criteria.getKey())), criteria.getValue().toString().toLowerCase() + "%"));
                    break;
                case LIKE_START:
                    predicates.add(builder.like(builder.lower(root.get(criteria.getKey())), "%" + criteria.getValue().toString().toLowerCase()));
                    break;
                case IN:
                    predicates.add(builder.in(root.get(criteria.getKey())).value(criteria.getValue()));
                    break;
                case NOT_IN:
                    predicates.add(builder.not(root.get(criteria.getKey())).in(criteria.getValue()));
                    break;
            }
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
```
Or custom
```java
public class CustomerSpecs {


    public static Specification<Customer> isLongTermCustomer() {
        return (root, query, builder) -> {
            LocalDate date = LocalDate.now().minusYears(2);
            return builder.lessThan(root.get(Customer_.createdAt), date);
        };
    }

    public static Specification<Customer> hasSalesOfMoreThan(MonetaryAmount value) {
        return (root, query, builder) -> {
            // build query here
        };
    }
}
```
- Controller
```java
@RestController
public class AppleController {
    @Autowired
    private AppleRepository appleRepository;

    @PostMapping
    void specification(@RequestBody List<SearchCriteria> searchCriteria) {
        AppleSpecification appleSpecification = new AppleSpecification();
        searchCriteria.stream().map(searchCriterion -> new SearchCriteria(searchCriterion.getKey(), searchCriterion.getValue(), searchCriterion.getOperation())).forEach(appleSpecification::add);
        List<Apple> msGenreList = appleRepository.findAll(appleSpecification);
        msGenreList.forEach(System.out::println);
    }
}
```