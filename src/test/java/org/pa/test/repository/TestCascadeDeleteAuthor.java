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
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.pa.entity.Author;

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
public class TestCascadeDeleteAuthor extends TestCase {

    @Autowired
    private AuthorsRepository authorRepo;
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

    public TestCascadeDeleteAuthor() {
    }

    @BeforeClass
    public static void setUpClass() {
        String firstName = new Date().getTime() + "dafn";
        String lastName = new Date().getTime() + "daln";
        TEST_AUTHOR_ID = CaseGen.getInstance().createTestAuthor(firstName, lastName);
        String title = new Date().getTime() + "dacb";
        TEST_BOOK_ID = CaseGen.getInstance().createTestBook(title, TEST_AUTHOR_ID);
        // create our temp review
        String TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "dacr";
        TEST_REVIEW_ID = CaseGen.getInstance().createTestReview(TEST_BOOK_ID, 5, TEST_REVIEW_BODY_MODIFY);
        // create a test category
        TEST_CATEGORY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "dacc");
        // now the test book - category
        TEST_BOOK_CATEGORY_ID = CaseGen.getInstance().createTestBookCategory(TEST_BOOK_ID, TEST_CATEGORY_ID);
    }

    @AfterClass
    public static void tearDownClass() {

        CaseGen.getInstance().deleteTestAuthor(TEST_AUTHOR_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_ID);
        // just in case our test's fail
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID);
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CATEGORY_ID);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDeleteAuthor() {
        boolean noErrorsFound = true;
        try {
            authorRepo.delete(TEST_AUTHOR_ID);

        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        // now let's verify we don't find test Author
        boolean author_found = false;
        List<Author> authors = authorRepo.findAll();
        for (Author author : authors) {
            if (author.getId().intValue() == TEST_AUTHOR_ID) {
                author_found = true;
                break;
            }
        }
        assertFalse("verify test author not found", author_found);

        // now let's verify we don't find test author books
        boolean book_found = false;
        List<Book> books = bookRepo.findAll();
        for (Book book : books) {
            if (book.getAuthorId().getId().intValue() == TEST_AUTHOR_ID) {
                book_found = true;
                break;
            }
        }
        assertFalse("verify test author not found", book_found);

        // now let's verify we don't find test author books categorized
        boolean book_categories_found = false;
        List<BookCategory> bookCats = bookCatRepo.findAll();
        for (BookCategory bookCat : bookCats) {
            if (bookCat.getBookId().getAuthorId().getId().intValue() == TEST_AUTHOR_ID) {
                book_categories_found = true;
                break;
            }
        }
        assertFalse("verofy test author not found", book_categories_found);

        // now let's verify we don't find test author book reviews
        boolean reviews_found = false;
        List<Review> reviews = reviewRepo.findAll();
        for (Review review : reviews) {
            if (review.getBookId().getAuthorId().getId().intValue() == TEST_AUTHOR_ID) {
                reviews_found = true;
                break;
            }
        }
        assertFalse("verofy test author not found", book_found);
    }

}
