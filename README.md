<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**  *generated with [DocToc](https://github.com/thlorenz/doctoc)*

- [This is the monolithic application](#this-is-the-monolithic-application)
- [Question App](#question-app)
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
    - [Direct Database Execution](#direct-database-execution)
      - [Key Differences: JPQL vs. Native Query](#key-differences-jpql-vs-native-query)
    - [Code Breakdown](#code-breakdown)
- [Quiz App](#quiz-app)
    - [Controller layer](#controller-layer-1)
    - [Service Layer](#service-layer)
      - [Table 1: The quiz Table](#table-1-the-quiz-table)
      - [Table 2: Hibernate automatically creates a **join table**.](#table-2-hibernate-automatically-creates-a-join-table)
  - [`Owner Entity Name (Quiz)+_+Field Variable Name (questionList)=quiz_question_list`](#owner%C2%A0entity%C2%A0name%C2%A0quiz_field%C2%A0variable%C2%A0name%C2%A0questionlistquiz_question_list)
- [🧩 **How `getQuestionList()` Works Now**](#-how-getquestionlist-works-now)
- [📌 Summary](#-summary)
    - [DAO Layer](#dao-layer)
    - [Model layer](#model-layer-1)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

#[Telusko Microservice Tutorial using Java (question-service, quiz-service, quizapp)](https://youtube.com/playlist?list=PLsyeobzWxl7rRyGcqgZ3MP5pWGPwUvprI&si=e8pFZoD5_qx7kimC)

# This is the monolithic application

# Question App
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

Users will access localhost:8080/question/allQuestions.
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
getQuestionsByCategory method.

```
    @GetMapping("category/{category}")
    public ResponseEntity<List<Question>> getQuestionByCategory(@PathVariable String category) {
        return questionService.getQuestionsByCategory(category);

    }
```

You could use {cat} instead but you need to specify the string as an
argument to PathVariable.

```
    @GetMapping("category/{cat}")
    public ResponseEntity<List<Question>> getQuestionByCategory(@PathVariable("cat") String category) {
        return questionService.getQuestionsByCategory(category);

    }
```

#### POST method

Spring says you just specify the JSON I will convert that into an object but you have to also mention a request 
body because you are sending this data in the request from the client side 
to the server
```
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
The List<Question> is a list of questions in a JSON format. But instead we can return a ResponseEntity
object that has 2 parameters; the data returned and the status code. We put that in the
try block but if there is an exception then it will print the error on the console. What is something goes wrong in the
try? Then return an empty ArrayList and an HTTP.BAD_REQUEST.
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

```
public ResponseEntity<String> addQuestion(Question question) {
questiondao.save(question);
return new ResponseEntity<>( "success",HttpStatus.CREATED);
}
```

You can test this using Postman. You do not need to include "id": 2 as this
is autogenerated. The HttpStatus.CREATED returns the code 201 in Postman.

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

```

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

```
@Repository
public interface Questiondao extends JpaRepository<Question,Integer> {
    List<Question> findByCategory(String category); //JPA is smart enough to work out you want
    // everything with that category

    @Query(value = "SELECT * FROM question q WHERE q.category=:category ORDER BY RAND() LIMIT :numQ",nativeQuery = true)
    List<Question> findRandomByCategory(String category, int numQ);
}
```
In Spring Data JPA, `nativeQuery = true` tells the framework to execute the exact SQL string provided directly in the database, bypassing the Java Persistence Query Language (JPQL) parser.

Here is a breakdown of what a native query means and does in this context:

### Direct Database Execution

**Standard SQL:** The query uses raw SQL syntax specific to your database (e.g., MySQL, PostgreSQL) rather than Java entity names.

**No JPQL Mapping:** It targets actual database table names (`question`) and column names, not Java class entities or field names.

**Database Functions:** It allows the use of database-specific functions like `RAND()` (MySQL) which do not exist in standard JPQL.

#### Key Differences: JPQL vs. Native Query

Here is the feature comparison organized into a clean, readable table:

| Feature | JPQL (`nativeQuery = false`) | Native Query (`nativeQuery = true`) |
| --- | --- | --- |
| **Target** | Java Entities and fields | Database Tables and columns |
| **Portability** | Database independent | Database specific (harder to switch DBs) |
| **Functions** | Restricted to JPA standard | Any function supported by your DB |
| **Validation** | Checked at application startup | Checked only when executed by the DB |

### Code Breakdown

**SELECT * FROM question:** Selects all columns from the actual database table named `question`.

**q.category = :category:** Filters by the column `category` using a named parameter.

**ORDER BY RAND():** Uses the database's random function to shuffle rows.

**LIMIT :numQ:** Restricts the result count using a named parameter (supported natively in modern Spring Data JPA).

All the CRUD operations will be handled by JPA. It takes the name of the class that 
maps to the table name and the Primary key type.
Remember in your service you had?

```java
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

**Question model Entity Field:** category -> **SQL Column:** category

When you call `findByCategory()`, Hibernate automatically translates it into this SQL statement under the hood:

```sql
SELECT * FROM product WHERE category = ?;
```

# Quiz App

### Controller layer

Accesible through quiz/create?category=Java&numQ=5&title=JQuiz
```
@Autowired
    QuizService quizService;
    @PostMapping("create")
    public ResponseEntity<String> createQuiz(@RequestParam String category, @RequestParam int numQ, @RequestParam String title) {
//        return new ResponseEntity<String>("I am here", HttpStatus.OK);
        return quizService.createQuiz(category,numQ,title);
    }
```

The getQuiz method returns a List of type QuestionWrapper which is a subset of
the Question model.
```
@GetMapping("getQuiz/{id}")
public ResponseEntity<List<QuestionWrapper>> getQuiz(@PathVariable int id) {
return quizService.getQuiz(id);
}
```

### Service Layer

I fetch a quiz from the database.
```
List<Question> questionList = questiondao.findRandomByCategory(category,numQ);
```

The "result" may contain a  Quiz object (if the quiz exists), or be 
empty (if no quiz found).
The get() pulls out the actual Quiz from the Optional—but only works 
if the Optional is not empty. The .getQuestionList() calls the getter on the Quiz.

:The Optional word below allows for when that particular id does not exist in the
quiz table. The result object has a list of questions. We need to convert the Question
type to QuestionWrapper type.

```
public ResponseEntity<List<QuestionWrapper>> getQuiz(int id) {
        Optional<Quiz> result = quizdao.findById(id);

        List<Question> questionList = result.get().getQuestionList();
        List<QuestionWrapper> questionWrapperList = new ArrayList<>();

        for (Question qn : questionList) {
            QuestionWrapper qw = new QuestionWrapper(qn.getId(),qn.getQuestionTitle(),qn.getOption1(),qn.getOption2(),qn.getOption3(),qn.getOption4());
            questionWrapperList.add(qw);
        }
        return new ResponseEntity<>(questionWrapperList, HttpStatus.OK);
    }

```
To calculate the score this method takes a quiz ID and a list of user responses (in the body as JSON).
It retrieves the correct answers from the quiz's questions.
quizdao.findById(id) uses JPA to fetch the quiz record.
The result is wrapped inside an Optional<Quiz> to handle “quiz not found” safely.

```
public ResponseEntity<Integer> calculateResult(int id, List<Response> responses) {
        Optional<Quiz> quiz = quizdao.findById(id); // id=1, category=Java, quiz_title=JQuiz

        List<Question> questionList = quiz.get().getQuestionList();
        int i = 0;
        String answer;
        int points=0;
        for (Response response : responses) {
            answer = questionList.get(i).getRightAnswer();
            i++;

            if (response.getResponse().equals(answer))
                points++;
        }
        return new ResponseEntity<>(points, HttpStatus.OK);
    }
```
The table **quiz_question_ids** was created because of this line:

```java
@ManyToMany
private List<Question> questionList;
```

Hibernate must store a **many-to-many relationship** between:

* A Quiz
* Multiple Questions

and also:

* A Question
* Can belong to multiple Quizzes

**Many-to-many relationships cannot be stored inside one table**, 

Hibernate creates and configures your database tables based on your model:

#### Table 1: The quiz Table

This is your core entity table. Hibernate reads your fields and builds it like this:

```sql
CREATE TABLE quiz (
id INT NOT NULL,
quiz_title VARCHAR(255),
category VARCHAR(255),
PRIMARY KEY (id)
);
```

Note that Hibernate automatically converts your camelCase Java field quizTitle to snake_case quiz_title using its default physical naming strategy.

#### Table 2: Hibernate automatically creates a **join table**.

When you declare `@ManyToMany` without providing an explicit `@JoinTable` name customization, Hibernate looks at your configuration and follows a strict naming pattern to create a third table:

`Owner Entity Name (Quiz)+_+Field Variable Name (questionList)=quiz_question_list`
---

The schema for this autogenerated join table looks like this under the hood:

```sql
CREATE TABLE quiz_question_list (
    quiz_id INT NOT NULL,
    question_list_id INT NOT NULL,
    CONSTRAINT fk_quiz FOREIGN KEY (quiz_id) REFERENCES quiz(id),
    CONSTRAINT fk_question FOREIGN KEY (question_list_id) REFERENCES question(id)
);
```

**How the columns inside the join table are named:**

1. `quiz_id:` This matches the primary key of the class owning the relationship (`Quiz`).
1. `question_list_id:` This is formed by taking your field variable name (`questionList`) and appending `_id`.

# 🧩 **How `getQuestionList()` Works Now**

When you call:

```java
quiz.get().getQuestionList();
```

Hibernate runs SQL like:

```sql
SELECT question_id
FROM quiz_question_ids
WHERE quiz_id = ?
```

Then it loads each `Question` entity from the **question** table.

You never wrote this SQL — Hibernate does it automatically.

---

---

# 📌 Summary

| What you used                 | Result                                                                      |
| ----------------------------- | --------------------------------------------------------------------------- |
| `@ManyToMany`                 | Hibernate must create a join table                                          |
| Type of table                 | `quiz_question_ids` (auto-named)                                            |
| Why?                          | Many-to-many cannot be expressed in a single table                          |
| How `getQuestionList()` works | Hibernate looks up question IDs in the join table, then loads each question |

---


### DAO Layer

We use @Query annotation and a nativeQuery. This is JPQL. It uses : for the
category variable.
This creates a quiz_question_list table that has quiz id and question_list_id per row.
```
@Query(value = "SELECT * FROM question q WHERE q.category=:category ORDER BY RAND() LIMIT :numQ",nativeQuery = true)
List<Question> findRandomByCategory(String category, int numQ);
```


### Model layer

@ManyToMany means we have a quiz with many questions.

```
@Data
@Entity
@Getter
public class Quiz {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private int id;

    private String quizTitle;
    private String category;

    @ManyToMany
    private List<Question> questionList;
}
```

The reason for the QuestionWrapper is to have a model for a class that is the same
as the Question model but without the right answer. This is used for when we get the quiz. This is separating Database Core Entities from your Data Transfer Objects (DTOs).

Telusko used the `@Entity` annotationon QuestionWrapper.

Why should you remove `@Entity`?

The `@Entity` annotation tells Hibernate that this class directly maps to a physical database table layout. If you leave `@Entity` active on `QuestionWrapper`, Hibernate will try to create a table named `question_wrapper` in your database on application startup.
Since your wrapper is strictly a temporary data transport container meant for HTTP traffic, it should not be tracked by your persistence engine. Removing `@Entity` (and optionally removing `@Id`) turns it into a pure DTO (Data Transfer Object).

### Production-Ready Safety Upgrade for getQuiz

```java
public ResponseEntity<List<QuestionWrapper>> getQuiz(int id) {
    return quizdao.findById(id)// Returns an Optional<Quiz>
            .map(quiz -> {
                List<QuestionWrapper> wrappers = quiz.getQuestionList().stream()
                        .map(qn -> new QuestionWrapper(
                                qn.getId(),
                                qn.getQuestionTitle(),
                                qn.getOption1(),
                                qn.getOption2(),
                                qn.getOption3(),
                                qn.getOption4()
                        ))
                        .toList();
                return new ResponseEntity<>(wrappers, HttpStatus.OK);
            })// Transforms Optional<Quiz> into Optional<ResponseEntity>
            // If the quiz doesn't exist in the database, safely return a 404 Not Found
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Fallback if empty
}
```


In real-world applications, calling `.get()` directly on an `Optional` without verifying its presence with `.isPresent()` can cause a `NoSuchElementException` crash if someone requests an invalid quiz ID (e.g., `/getQuiz/9999`).
You can make your method safer and more expressive by handling that missing case gracefully using `.map()` and `.orElse()` (or `.orElseThrow()`):


The reason `.orElse()` works here without a stream is because it belongs to the `Optional` class, not the Stream API.
While Optional and Stream are both part of Java 8+ functional programming and look similar because they use methods like `.map()`, they serve entirely different purposes.

#### The Conceptual Difference

A Stream is a pipe designed to process a collection of many elements sequentially.
An Optional is a wrapper box designed to contain either exactly one object or nothing at all (null). It is a tool to prevent NullPointerException crashes.

#### How `.orElse()` Operates on an Optional
Think of an Optional as a gift box. You call `.orElse()` to define your backup plan in case the box turns out to be empty.
When you chain `.map()` and `.orElse()` on an Optional like this:

```java
return quizdao.findById(id) // Returns an Optional<Quiz>
        .map(quiz -> {
            // ... transformation logic ...
            return new ResponseEntity<>(wrappers, HttpStatus.OK);
        }) // Transforms Optional<Quiz> into Optional<ResponseEntity>
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // Fallback if empty
```

The execution follows this specific logic branch:
1. quizdao.findById(id) runs.
- If the quiz exists, the box contains a Quiz object.
- If the quiz does not exist, it returns an empty box (Optional.empty()).
2. `.map(...)` checks the box:
- If a Quiz is present: It opens the box, runs your transformation code inside the curly braces to convert the quiz into a ResponseEntity, and puts that new response entity back inside an Optional box.
- If the box is empty: It skips your transformation logic entirely and just passes along the empty Optional box.
3. `.orElse(...)` looks at the final box:
- If the box is full: It unwraps the object inside (your successful 200 OK response) and returns it.
- If the box is empty: It ignores the box entirely and returns your fallback value (the 404 NOT FOUND response) instead.