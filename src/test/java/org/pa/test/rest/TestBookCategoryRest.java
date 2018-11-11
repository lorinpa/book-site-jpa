package org.pa.test.rest;

import com.jayway.jsonpath.JsonPath;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 *
 * @category lorinpa
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/rest-context.xml")})
public class TestBookCategoryRest {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final static String LIST_URL = "/export/book_category/all";
    private final static String ADD_BOOK_CATEGORY_URL = "/add/book_category/%d/%d";
    private final static String MODIFY_BOOK_CATEGORY_URL = "/modify/book_category/%d/%d/%d";
    private final static String DELETE_BOOK_CATEGORY_URL = "/delete/book_category/%d";

    /**
     * *************************
     */
    // DELETE record
    private static int TEST_BOOK_CAT_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_BOOK_CAT_MODIFY_ID;
    // used to ADD
    private static int TEST_BOOK_CAT_ADD_ID;
    // used for LISt
    private static int HUCK_FINN_HUMOR_BOOK_CAT_ID;
    /**
     * * NOTE WE NEED TO CREATE TEMP CATEGORIES FOR OUT TEST *
     */
    // DELETE record
    private static int TEST_CATEGORY_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_CATEGORY_MODIFY_ID;
    // used to ADD
    private static int TEST_CATEGORY_ADD_ID;
    // more temp value
    private static int HUCK_FINN_BOOK_ID;
    private static int FICTION_CATEGORY_ID;

