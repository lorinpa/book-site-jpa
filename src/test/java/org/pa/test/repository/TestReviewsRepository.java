/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.test.repository;

import org.pa.repository.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;

import org.pa.entity.Book;
import org.pa.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author lorinpa public-action.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class TestReviewsRepository extends TestCase {

    @Autowired
    private ReviewsRepository reviewRepo;
    @Autowired
    private BooksRepository bookRepo;

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

    public TestReviewsRepository() {
    }

    @BeforeClass
    public static void setUpClass() {
        
        int MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, MARK_TWAIN_ID);

        TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "rrm";
        TEST_REVIEW_ID_MODIFY = CaseGen.getInstance().createTestReview(HUCK_FINN_BOOK_ID, TEST_REVIEW_STARS_MODIFY, TEST_REVIEW_BODY_MODIFY);

        TEST_REVIEW_BODY_DELETE = new Date().getTime() + "rrd";
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testList() {
        boolean noErrorsFound = true;
        try {
            List<Review> list = reviewRepo.findAll();
            assertNotNull("verify we got results", list);
            assertTrue("verify we have records", list.size() > 0);

            boolean RECORD_FOUND = false;
            for (Review review : list) {
                if (review.getId().intValue() == HUCK_FINN_5_STAR_REVIEW) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify we found 5 star review of Huck Finn", RECORD_FOUND);
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestReviewsRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void testAddReview() {
        String TEST_REVIEW_BODY_ADD = new Date().getTime() + "rra";
        boolean noErrorsFound = true;
        int stars = 2;
        try {
            Review newReview = new Review();
            newReview.setBody(TEST_REVIEW_BODY_ADD);
            Book bookHuckFinn = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
            newReview.setBookId(bookHuckFinn);
            newReview.setStars(stars);
            reviewRepo.addNew(newReview);
            assertTrue("verify an id was assigned", newReview.getId() != null);
            TEST_REVIEW_ID_ADD = newReview.getId();
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestReviewsRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void testModifyReview() {
        boolean noErrorsFound = true;
        String body = "this is a new review" + new Date().getTime();
        int stars = 2; // different than 4
        Book book = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
        try {
             Review updatedReview = reviewRepo.update(TEST_REVIEW_ID_MODIFY, book, stars, body);
            assertTrue("verify the updated review had the original review id", updatedReview.getId() == TEST_REVIEW_ID_MODIFY);
            assertTrue("verify the updated review has the correct stars value", updatedReview.getStars() == stars);
            assertTrue("verify the updated review has the correct body value", updatedReview.getBody().equals(body));
            assertTrue("verify the updated review has the correct book value", updatedReview.getBookId().getId().intValue() == HUCK_FINN_BOOK_ID);        
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestReviewsRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void testDelete() {
        boolean noErrorsFound = true;
        try {
            reviewRepo.delete(new Integer(TEST_REVIEW_ID_DELETE));
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestReviewsRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
        // now lets search the findAll results and verify the deleted review is gone
        List<Review> list = reviewRepo.findAll();
        boolean record_found = false;
        for (Review review : list) {
            if (review.getId().intValue() == TEST_REVIEW_ID_DELETE) {
                record_found = true;
                break;
            }
        }
        assertFalse("verify we did not find the record", record_found);
    }

}
