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

//import org.pa.dbutil.DbUtil;
import org.pa.entity.Book;

import org.pa.exception.MessageDetailDefinitions;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa
 
 Note! We use our CaseGen utility to create a set of test data

 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/dispatcher-context.xml")})

public class TestBookController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final String REDIRECT_TO_BOOK_HOME_NAME = "redirect:list.htm";
    // Note! THe book controller on adds and modifies forwords to home, not a redirect
    private final String BOOK_HOME_NAME = "book/list";

    // used to test add book
    private static Integer MARK_TWAIN_ID;

    private static int HUCLEBERRY_FINN_BOOK_ID;
    private static int JOE_SMOE_ID;

    // used to ssed value
    private static String className;
    // used for MODIFY
    private static int TEST_BOOK_ID_MODIFY;
    // used for DELETE test 
    private static int TEST_BOOK_ID_DELETE;
    // used for ADD test
    private static int TEST_BOOK_ID_ADD;

    @BeforeClass
    public static void setUpClass() {

        // use or test case generator to generate books
        MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);

        // used for LIST test 
        HUCLEBERRY_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, MARK_TWAIN_ID);

        JOE_SMOE_ID = CaseGen.getInstance().getTestAuthor(CaseGen.JOE, CaseGen.SMOE);

        if (className == null) {
            className = "BkCtrl";
        }
        // use an op in the name
        TEST_BOOK_ID_MODIFY = CaseGen.getInstance().createTestBook("M" + className + new Date().getTime(), JOE_SMOE_ID);

        TEST_BOOK_ID_DELETE = CaseGen.getInstance().createTestBook("D" + className + new Date().getTime(), JOE_SMOE_ID);

    }

    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_MODIFY);
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_DELETE);
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_ADD);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    /*
       * Tests the Author controller. Verifies the a view is dispatched with a list of Books
     */
    @Test
    public void testListView() {
        boolean no_errors_found = true;
        try {

            ResultActions requestResult
                    = this.mockMvc.perform(get("/book/list.htm"))
                    .andExpect(view().name("book/list"))
                    .andExpect(model().attributeExists("list"))
                    .andExpect(forwardedUrl("/WEB-INF/views/book/list.jsp"));
            List<Book> list = (List<Book>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records ", (list.size() > 0));
            boolean RECORD_FOUND = false;
            for (Book book : list) {
                if (book.getId().intValue() == HUCLEBERRY_FINN_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify list contained Huckleberry Finn", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     *   Tests the controller services the "add new book" function.
     *    Verify the view response is a 301 (redirect to book home/list).
     *    Verify the controller dispatches a model with the new author
     *    Verify the server assigned the new book an id (key) 
     *    Note! We are assuming the new id/key is going to be a number greated than existing keys values
     *
     *   Note! Add book does not do a redirect to home. It forwards to home. A work around for the gui error handling
     *   If the submit of an add or modify failes we need to repaint the form. We have to make sure the related author list is
     *   repopulated. Thus we return a ModelView instead of a redirect.
     */
    @Test
    public void testAddBook() {
        boolean no_errors_found = true;
        try {
            String title = new Date().getTime() + "";
            ResultActions andExpect = mockMvc.perform(post("/book/add")
                    .param("authorId.id", MARK_TWAIN_ID + "")
                    .param("title", title)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("book"))
                    .andExpect(status().isOk())
                    .andExpect(view().name(BOOK_HOME_NAME));
            Book bookResult = (Book) andExpect.andReturn().getModelAndView().getModelMap().get("book");
            assertTrue(bookResult.getTitle().equals(title));
            assertTrue("verify id is not null ", bookResult.getId() != null);
            // assertTrue(bookResult.getId() > greatestBookId);
            TEST_BOOK_ID_ADD = bookResult.getId();
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     *  Tests the controller dispatches a model with the updated book. 
     *  Note! We modify the last book generated by DbUtil to avoid conflicting with other tests. 
     */
    @Test
    public void testModBook() {
        boolean no_errors_found = true;
        try {
            String testTitle = new Date().getTime() + "mc";
            ResultActions andExpect = mockMvc.perform(post("/book/edit")
                    .param("id", TEST_BOOK_ID_MODIFY + "")
                    .param("authorId.id", JOE_SMOE_ID + "")
                    .param("title", testTitle)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name(BOOK_HOME_NAME));
            Book bookResult = (Book) andExpect.andReturn().getModelAndView().getModelMap().get("book");
            assertTrue("verify title updated", bookResult.getTitle().equals(testTitle));
            // Note! We need to test the int value, the Integer objects don't match?
            assertTrue("verify key/id is unchanged", bookResult.getId().intValue() == TEST_BOOK_ID_MODIFY);
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     * Tests the controller rejects attempt to add a duplicate author 
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testAddDuplicateCategory() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book/add")
                    .param("authorId.id", MARK_TWAIN_ID + "")
                    .param("title", CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("book/add"))
                    .andExpect(model().attributeExists("book"));
            // get the error message is nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.book");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("title", MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     * Tests the controller rejects attempt to modify a book so that it  duplicate an existing book/author 
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testModifyDuplicateCategory() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book/edit")
                    .param("id", TEST_BOOK_ID_MODIFY + "")
                    .param("authorId.id", MARK_TWAIN_ID + "")
                    .param("title", CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("book/edit"))
                    .andExpect(model().attributeExists("book"));
            // get the error message is nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.book");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("title", MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     *  Tests the contoller dispatches a success response upon deleting a book.
     *  Note! Sucess is indicated by a simple redirect to the category home (list). The spring mvc result object should
     *  have an empty error list. 
     * 
     *  We render the list books view twice. First time to get a count of books before delete.
     */
    @Test
    public void testDelete() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/book/delete").param("id", TEST_BOOK_ID_DELETE + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_BOOK_HOME_NAME))
                    .andExpect(model().attributeExists("book"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.book");
            assertTrue(bindingResult.getAllErrors().size() == 0);
        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);

        no_errors_found = true;
        try {
            ResultActions requestResult
                    = this.mockMvc.perform(get("/book/list.htm"))
                    .andExpect(view().name("book/list"))
                    .andExpect(model().attributeExists("list"))
                    //    .andExpect(model().attribute("list", hasItem(huckleBerryFinn)))
                    .andExpect(forwardedUrl("/WEB-INF/views/book/list.jsp"));
            List<Book> list = (List<Book>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            boolean RECORD_FOUND = false;
            for (Book book : list) {
                if (book.getId().intValue() == TEST_BOOK_ID_DELETE) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify list did not contained the deleted record id", RECORD_FOUND);

        } catch (Exception ex) {
            Logger.getLogger(TestBookController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

}
