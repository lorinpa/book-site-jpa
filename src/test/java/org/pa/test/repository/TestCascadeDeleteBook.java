/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pa.test.repository;

import java.util.Date;
import org.pa.repository.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertTrue;
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

import org.pa.entity.BookCategory;
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
public class TestCascadeDeleteBook extends TestCase {
    
    @Autowired
    private BooksRepository bookRepo;
    
    @Autowired
    private ReviewsRepository reviewRepo;
    
    @Autowired
    private BookCategoriesRepository bookCatRepo;
    
     // a temp review created for test
     private static int TEST_REVIEW_ID;
    // a temp book category created for test
     private static int TEST_CATEGORY_ID;
     private static int TEST_BOOK_CATEGORY_ID;
     
     // a temp author
     private static int TEST_AUTHOR_ID;
      // a temp book written by temp author
     private static int TEST_BOOK_ID;
    
    
    public TestCascadeDeleteBook() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        String firstName = new Date().getTime() + "dbfn";
        String lastName = new Date().getTime()  + "dbln";
         TEST_AUTHOR_ID = CaseGen.getInstance().createTestAuthor(firstName,lastName);
         String title = new Date().getTime( ) +"dbcb";
         TEST_BOOK_ID = CaseGen.getInstance().createTestBook(title, TEST_AUTHOR_ID);
         // create our temp review
         String TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "dbcr";
        TEST_REVIEW_ID = CaseGen.getInstance().createTestReview(TEST_BOOK_ID, 5, TEST_REVIEW_BODY_MODIFY);
       // create a test category
        TEST_CATEGORY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "dbcc");
        // now the test book - category
        TEST_BOOK_CATEGORY_ID = CaseGen.getInstance().createTestBookCategory(TEST_BOOK_ID, TEST_CATEGORY_ID);
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID);
        CaseGen.getInstance().deleteTestAuthor(TEST_AUTHOR_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_ID);
        // just in case our test's fail
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CATEGORY_ID);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID);
    }

    /*
    *  Tests that the repositories reflect a cascading delete. Reviews and BookCategories are dependent on Books.
    *  THat means when we delete a book, the database should automatically delete are of the corresponding reviews of that book, 
    *  and any categorizations of  that book. 
    *
    *   We use a temp book  as our test case. We verify that all reviews of temp book are deleted as well as categorizations of temp book.
    */
    @Test 
    public void testDeleteBook() {
         boolean noErrorsFound = true;
        try {
            bookRepo.delete(new Integer(TEST_BOOK_ID));
        }  catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestCascadeDeleteBook.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
        
        //  let's verify we don't find  temp book
        boolean book_found = false;
        List<Book> books = bookRepo.findAll();
        for (Book book : books) {
            if (book.getId().intValue() == TEST_BOOK_ID) {
                book_found = true;
                break;
            }
        }
        assertFalse("verify we did not find book category of temp book", book_found);
        // now let's verify we don't find the reviews of temp book
        boolean review_found = false;
        List<Review> reviews = reviewRepo.findAll();
        for (Review review: reviews) {
            if (review.getBookId().getId().intValue() == TEST_BOOK_ID) {
                review_found = true;
                break;
            }
        }
        assertFalse("verify we did not find review of temp book", review_found);
        
        // now let's verify we don't find a book category of temp book
        boolean book_cat_found = false;
        List<BookCategory> bookCats = bookCatRepo.findAll();
        for (BookCategory bookCat : bookCats) {
            if (bookCat.getBookId().getId().intValue() == TEST_BOOK_ID) {
                book_cat_found = true;
                break;
            }
        }
        assertFalse("verify we did not find book category of temp book", book_cat_found);
    
    }
}
