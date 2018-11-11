package org.pa.test.controller;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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

import org.pa.entity.BookCategory;
import org.pa.exception.MessageDetailDefinitions;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

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

public class TestBooKCategoryController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final String REDIRECT_HOME_NAME = "redirect:list.htm";
    private final String HOME_NAME = "book-cat/list";
    
    // DELETE record
    private static int TEST_BOOK_CAT_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_BOOK_CAT_MODIFY_ID;
    // used to ADD
    private static int TEST_BOOK_CAT_ADD_ID;
    // used for LISt
    private static int HUCK_FINN_HUMOR_BOOK_CAT_ID;
    /*** NOTE WE NEED TO CREATE TEMP CATEGORIES FOR OUT TEST **/
    // DELETE record
    private static int TEST_CATEGORY_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_CATEGORY_MODIFY_ID;
    // used to ADD
    private static int TEST_CATEGORY_ADD_ID;
    // more temp value
    private static int HUCK_FINN_BOOK_ID;
    private static int FICTION_CATEGORY_ID;

    @BeforeClass
    public static void setUpClass() {

        // temp categories   
        TEST_CATEGORY_MODIFY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkm");
        TEST_CATEGORY_DELETE_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkd");
        TEST_CATEGORY_ADD_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bka");

        // lets use Huckleberry Finn as our book
        int mark_twain_author_id = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, mark_twain_author_id);
        // book categories
        TEST_BOOK_CAT_DELETE_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_DELETE_ID);
        TEST_BOOK_CAT_MODIFY_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_MODIFY_ID);

        FICTION_CATEGORY_ID = CaseGen.getInstance().getTestCategory(CaseGen.CATEGORY_FICTION_TITLE);
        HUCK_FINN_HUMOR_BOOK_CAT_ID = CaseGen.getInstance().getTestBookCategory(HUCK_FINN_BOOK_ID, FICTION_CATEGORY_ID);
    }

    @AfterClass
    public static void tearDownClass() {
        // categories
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_DELETE_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_MODIFY_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_ADD_ID);
        // book categories
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CAT_DELETE_ID);
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CAT_MODIFY_ID);
        CaseGen.getInstance().deleteTestBookCategory(TEST_BOOK_CAT_ADD_ID);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    /*
       * Tests the Author controller. Verifies the a view is dispatched with a list of Book Categories.
     */
    @Test
    public void testListView() {
        boolean noErrorsFound = true;
        try {

            ResultActions requestResult
                    = this.mockMvc.perform(get("/book-cat/list.htm"))
                    .andExpect(view().name("book-cat/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/book-cat/list.jsp"));
            List<BookCategory> list = (List<BookCategory>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify list has records ", (list.size() > 0));
            boolean RECORD_FOUND = false;
            for (BookCategory bookCat : list) {
                if (bookCat.getId().intValue() == HUCK_FINN_HUMOR_BOOK_CAT_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify list contained Huckleberry Finn", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *   Tests the controller services the "add new book cateogry" function.
     *    Verify the view response is a 301 (redirect to review home/list).
     *    Verify the controller dispatches a model with the new book category
     *    Verify the server assigned the new book category an id (key) 
     */
    @Test
    public void testAddBookCategory() {
        boolean noErrorsFound = true;
        try {
            ResultActions andExpect = mockMvc.perform(post("/book-cat/add")
                    .param("bookId.id", HUCK_FINN_BOOK_ID + "")
                    .param("categoryId.id", TEST_CATEGORY_ADD_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("bookCategory"))
                    .andExpect(status().isOk())
                    .andExpect(view().name(HOME_NAME));
            BookCategory bookCatResult = (BookCategory) andExpect.andReturn().getModelAndView().getModelMap().get("bookCategory");
            assertTrue("verify we have an id ", bookCatResult.getId() != null);
            assertTrue(bookCatResult.getBookId().getId().intValue() == HUCK_FINN_BOOK_ID);
            assertTrue(bookCatResult.getCategoryId().getId().intValue() == TEST_CATEGORY_ADD_ID);
            TEST_BOOK_CAT_ADD_ID = bookCatResult.getId();

        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the controller dispatches a model with the updated book category 
     *  Note! We modify the last book generated by DbUtil to avoid conflicting with other tests. 
     */
    @Test
    public void testModBookReview() {
        boolean noErrorsFound = true;
        try {
            ResultActions andExpect = mockMvc.perform(post("/book-cat/edit")
                    .param("id", TEST_BOOK_CAT_MODIFY_ID + "")
                    .param("bookId.id", HUCK_FINN_BOOK_ID + "")
                    .param("categoryId.id", TEST_CATEGORY_MODIFY_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name(HOME_NAME));
            BookCategory bookCatResult = (BookCategory) andExpect.andReturn().getModelAndView().getModelMap().get("bookCategory");
            assertTrue("verify the id did not change", bookCatResult.getId().intValue() == TEST_BOOK_CAT_MODIFY_ID);
            assertTrue(bookCatResult.getBookId().getId().intValue() == HUCK_FINN_BOOK_ID);
            assertTrue(bookCatResult.getCategoryId().getId().intValue() == TEST_CATEGORY_MODIFY_ID);
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the contoller dispatches a success response upon deleting a book category.
     *  Note! Sucess is indicated by a simple redirect to the book cateogry home (list). The spring mvc result object should
     *  have an empty error list. 
     * 
     *  We render the list book categories view twice. First time to get a count of book categories before delete.
     *  Second time to get count of book categories after the delete.  The second time should have a list who's size
     *  is exactlry one less than our original count. 
     */
    @Test
    public void testDelete() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book-cat/delete")
                    .param("id", TEST_BOOK_CAT_DELETE_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_HOME_NAME))
                    .andExpect(model().attributeExists("bookCategory"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.bookCategory");
            assertTrue(bindingResult.getAllErrors().isEmpty());
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult
                    = this.mockMvc.perform(get("/book-cat/list.htm"))
                    .andExpect(view().name("book-cat/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/book-cat/list.jsp"));
            List<BookCategory> list = (List<BookCategory>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records ", (list.size() > 0 ));
            boolean RECORD_FOUND = false;
            for (BookCategory bookCat : list) {
                if (bookCat.getId().intValue() == TEST_BOOK_CAT_DELETE_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record is not found", RECORD_FOUND);     
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     * Tests the controller rejects an attempt to add a duuplicate bookCategory.
     * Verifies the correct error message is dispatched.
     * Verifies the add view is dispathed
     */
    @Test
    public void testAddDuplicate() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book-cat/add")
                    .param("bookId.id", HUCK_FINN_BOOK_ID + "")
                    .param("categoryId.id", FICTION_CATEGORY_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("bookCategory"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("book-cat/add"));

            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.bookCategory");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("bookCat", MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
         assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the controller rejects an attempt to modify a record so that it becomes a duplicate.
     *  Verifies correct error message displatched.
     *  Verifies the modify view is dispatched (so the user can recover/correct)
     */
    @Test
    public void testModDuplicate() {
         boolean noErrorsFound = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book-cat/edit")
                    .param("id", TEST_BOOK_CAT_MODIFY_ID + "")
                    .param("bookId.id", HUCK_FINN_BOOK_ID +"")
                    .param("categoryId.id",  FICTION_CATEGORY_ID+ "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("book-cat/edit"));

            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.bookCategory");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("bookCat", MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestBooKCategoryController.class.getName()).log(Level.SEVERE, null, ex);
             noErrorsFound = false;
        }
          assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
