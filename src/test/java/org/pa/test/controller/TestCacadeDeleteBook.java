package org.pa.test.controller;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.pa.entity.Book;

import org.pa.entity.BookCategory;
import org.pa.entity.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa public-action.org
 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/dispatcher-context.xml")})
public class TestCacadeDeleteBook {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final static String REDIRECT_BOOK_HOME_URL = "redirect:list.htm";
    // a temp review created for test
    private static int TEST_REVIEW_ID;
    // a temp book category created for test
    private static int TEST_CATEGORY_ID;
    private static int TEST_BOOK_CATEGORY_ID;

    // a temp author
    private static int TEST_AUTHOR_ID;
    // a temp book written by temp author
    private static int TEST_BOOK_ID;

    public TestCacadeDeleteBook() {
    }

    @BeforeClass
    public static void setUpClass() {
        String firstName = new Date().getTime() + "dbfn";
        String lastName = new Date().getTime() + "dbln";
        TEST_AUTHOR_ID = CaseGen.getInstance().createTestAuthor(firstName, lastName);
        String title = new Date().getTime() + "dbcb";
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
        CaseGen.getInstance().deleteTestAuthor(TEST_AUTHOR_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_ID);
        // just in case our test's fail
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID);
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CATEGORY_ID);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    /*
     *   Tests the BookCategory controller displays the result of a cascading delete.
     *   Both BookCategories and Reviews are dependent on the existance of a book entity. If the 
     *   application deletes the book all corresponding bookCategories and reviews are also deleted. 
     */
    @Test
    public void testDeleteBook() {

        boolean noErrorsFound = true;
        // now post delete request for Huckleberry finn
        try {
            ResultActions requestResult = mockMvc.perform(post("/book/delete").param("id", TEST_BOOK_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_BOOK_HOME_URL))
                    .andExpect(model().attributeExists("book"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.book");
            assertTrue(bindingResult.getAllErrors().size() == 0);
        } catch (Exception ex) {
            Logger.getLogger(TestCacadeDeleteBook.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
        
        boolean RECORD_FOUND;
        noErrorsFound = true;
        try {

            ResultActions requestResult = this.mockMvc.perform(get("/book/list"))
                    .andExpect(view().name("book/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/book/list.jsp"));

            List<Book> list = (List<Book>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have books", list.size() > 0);
            RECORD_FOUND = false;
            for (Book book : list) {
                if (book.getId().intValue() == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);

        } catch (Exception ex) {
            Logger.getLogger(TestCacadeDeleteBook.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {

            ResultActions requestResult = this.mockMvc.perform(get("/book-cat/list"))
                    .andExpect(view().name("book-cat/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/book-cat/list.jsp"));

            List<BookCategory> list = (List<BookCategory>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records ",list.size() > 0);
             RECORD_FOUND = false;
            for (BookCategory bookCat : list) {
                if (bookCat.getBookId().getId().intValue() == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCacadeDeleteBook.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get("/review/list"))
                    .andExpect(view().name("review/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/review/list.jsp"));

            List<Review> list = (List<Review>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records", list.size() >  0);
              RECORD_FOUND = false;
            for (Review review : list) {
                if (review.getBookId().getId().intValue() == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCacadeDeleteBook.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

    }
}
