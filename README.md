<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [Configuration](#configuration)
  - [Database connectivity](#database-connectivity)
- [Code](#code)
  - [Controller layer](#controller-layer)
    - [GET method](#get-method)
    - [POST method](#post-method)
  - [Service layer](#service-layer)
    - [GET method](#get-method-1)
    - [POST method](#post-method-1)
  - [Model layer](#model-layer)
  - [DAO layer](#dao-layer)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

#[Telusko Microservice Tutorial using Java (question-service, quiz-service, quizapp)](https://youtube.com/playlist?list=PLsyeobzWxl7rRyGcqgZ3MP5pWGPwUvprI&si=e8pFZoD5_qx7kimC)

## Configuration

After I went to https://start.spring.io my build.gradle looks like this. Maven's pom.xml would look similar.

```
dependencies {
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
implementation 'org.springframework.boot:spring-boot-starter-web'
compileOnly 'org.projectlombok:lombok'
runtimeOnly 'com.mysql:mysql-connector-j'
annotationProcessor 'org.projectlombok:lombok'
testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### Database connectivity

If you (or Responsiv) are using Maven then your application.properties would have the
following database configuration:

```
spring.datasource.driver-class-name=org.postgreql.Driver
spring.datasource.url=jdbc:postgresql://localhost:3306/questiondb
spring.datasource.username=******
spring.datasource.username=******
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
```

Else if you are using gradle's quizapp/build/resources/main/application.properties:

```
# Database connection settings
spring.datasource.url=jdbc:mysql://localhost:3306/questiondb
spring.datasource.username=******
spring.datasource.password=******

# Specify the DBMS
spring.jpa.database = MYSQL

# Show or not log for each sql query
spring.jpa.show-sql = true

# Hibernate ddl auto (create, create-drop, update)
spring.jpa.hibernate.ddl-auto = update

# Use spring.jpa.properties.* for Hibernate native properties (the prefix is
# stripped before adding them to the entity manager)
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.MySQLDialect

```

## Code

### Controller layer

#### GET method

The controller layer accepts responses from your user. 

In src/main/java/com/telusko/quizapp/controller/QuestionController

The following import includes the RestController (accepts requests) and the RequestMapping (path URI).

```
import org.springframework.web.bind.annotation.*;
```

Users will access localhost:8080/question/allQuestions
```
@RestController
@RequestMapping("question")
public class QuestionController {

    @Autowired
    QuestionService questionService;
    @GetMapping("allQuestions")
    public ResponseEntity<List<Question>> getAllQuestions(){
//        return "Hi, these are your questions:";
        return questionService.getAllQuestions();

    }
```

Then the Controller layer request goes to the service layer which does some processing or business logic.
```
import com.telusko.quizapp.service.QuestionService;
...
@Autowired
    QuestionService questionService;
    .....
return questionService.getAllQuestions();
```
In the controller the {category} comes from the URL as a parameter.
You need the @PathVariable annotation to pass it as an argument to the
getQuestionByCategory method.

```java
    @GetMapping("category/{category}")
    public ResponseEntity<List<Question>> getQuestionByCategory(@PathVariable String category) {
        return questionService.getQuestionsByCategory(category);

    }
```

You could use {cat} instead but you need to specify the string as an
argument to PathVariable.

```java
    @GetMapping("category/{cat}")
    public ResponseEntity<List<Question>> getQuestionByCategory(@PathVariable("cat") String category) {
        return questionService.getQuestionsByCategory(category);

    }
```

#### POST method

pring says you just specify the
// JSON I will convert that into an object but you have to also mention a request body because
// you are sending this data in the request from the client side to the server
```java
    @PostMapping("addQuestion")
    public ResponseEntity<String> addQuestion(@RequestBody Question question){//Spring says you just specify the
        // JSON I will convert that into an object but you have to also mention a request body because
        // you are sending this data in the request from the client side to the server
        return questionService.addQuestion(question);

    }
```

### Service layer

Path: src/main/java/com/telusko/quizapp/service/QuestionService

#### GET method
The @Service annotation does the same as @Component.
We also Autowire the Questiondao so that I can call it's methods (findAll).
```
@Service
public class QuestionService {

    @Autowired
    Questionddao questiondao;
    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questiondao.findAll(), HttpStatus.OK);
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }
```

If your service layer wants to retrieve from the database you need DAO layer which
connects to the database to fetch data.

But before that you need to model the data in the DB table. To represent the table we create a class called Entities.
We also called them Models (MVC).

#### POST method

The save method is given to you by JPA.

```java
public ResponseEntity<String> addQuestion(Question question) {
questiondao.save(question);
return new ResponseEntity<>( "success",HttpStatus.CREATED);
}
```


### Model layer

Path: src/main/java/com/telusko/quizapp/model/Question

The class name matches the table name, the fields match your column names.
The number of Question objects matches the rows in the DB. That is your ORM (object relational mapping).


```
@Data //coming from Lombok
@Entity //We want this table to be mapped to this class
@Getter
public class Question {
    @Id //primary key
    @GeneratedValue(strategy = GenerationType.AUTO) //Id is auto generated
    private int id;

    private String category;
    private String difficultyLevel;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    @Getter
    @Setter
    private String questionTitle;
    private String rightAnswer;

}
```

Here the field questionTitle matches to question_title in the table. Automatically JPA would take in the camelCase field name
then create question_title (This is called snake_casing)

### DAO layer

Path: src/main/java/com/telusko/quizapp/dao/QuestionDao

Remember in the Service we had @Service. Well in DAO we have @Repository.
Before in JDBC we used to have something like this:

```agsl

✅ 1. DAO Example (Classic Java + JDBC)
This is the “old school” DAO pattern: explicit SQL, JDBC, low-level control.
UserDao.java
public class UserDao {

    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, first_name, last_name FROM users WHERE id = ?";
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name")
            );
        }
        return null;
    }
```

Because you got JPA from spring.io you can create an interface:

```aidl
@Repository
public interface Questiondao extends JpaRepository<Question,Integer> {
    List<Question> findByCategory(String category); //JPA is smart enough to work out you want
    // everything with that category

    @Query(value = "SELECT * FROM question q WHERE q.category=:category ORDER BY RAND() LIMIT :numQ",nativeQuery = true)
    List<Question> findRandomByCategory(String category, int numQ);
}
```

All the CRUD operations will be handled by JPA. It takes the name of the class that 
maps to the table name and the Primary key type.
Remember in your service you had?

```androiddatabinding
try {
            return new ResponseEntity<>(questiondao.findAll(), HttpStatus.OK);
        }catch 
```
You don't need to create the findAll in your DAO class since 
JPA gives that to you without you having to implement it since it
does not have any parameters.

In this example you do not need to write any HQL (Hibernate Query Language)
or JPQL (JPA Query Language) since JPA is smart enough to know that category argument
matches a column in the table.

```java
List<Question> findByCategory(String category); //JPA is smart enough to work out you want
    // everything with that category
```
