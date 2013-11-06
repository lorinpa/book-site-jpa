/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.test.rest;

import org.pa.rest.controller.*;
import com.jayway.jsonpath.JsonModel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;

import org.pa.exception.MessageDetailDefinitions;
import org.pa.rest.message.MessageDefinitions;
import org.pa.test.category.Critical;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 *
 * @author lorinpa
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/rest-context.xml")})
public class TestAuthorRestController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final static String ADD_AUTHOR_URL = "/add/authors/%s/%s";
    private final static String MODIFY_AUTHOR_URL = "/modify/authors/%d/%s/%s";
    private final static String DELETE_AUTHOR_URL = "/delete/authors/%d";

    // used for list test
    private static int MARK_TWAIN_ID;
    // used to test add and MODIFY -- Same as ED SMED ID
    private static int LAST_GENERATED_AUTHOR_ID;
    // used for DELETE test 
    private static int JOE_SMOE_ID;

    // used to seed values
    private static String className;
    // DELETE record
    private static int TEST_AUTHOR_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_AUTHOR_MODIFY_ID;
    // used to ADD
    private static int TEST_AUTHOR_ADD_ID;
    /* 
     *  Let's add at least on Author before we run any tests.
     *  In addition let's capture the test author's id value and add the add id to our ArrayList.
     *  When we want to test modify author, we get the test record created here.
     */

    @BeforeClass
    public static void setUpClass() {

        MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        JOE_SMOE_ID = CaseGen.getInstance().getTestAuthor(CaseGen.JOE, CaseGen.SMOE);
        if (className == null) {
            className = "AuthorRestController";
        }
        TEST_AUTHOR_DELETE_ID = CaseGen.getInstance().createTestAuthor(new Date().getTime() + "", className);
        TEST_AUTHOR_MODIFY_ID = CaseGen.getInstance().createTestAuthor(new Date().getTime() + "", className);

    }

    /*  Delete test records which were created in the setUpClass
     *  When we are done with the suite of tests. We get each record id created for the test suite.
     *  We go through the list and delete each test record.
     */
    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_DELETE_ID);
        CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_MODIFY_ID);
        CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_ADD_ID);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void authorExportTest() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(AuthorRest.EXPORT_ALL_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.authors").isArray())
                    .andExpect((jsonPath("$.authors[0].id").value(MARK_TWAIN_ID)));
            /*
             *  The following represents a different style of expressing the tests.
             *   We  walk the response data directly.
             */
            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);

            Object obj = model.get("$.authors");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_authors = ((JSONArray) obj).size();
            assertTrue("at least 2 authors returned", num_authors >= 2);
            Object jsonObj = ((JSONArray) obj).get(0);
            assertTrue("object is a json object", (jsonObj instanceof JSONObject));
            Object id = ((JSONObject) jsonObj).get("id");
            assertTrue("id is an integer", (id instanceof Integer));
            assertTrue(((Integer) id).intValue() == MARK_TWAIN_ID);

        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void addAuthorTest() {
        boolean noErrorsFound = true;
        try {
            String lastName = new Date().getTime() + "";
            String firstName = "testRestC";
            String requestUrl = String.format(ADD_AUTHOR_URL, firstName, lastName);
            ResultActions requestResult = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.lastName").value(lastName))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            Integer idObj = (Integer) model.get("$.data.id");
            TEST_AUTHOR_ADD_ID = idObj;
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void modifyAuthorTest() {
        boolean noErrorsFound = true;
        try {
            String newLastName = "NS" + new Date().getTime();

            String requestUrl = String.format(MODIFY_AUTHOR_URL, TEST_AUTHOR_MODIFY_ID, "Ed", newLastName);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.lastName").value(newLastName))
                    .andExpect(jsonPath("$.status").value(MessageDefinitions.SUCCESS));
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests that the controller returns a specific error message when 
     * we try and add a duplicate author (E.G. a second Mark Twain(.
     */
    @Test
    public void duplicateAddAuthorTest() {
        boolean noErrorsFound = true;
        try {
            String requestUrl = String.format(ADD_AUTHOR_URL, CaseGen.MARK, CaseGen.TWAIN);
            ResultActions andExpect = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION));
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /* 
     *  Tests the controller rejects a modify request which would result in a duplicate author.
     *   Verifies correct error message is dispatched
     */
    @Category(Critical.class)
    @Test
    public void duplicateEditAuthorTest() {
        boolean noErrorsFound = true;
        try {

            String requestUrl = String.format(MODIFY_AUTHOR_URL, TEST_AUTHOR_MODIFY_ID, CaseGen.MARK, CaseGen.TWAIN);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.EXCEPTION").value(MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION));

        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the controller processes a delete Author request properly.
     *   Verifies sucess message.
     *   Compares the counts and verifes the total is reduced by one.
     *   Verifies the id/ket of the deleted record does not exist in the second list view.
     */
    @Test
    public void deleteAuthorTest() {

        boolean noErrorsFound = true;
        try {
            String url = String.format(DELETE_AUTHOR_URL, TEST_AUTHOR_DELETE_ID);
            ResultActions requestResult = this.mockMvc.perform(delete(url)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            JSONObject obj = (JSONObject) model.getJsonObject();

            assertTrue("object is a json Object", (obj instanceof JSONObject));
            String action = obj.get("action").toString();
            String status = obj.get("status").toString();
            JSONObject authorData = (JSONObject) obj.get("data");
            Integer id = (Integer) authorData.get("id");
            assertTrue("action is delete ", action.equals(MessageDefinitions.DEL_OPERATION));
            assertTrue("status is success ", status.equals(MessageDefinitions.SUCCESS));
            assertTrue("id is Joe smoe's", (TEST_AUTHOR_DELETE_ID == id.intValue()));
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
        
        noErrorsFound = true;
        // get the new count after the delete
        try {
            ResultActions requestResult = this.mockMvc.perform(get(AuthorRest.EXPORT_ALL_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            Object obj = model.get("$.authors");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int list_size = ((JSONArray) obj).size();

            // finally verify deleted id does not exist in the list of records
            JSONArray authorList = (JSONArray) obj;
            JSONObject authorObj;
            boolean RECORD_FOUND = false;
            Integer author_id;
            for (int nIndex = 0; nIndex < list_size; nIndex++) {
                authorObj = (JSONObject) authorList.get(nIndex);
                author_id = (Integer) authorObj.get("id");
                if (author_id.intValue() == TEST_AUTHOR_DELETE_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            Assert.assertFalse("verify deleted record id not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
          assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
