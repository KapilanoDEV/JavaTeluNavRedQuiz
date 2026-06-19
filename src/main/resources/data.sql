INSERT INTO question (id, category, difficulty_level, option1, option2, option3, option4, question_title, right_answer) VALUES
                                                                                                                           (1,'Java','Easy','true','false','0','null','In Java, what is the default value of an uninitialized boolean variable?','false'),
                                                                                                                           (2,'Java','Easy','4','5','6','Compile error','What will the following Java code snippet output?\nint x = 5;\nSystem.out.println(x++);','5'),
                                                                                                                           (202,'Python','Medium','To add inline comments within a function','To mark a function as deprecated and discourage its use','To modify or extend the behavior of a function or class method','To define a new variable within a function''s scope','What is the purpose of a Python decorator?','To modify or extend the behavior of a function or class method'),
                                                                                                                           (252,'Java','Easy','100','127','255','999','Maximum value for short in java','127'),
                                                                                                                           (302,'Java','Easy','equals() method compares the contents of objects, while == operator compares memory addresses','equals() method is used to compare primitive data types, while == operator is used for objects','equals() method & == operator perform the same type of comparison and can be used interchangeably','equals() method compares memory addresses, while == operator compares the contents of objects','What is the diff b/w equals() method & == operator in Java when comparing objects?','equals() method compares the contents of objects, while == operator compares memory addresses'),
                                                                                                                           (303,'Java','Easy','const','final','constant','static','Which keyword is used to define a constant in Java?','final'),
                                                                                                                           (352,'Python','Easy','str1.str2','str1+str2','str1,str2','concat(str1,str2)','How do you concatenate two strings in Python??','str1+str2'),
                                                                                                                           (402,'Python','Medium','{key: value for key, value in iterable}','dict(iterable)','create_dict(iterable)','{key, value in iterable}','How do you create a dictionary using a comprehension in Python?','{key: value for key, value in iterable}');

ALTER SEQUENCE question_seq RESTART WITH 501;

INSERT INTO quiz (id, category, quiz_title) VALUES
                                           (1,'Java','JQuiz'),
                                           (2,'Python','PyQuiz'),
                                           (52,'Python','PyQuiz'),
                                           (102,'Python','PyQuiz'),
                                           (103,'Python','PythonQuiz');

INSERT INTO quiz_question_list (quiz_id, question_list_id) VALUES
                                                               (1,302),(1,303),(1,252),(1,2),(1,1),
                                                               (2,202),(2,352),
                                                               (52,352),(52,202),
                                                               (102,202),(102,352),
                                                               (103,402),(103,202),(103,352);

ALTER SEQUENCE quiz_seq RESTART WITH 201;