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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;

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
public class TestRestCascadeDeleteAuthor {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    private final static String DELETE_AUTHOR_URL = "/delete/authors/%d";

    // a temp review created for test
    private static int TEST_REVIEW_ID;
    // a temp book category created for test
    private static int TEST_CATEGORY_ID;
    private static int TEST_BOOK_CATEGORY_ID;

    // a temp author
    private static int TEST_AUTHOR_ID;
    // a temp book written by temp author
    private static int TEST_BOOK_ID;

    @BeforeClass
    public static void setUpClass() {
        String firstName = new Date().getTime() + "rdbf";
        String lastName = new Date().getTime() + "rdbl";
        TEST_AUTHOR_ID = CaseGen.getInstance().createTestAuthor(firstName, lastName);
        String title = new Date().getTime() + "rbcb";
        TEST_BOOK_ID = CaseGen.getInstance().createTestBook(title, TEST_AUTHOR_ID);
        // create our temp review
        String TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "rbcr";
        TEST_REVIEW_ID = CaseGen.getInstance().createTestReview(TEST_BOOK_ID, 5, TEST_REVIEW_BODY_MODIFY);
        // create a test category
        TEST_CATEGORY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "rbcc");
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
    public void deleteAuthor() {
        boolean noErrorsFound = true;
        boolean RECORD_FOUND;
        int num_records;
        try {
            String url = String.format(DELETE_AUTHOR_URL, TEST_AUTHOR_ID);
            ResultActions requestResult = this.mockMvc.perform(delete(url)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            String action = JsonPath.read(content, "$.action");
            String status = JsonPath.read(content, "$.status");
            LinkedHashMap authorData = JsonPath.read(content, "$.data");
            Integer id = (Integer) authorData.get("id");
            assertTrue("action is delete ", action.equals(MessageDefinitions.DEL_OPERATION));
            assertTrue("status is success ", status.equals(MessageDefinitions.SUCCESS));
            assertTrue("id is Joe smoe's", (TEST_AUTHOR_ID == id));
        } catch (Exception ex) {
            Logger.getLogger(TestRestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(AuthorRest.EXPORT_ALL_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.authors").isArray());

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray list = (JSONArray) JsonPath.read(content, "$.authors");
            assertTrue("list is a json array", (list instanceof JSONArray));
            num_records = list.size();
            assertTrue("verify we have records", num_records > 0);

            LinkedHashMap record;
            Integer id;
            RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_records; nIndex++) {
                record = (LinkedHashMap) list.get(nIndex);
                id = (Integer) record.get("id");
                if (id == TEST_AUTHOR_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);

        } catch (Exception ex) {
            Logger.getLogger(TestRestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(BookRest.EXPORT_ALL_URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.books").isArray());

            String content = requestResult.andReturn().getResponse().getContentAsString();

            JSONArray list = (JSONArray) JsonPath.read(content, "$.books");
            assertTrue("object is a json array", (list instanceof JSONArray));
            num_records = list.size();
            assertTrue("verify we have records", num_records > 0);

            LinkedHashMap record;
            Integer id;
            RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_records; nIndex++) {
                record = (LinkedHashMap) list.get(nIndex);
                id = (Integer) record.get("id");
                if (id == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestRestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get("/export/book_category/all")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.book_categories").isArray());

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray list = (JSONArray) JsonPath.read(content, "$.book_categories");
            num_records = list.size();
            assertTrue("verify we have records", num_records > 0);
            LinkedHashMap record;
            Integer id;
            RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_records; nIndex++) {
                record = (LinkedHashMap) list.get(nIndex);
                id = (Integer) record.get("book_id");
                if (id == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestRestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get("/export/review/all")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray list = (JSONArray) JsonPath.read(content, "$.reviews");
            assertTrue("object is a json array", (list instanceof JSONArray));
            num_records = list.size();
            assertTrue("verify we have records", num_records > 0);

            LinkedHashMap record;
            Integer id;
            RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_records; nIndex++) {
                record = (LinkedHashMap) list.get(nIndex);
                id = (Integer) record.get("book_id");
                if (id == TEST_BOOK_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestRestCascadeDeleteAuthor.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