    /* 
     *  Let's add at least on Author before we run any tests.
     *  In addition let's capture the test category's id value and add the add id to our ArrayList.
     *  When we want to test modify category, we get the test record created here.
     */
    @BeforeClass
    public static void setUpClass() {
        // temp categories   
        TEST_CATEGORY_MODIFY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkrm");
        TEST_CATEGORY_DELETE_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkrd");
        TEST_CATEGORY_ADD_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkra");

        // lets use Huckleberry Finn as our book
        int mark_twain_author_id = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, mark_twain_author_id);
        // book categories
        TEST_BOOK_CAT_DELETE_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_DELETE_ID);
        TEST_BOOK_CAT_MODIFY_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_MODIFY_ID);

        FICTION_CATEGORY_ID = CaseGen.getInstance().getTestCategory(CaseGen.CATEGORY_FICTION_TITLE);
        HUCK_FINN_HUMOR_BOOK_CAT_ID = CaseGen.getInstance().getTestBookCategory(HUCK_FINN_BOOK_ID, FICTION_CATEGORY_ID);
    }

    /*  Delete test records which were created in the setUpClass
     *  When we are done with the suite of tests. We get each record id created for the test suite.
     *  We go through the list and delete each test record.
     */
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

    @Test
    public void listBookCategoriesTest() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.book_categories").isArray());
            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray obj = (JSONArray) JsonPath.read(content, "$.book_categories");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int list_size = ((JSONArray) obj).size();
            assertTrue("at least 2 book_categories returned", list_size > 0);

            JSONArray list = (JSONArray) obj;
            LinkedHashMap record;
            boolean RECORD_FOUND = false;
            Integer id;
            for (int nIndex = 0; nIndex < list_size; nIndex++) {
                record = (LinkedHashMap) list.get(nIndex);
                id = (Integer) record.get("id");
                if (id.intValue() == HUCK_FINN_HUMOR_BOOK_CAT_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify record  not found", RECORD_FOUND);

        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void addCategoryTest() {
        boolean noErrorsFound = true;
        try {
            String requestUrl = String.format(ADD_BOOK_CATEGORY_URL, HUCK_FINN_BOOK_ID, TEST_CATEGORY_ADD_ID);
            ResultActions requestResult = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.book_id").value(HUCK_FINN_BOOK_ID))
                    .andExpect(jsonPath("$.data.category_id").value(TEST_CATEGORY_ADD_ID))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
            String content = requestResult.andReturn().getResponse().getContentAsString();
            Integer id = (Integer) JsonPath.read(content, "$.data.id");
            TEST_BOOK_CAT_ADD_ID = id;
        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void modifyBookCategoryTest() {
        boolean noErrorsFound = true;
        try {
            String requestUrl
                    = String.format(MODIFY_BOOK_CATEGORY_URL, TEST_BOOK_CAT_MODIFY_ID, HUCK_FINN_BOOK_ID, TEST_CATEGORY_MODIFY_ID);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.id").value(TEST_BOOK_CAT_MODIFY_ID))
                    .andExpect(jsonPath("$.data.book_id").value(HUCK_FINN_BOOK_ID))
                    .andExpect(jsonPath("$.data.category_id").value(TEST_CATEGORY_MODIFY_ID))
                    .andExpect(jsonPath("$.status").value(MessageDefinitions.SUCCESS));
        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests that the controller returns a specific error message when 
     * we try and add a duplicate category (E.G. a second fiction)
     */
    @Test
    public void addDuplicateBookCateogryTest() {
        boolean noErrorsFound = true;
        try {
            String requestUrl = String.format(ADD_BOOK_CATEGORY_URL, HUCK_FINN_BOOK_ID, FICTION_CATEGORY_ID);
            ResultActions andExpect = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION));
        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /* 
     *  Tests the controller rejects a modify request which would result in a duplicate category.
     *   Verifies correct error message is dispatched
     */
    @Test
    public void duplicateEditCategoryTest() {
        boolean noErrorsFound = true;
        try {
            String requestUrl = String.format(MODIFY_BOOK_CATEGORY_URL, TEST_BOOK_CAT_MODIFY_ID, HUCK_FINN_BOOK_ID, FICTION_CATEGORY_ID);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION));

        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }


    /*
     *  Tests the controller processes a delete Review request properly.
     *   Verifies sucess message.
     *   Counts the number of category records before delete.
     *   Counts the number of category records after delete 
     *   Compares the counts and verifes the total is reduced by one.
     *   Verifies the id/ket of the deleted record does not exist in the second list view.
     */
    @Test
    public void deleteBookCategoryTest() {
        boolean noErrorsFound = true;
        try {
            String url = String.format(DELETE_BOOK_CATEGORY_URL, TEST_BOOK_CAT_DELETE_ID);
            ResultActions requestResult = this.mockMvc.perform(delete(url)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            String action = JsonPath.read(content, "$.action");
            String status = JsonPath.read(content, "$.status");
            LinkedHashMap categoryData = (LinkedHashMap) JsonPath.read(content, "$.data");
            Integer id = (Integer) categoryData.get("id");
            assertTrue("action is delete ", action.equals(MessageDefinitions.DEL_OPERATION));
            assertTrue("status is success ", status.equals(MessageDefinitions.SUCCESS));
            assertTrue("id is delete record id", (TEST_BOOK_CAT_DELETE_ID == id.intValue()));
        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);

        // get the new count after the delete
        noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray obj = (JSONArray) JsonPath.read(content, "$.book_categories");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_book_categories_after_delete = ((JSONArray) obj).size();
            assertTrue("verofy records returned", num_book_categories_after_delete > 0);
            // finally verify deleted id does not exist in the list of records
            JSONArray categoryList = (JSONArray) obj;
            LinkedHashMap categoryObj;
            boolean DELETE_ID_FOUND = false;
            Integer category_id;
            for (int nIndex = 0; nIndex < num_book_categories_after_delete; nIndex++) {
                categoryObj = (LinkedHashMap) categoryList.get(nIndex);
                category_id = (Integer) categoryObj.get("id");
                if (category_id.intValue() == TEST_BOOK_CAT_DELETE_ID) {
                    DELETE_ID_FOUND = true;
                    break;
                }
            }
            Assert.assertFalse("verify deleted record id not found", DELETE_ID_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestBookCategoryRest.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
