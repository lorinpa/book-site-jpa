package org.pa.test.controller;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.pa.entity.Review;

import org.springframework.validation.BindingResult;

import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa
 
 Note! We use our CaseGen utility to create a set of test data.

 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/dispatcher-context.xml")})

public class TestReviewController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final String REDIRECT_TO_REVIEW_HOME_NAME = "redirect:list.htm";
    // NOTE!!!  In order to recover from a failed add or modify submit, the review contoller does not redirect, it simply
    // forwards the response. It's a work-around because reviews carry a related book list
    private final String REVIEW_HOME_NAME = "review/list";

    // review that should be in the test cases
    private static int HUCK_FINN_5_STAR_REVIEW;

    // used for various tests
    private static int HUCK_FINN_BOOK_ID;

    // ADD
    private static int TEST_REVIEW_ID_ADD;

    // MODIFY
    private static int TEST_REVIEW_ID_MODIFY;
    private static final int TEST_REVIEW_STARS_MODIFY = 3;
    private static String TEST_REVIEW_BODY_MODIFY;

    // DELETE
    private static int TEST_REVIEW_ID_DELETE;
    private static final int TEST_REVIEW_STARS_DELETE = 0;
    private static String TEST_REVIEW_BODY_DELETE;

    @BeforeClass
    public static void setUpClass() {
        int MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, MARK_TWAIN_ID);

        TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "rcm";
        TEST_REVIEW_ID_MODIFY = CaseGen.getInstance().createTestReview(HUCK_FINN_BOOK_ID, TEST_REVIEW_STARS_MODIFY, TEST_REVIEW_BODY_MODIFY);

        TEST_REVIEW_BODY_DELETE = new Date().getTime() + "rcd";
        TEST_REVIEW_ID_DELETE = CaseGen.getInstance().createTestReview(HUCK_FINN_BOOK_ID, TEST_REVIEW_STARS_DELETE, TEST_REVIEW_BODY_DELETE);

        HUCK_FINN_5_STAR_REVIEW = CaseGen.getInstance().getTestReview(HUCK_FINN_BOOK_ID, 5, CaseGen.REVIEW_BODY_FIVE_STAR);

    }

    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_ADD);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_MODIFY);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_DELETE);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    /*
    * Tests the Author controller. Verifies the a view is dispatched with a list of Reviews.
     */
    @Test
    public void testListView() {
        boolean noErrorsFound = true;
        try {

            ResultActions requestResult
                    = this.mockMvc.perform(get("/review/list.htm"))
                    .andExpect(view().name("review/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/review/list.jsp"));
            List<Review> list = (List<Review>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records", (list.size() > 0));
            boolean RECORD_FOUND = false;
            for (Review review : list) {
                if (review.getId().intValue() == HUCK_FINN_5_STAR_REVIEW) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify we found record", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestReviewController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *   Tests the controller services the "add new review" function.
     *    Verify the view response is a 301 (redirect to review home/list).
     *    Verify the controller dispatches a model with the new review
     *    Verify the server assigned the new book an id (key) 
     */
    @Test
    public void testAddReview() {
        boolean noErrorsFound = true;
        try {
            String body = new Date().getTime() + "Add review body";
            int stars = 4;
            ResultActions andExpect = mockMvc.perform(post("/review/add")
                    .param("bookId.id", HUCK_FINN_BOOK_ID + "")
                    .param("stars", stars + "")
                    .param("body", body)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("review"))
                    .andExpect(status().isOk())
                    .andExpect(view().name(REVIEW_HOME_NAME));
            Review reviewResult = (Review) andExpect.andReturn().getModelAndView().getModelMap().get("review");
            assertTrue("verify id returned ", reviewResult.getId() != null);
            TEST_REVIEW_ID_ADD = reviewResult.getId();
            assertTrue("verify correct stars value returned", reviewResult.getStars() == stars);
            assertTrue("verify body ", reviewResult.getBody().equals(body));
            assertTrue("verify book id ", reviewResult.getBookId().getId().intValue() == HUCK_FINN_BOOK_ID);

        } catch (Exception ex) {
            Logger.getLogger(TestReviewController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the controller dispatches a model with the updated book. 
     *  Note! We modify the last book generated by DbUtil to avoid conflicting with other tests. 
     */
    @Test
    public void testModBook() {
        boolean noErrorsFound = true;
        try {
            String body = new Date().getTime() + "modifed review body";
            int stars = 2; // not 3 original value
            ResultActions andExpect = mockMvc.perform(post("/review/edit")
                    .param("id", TEST_REVIEW_ID_MODIFY + "")
                    .param("bookId.id", HUCK_FINN_BOOK_ID + "")
                    .param("body", body)
                    .param("stars", stars + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_REVIEW_HOME_NAME));
            Review reviewResult = (Review) andExpect.andReturn().getModelAndView().getModelMap().get("review");
            assertTrue(reviewResult.getStars() == stars);
            assertTrue("verify title updated", reviewResult.getBody().equals(body));
            // Note! We need to test the int value, the Integer objects don't match?
            assertTrue("verify key/id is unchanged", reviewResult.getId().intValue() == TEST_REVIEW_ID_MODIFY);
        } catch (Exception ex) {
            Logger.getLogger(TestReviewController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the contoller dispatches a success response upon deleting a review.
     *  Note! Sucess is indicated by a simple redirect to the review home (list). The spring mvc result object should
     *  have an empty error list. 
     */
    @Test
    public void testDelete() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/review/delete")
                    .param("id", TEST_REVIEW_ID_DELETE + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_REVIEW_HOME_NAME))
                    .andExpect(model().attributeExists("review"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.review");
            assertTrue(bindingResult.getAllErrors().size() == 0);
        } catch (Exception ex) {
            Logger.getLogger(TestReviewController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult
                    = this.mockMvc.perform(get("/review/list.htm"))
                    .andExpect(view().name("review/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/review/list.jsp"));
            List<Review> list = (List<Review>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records ", (list.size() >= 4));
            int  num_reviews_after_delete = list.size();
              boolean RECORD_FOUND = false;
            for (Review review : list) {
                if (review.getId().intValue() == TEST_REVIEW_ID_DELETE) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify we did not find record", RECORD_FOUND);       
        } catch (Exception ex) {
            Logger.getLogger(TestReviewController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
