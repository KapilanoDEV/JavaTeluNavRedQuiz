package com.telusko.quizapp.service;

import com.telusko.quizapp.dao.Questiondao;
import com.telusko.quizapp.dao.Quizdao;
import com.telusko.quizapp.model.Question;
import com.telusko.quizapp.model.QuestionWrapper;
import com.telusko.quizapp.model.Quiz;
import com.telusko.quizapp.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class QuizService {

    @Autowired
    Quizdao quizdao;

    @Autowired
    Questiondao questiondao;

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {
        Quiz quiz = new Quiz();

        List<Question> questionList = questiondao.findRandomByCategory(category,numQ);

        quiz.setQuizTitle(title);
        quiz.setCategory(category);
        quiz.setQuestionList(questionList);
        quizdao.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuiz(int id) {
        return quizdao.findById(id)
                .map(quiz -> { // Java infers that 'quiz' is a 'Quiz' object!
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

                })
                // If the quiz doesn't exist in the database, safely return a 404 Not Found
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Integer> calculateResult(int id, List<Response> responses) {


        return quizdao.findById(id)
                .map(quiz -> {
                    // 1. Transform the database questions into a high-speed lookup Map: Id -> RightAnswer
                    Map<Integer,String> responseMap = quiz.getQuestionList().stream()
                            .collect(Collectors.toMap(Question::getId, Question::getRightAnswer));
                    // 2. Stream through incoming user responses, filter out correct ones, and count them
                    long correctCount = responses.stream()
                            .filter(resp -> {
                                String correctAnswer = responseMap.get(resp.getId());
                                return correctAnswer != null && correctAnswer.equals(resp.getResponse());
                            })
                            .count();
                    // 3. Return the total score cast safely to an Integer
                    return ResponseEntity.ok((int) correctCount); // 🛠️ Returns a ResponseEntity<Integer> with 200 OK
                })
                // If the quiz doesn't exist, return 404 Not Found
                .orElse(ResponseEntity.notFound().build());
    }
}
