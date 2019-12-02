package edu.baylor.flarn.services;


import edu.baylor.flarn.exceptions.RecordNotFoundException;
import edu.baylor.flarn.models.*;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ProblemServiceTest} contains the unit tests for the {@link ProblemService}.
 * Also integrations test for problem, category, knowledge-source, moderator, review, sessions, and questions.
 *
 * @author Dipta Das
 * @author Clinton Yeboah
 * @author Frimpong Boadu
 */

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class ProblemServiceTest {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Test
    /**
     * This test method is contains
     * Integration test for: problem,category,knowledge source,moderator,review,sessions,questions
     */
    void createProblem() throws RecordNotFoundException {

        //get Users; learner and moderator
        User moderator = userService.getUserByUsernameActive("moderator1@gm.com");
        assertThat(moderator.getUserType(), Is.is(UserType.MODERATOR));
        User reviewer = userService.getUserByUsernameActive("admin@gm.com");

        //Knowledge Source
        KnowledgeSource knowledgeSource = new KnowledgeSource();
        knowledgeSource.setContent("Test Knowledge source");

        //Category
        Category category = categoryService.getDefaultCategory();

        //Question
        Question question = new Question();
        question.setContent("Which of these is not a programming language");
        question.setOptions(new ArrayList<String>() {
            {
                add("Java");
                add("C++");
                add("realm");
            }
        });
        question.setAnswer(2);

        Review star = new Review();
        star.setUser(reviewer);
        star.setReviewType(ReviewType.STAR);

        Review comment = new Review();
        comment.setUser(reviewer);
        comment.setReviewType(ReviewType.COMMENT);
        comment.setCommentContent("This problem is ....");

        Problem problem = new Problem();
        problem.setDifficulty(Difficulty.EASY);
        problem.setTitle("Test problem");
        problem.setDescription("This is a problem for test");
        problem.setArchived(false);

        problem.setKnowledgeSource(knowledgeSource);
        problem.setCategory(category);
        problem.getQuestions().add(question);
        problem.setReviews(new HashSet<Review>() {
            {
                add(star);
                add(comment);
            }
        });

        Problem saved = problemService.createProblem(problem, moderator);

        assertEquals(saved, problem);
        assertNotNull(problemService.getProblemById(saved.getId()));

        assertEquals(knowledgeSource.getProblem(), saved);
        assertEquals(question.getProblem(), saved);
    }

    @Test
    void getAllProblems() {
        assertNotNull(problemService.getAllProblems());
    }

    @Test
    void getAllUnarchivedProblems() {
        List<Problem> problems = problemService.getAllUnarchivedProblems();
        problems.forEach(problem -> Assert.assertThat(problem.isArchived(), Is.is(false)));
    }


    @Test
    void archiveProblem() throws RecordNotFoundException {
        Problem problem = problemService.getProblemById(1L);

        //deactivate user
        problemService.archiveProblem(problem.getId());
        assertThatThrownBy(() -> problemService.getProblemById(problem.getId())).isInstanceOf(RecordNotFoundException.class).hasMessageContaining("Problem not found with id: " + problem.getId());

    }

    @Test
    void updateProblem() {
    }

    @Test
    void getRandomProblem() throws RecordNotFoundException {
        //get User
        User user = userService.getUserByUsernameActive("moderator1@gm.com");

        Problem problem = problemService.getRandomProblem(user);
        assertNotNull(problem);
        assertFalse(problem.isArchived());
    }
}