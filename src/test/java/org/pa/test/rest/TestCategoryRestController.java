package org.pa.test.rest;

//import com.jayway.jsonpath.JsonModel;
import com.jayway.jsonpath.JsonPath;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
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
public class TestCategoryRestController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final static String LIST_URL = "/export/category/all";
    private final static String ADD_CATEGORY_URL = "/add/category/%s";
    private final static String MODIFY_CATEGORY_URL = "/modify/category/%d/%s";
    private final static String DELETE_CATEGORY_URL = "/delete/category/%d";

    // DELETE record
    private static int TEST_CATEGORY_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_CATEGORY_MODIFY_ID;
    // used to ADD
    private static int TEST_CATEGORY_ADD_ID;
    // used for LISt
    private static int FICTION_CATEGORY_ID;

    /* 
     *  Let's add at least on Author before we run any tests.
     *  In addition let's capture the test category's id value and add the add id to our ArrayList.
     *  When we want to test modify category, we get the test record created here.
     */
    @BeforeClass
    public static void setUpClass() {
        FICTION_CATEGORY_ID = CaseGen.getInstance().getTestCategory(CaseGen.CATEGORY_FICTION_TITLE);
        TEST_CATEGORY_DELETE_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "dr");
        TEST_CATEGORY_MODIFY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "mr");
    }

    /*  Delete test records which were created in the setUpClass
     *  When we are done with the suite of tests. We get each record id created for the test suite.
     *  We go through the list and delete each test record.
     */
    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_DELETE_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_MODIFY_ID);
        CaseGen.getInstance().deleteTestCategory(TEST_CATEGORY_ADD_ID);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void listCategoriesTest() {
        boolean no_errors = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.categories").isArray())
                    .andExpect((jsonPath("$.categories[0].id").value(FICTION_CATEGORY_ID)));
            /*
             *  The following represents a different style of expressing the tests.
             *   We  walk the response data directly.
             */
            String content = requestResult.andReturn().getResponse().getContentAsString();
            Object obj = JsonPath.read(content, "$.categories");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_categories = ((JSONArray) obj).size();
            assertTrue("at least 1 category returned", num_categories > 0);
            // let's find FICTION
            JSONArray list = (JSONArray) obj;
            LinkedHashMap row = null;
            Integer id = null;
            boolean RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_categories; nIndex++) {
                row = (LinkedHashMap) list.get(nIndex);
                id = (Integer) row.get("id");
                if (id == FICTION_CATEGORY_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify we found record", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    @Test
    public void addCategoryTest() {
        boolean no_errors = true;
        try {
            String title = new Date().getTime() + "a";
            String requestUrl = String.format(ADD_CATEGORY_URL, title);
            ResultActions requestResult = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.title").value(title))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
            String content = requestResult.andReturn().getResponse().getContentAsString();
            Integer id = (Integer) JsonPath.read(content, "$.data.id");
            TEST_CATEGORY_ADD_ID = id;
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    @Test
    public void modifyCategoryTest() {
        boolean no_errors = true;
        try {
            String newTitle = new Date().getTime() + "mc";
            String requestUrl = String.format(MODIFY_CATEGORY_URL, TEST_CATEGORY_MODIFY_ID, newTitle);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.data.title").value(newTitle))
                    .andExpect(jsonPath("$.status").value(MessageDefinitions.SUCCESS));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     *  Tests that the controller returns a specific error message when 
     * we try and add a duplicate category (E.G. a second fiction)
     */
    @Test
    public void addDuplicateCateogryTest() {
        boolean no_errors = true;
        try {
            String requestUrl = String.format(ADD_CATEGORY_URL, CaseGen.CATEGORY_FICTION_TITLE);
            ResultActions andExpect = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /* 
     *  Tests the controller rejects a modify request which would result in a duplicate category.
     *   Verifies correct error message is dispatched
     */
    @Test
    public void duplicateEditCategoryTest() {
        boolean no_errors = true;
        try {
            String requestUrl = String.format(MODIFY_CATEGORY_URL, TEST_CATEGORY_MODIFY_ID, CaseGen.CATEGORY_FICTION_TITLE);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
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
    public void deleteCategoryTest() {
        boolean no_errors = true;
        try {
            String url = String.format(DELETE_CATEGORY_URL, TEST_CATEGORY_DELETE_ID);
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
            assertTrue("id is original", (TEST_CATEGORY_DELETE_ID == id));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);

        no_errors = true;
        // get the new list after delete, verify record is gone
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content()
                            .contentType("application/json;charset=UTF-8"));
            String content = requestResult.andReturn().getResponse().getContentAsString();
            JSONArray obj = (JSONArray) JsonPath.read(content, "$.categories");
            int num_categories = ((JSONArray) obj).size();
            // finally verify deleted id does not exist in the list of records
            LinkedHashMap row;
            JSONArray list = (JSONArray) obj;
            Integer id;
            boolean RECORD_FOUND = false;
            for (int nIndex = 0; nIndex < num_categories; nIndex++) {
                row = (LinkedHashMap) list.get(nIndex);
                id = (Integer) row.get("id");
                if (id == TEST_CATEGORY_DELETE_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify we did not find record", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryRestController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

}
