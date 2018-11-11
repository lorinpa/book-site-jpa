package org.pa.test.rest;

import com.jayway.jsonpath.JsonPath;
import org.pa.rest.controller.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;

import org.pa.exception.MessageDetailDefinitions;
import org.pa.rest.message.MessageDefinitions;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa Note! We use DbUtil to create a test case set. Specifically
 * we create 4 books by 3 authors. Since we don't know what sequence the tests
 * are run in, our delete book test deletes from the end of our list. Likewise,
 * our tests that verify a specific book value at a specific list position use
 * the beginning of the list.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration

@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/rest-context.xml")})
public class TestBookRestController {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private final static String ADD_URL_TEMPLATE = "/add/book/set/%d/%s";
    private final static String MOD_URL_TEMPLATE = "/modify/book/set/%d/%d/%s";

    private static int HUCLEBERRY_FINN_BOOK_ID;
    private static int MARK_TWAIN_ID;

    // used for MODIFY test we need the author's id
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
            className = "Rest";
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
     *  Simply tests that we get a list of books in JSON format.
     *   Note! We use the test case set generated by DbUtil. Therefore,
     *   we know the first book should have a title of Hucleberry FInn.
     */
    @Test
    public void testListBooks() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(BookRest.EXPORT_ALL_URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.books").isArray())
                    .andExpect((jsonPath("$.books[0].title").value(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE)));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray obj = (JSONArray) JsonPath.read(content, "$.books");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_categories = ((JSONArray) obj).size();
            assertTrue("at least 3 books returned", num_categories > 0);
            Object jsonObj = ((JSONArray) obj).get(0);
            assertTrue("object is a json object", (jsonObj instanceof LinkedHashMap));
            Object id = ((LinkedHashMap) jsonObj).get("id");
            assertTrue("id is an integer", (id instanceof Integer));
            assertTrue(((Integer) id) == HUCLEBERRY_FINN_BOOK_ID);
        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     * Use the id generated by DbUtil and verify the controller
     * returns the book Huckleberry Finn in json format.
     */
    @Test
    public void getBookTest() {
        boolean no_errors_found = true;
        try {
            // get one of the books added by or DbUtil test case generator
            this.mockMvc.perform(get("/export/book/find/" + HUCLEBERRY_FINN_BOOK_ID).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.title").value(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE));

        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     * Test whether the controller returns an error message when we 
     * try and change a book and to a duplicate title of HuckleBerry Finn written by
     * Mark Twain.
     */
    @Test
    public void modifyDuplicateBookTest() {
        boolean no_errors_found = true;
        try {
            String url = String.format(MOD_URL_TEMPLATE, TEST_BOOK_ID_MODIFY,
                    MARK_TWAIN_ID, CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE);
            this.mockMvc.perform(put(url).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION));
        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    @Test
    public void addBookTest() {
        boolean no_errors_found = true;
        try {
            String title = new Date().getTime() + "ar";
            int authorId = MARK_TWAIN_ID;
            String requestUrl = String.format(ADD_URL_TEMPLATE, authorId, title);
            ResultActions requestResult = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.title").value(title));
            // let's verify the new book id
            String content = requestResult.andReturn().getResponse().getContentAsString();

            LinkedHashMap jsonObj = (LinkedHashMap) JsonPath.read(content, "$.data");
            assertTrue("object is a json object", (jsonObj instanceof LinkedHashMap));
            Integer id = (Integer) jsonObj.get("id");
            assertTrue("id is an integer", (id instanceof Integer));
            TEST_BOOK_ID_ADD = id;
        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }


    /*
     *  Test the book rest controller dispatches a success message along with a represntation 
     *  of the updated book (after a modify request). 
     */
    @Test
    public void modifyBookTest() {
        boolean no_errors_found = true;
        try {
            String newTitle = new Date().getTime() + "mr";
            String requestUrl = String.format(MOD_URL_TEMPLATE, TEST_BOOK_ID_MODIFY, JOE_SMOE_ID, newTitle);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.title").value(newTitle))
                    .andExpect(jsonPath("$.data.id").value(TEST_BOOK_ID_MODIFY))
                    .andExpect(jsonPath("$.data.author_id").value(JOE_SMOE_ID))
                    .andExpect(jsonPath("$.status").value(MessageDefinitions.SUCCESS));
        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    @Test
    public void deleteBookTest() {
        boolean no_errors_found = true;
        try {
            this.mockMvc.perform(delete("/delete/book/" + TEST_BOOK_ID_DELETE).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));

        } catch (Exception ex) {
            Logger.getLogger(TestBookRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        // lets verify 
        assertTrue("verify no exceptions thrown", no_errors_found);
    }
}
