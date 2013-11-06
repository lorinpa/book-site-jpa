package org.pa.test.rest;

import com.jayway.jsonpath.JsonModel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static junit.framework.Assert.assertFalse;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 *
 * @review lorinpa
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/rest-context.xml")})
public class TestReviewRestController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final static String LIST_URL = "/export/review/all";
    private final static String ADD_REVIEW_URL = "/add/review/%s/%d/%d";
    private final static String MODIFY_REVIEW_URL = "/modify/review/%d/%s/%d/%d";
    private final static String DELETE_REVIEW_URL = "/delete/review/%d";

    // review that should be in the test cases
    private static int HUCK_FINN_5_STAR_REVIEW;

    // used for various tests
    private static int HUCK_FINN_BOOK_ID;

    // ADD
    private static int TEST_REVIEW_ID_ADD;

    // MODIFY
    private static int TEST_REVIEW_ID_MODIFY;
    private static final int TEST_REVIEW_STARS_MODIFY = 3;
    private static String TEST_REVIEW_BODY_MODIFY;

    // DELETE
    private static int TEST_REVIEW_ID_DELETE;
    private static final int TEST_REVIEW_STARS_DELETE = 0;
    private static String TEST_REVIEW_BODY_DELETE;

    /* 
     *  Let's add at least on Author before we run any tests.
     *  In addition let's capture the test review's id value and add the add id to our ArrayList.
     *  When we want to test modify review, we get the test record created here.
     */
    @BeforeClass
    public static void setUpClass() {

        int MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, MARK_TWAIN_ID);

        TEST_REVIEW_BODY_MODIFY = new Date().getTime() + "rrm";
        TEST_REVIEW_ID_MODIFY = CaseGen.getInstance().createTestReview(HUCK_FINN_BOOK_ID, TEST_REVIEW_STARS_MODIFY, TEST_REVIEW_BODY_MODIFY);

        TEST_REVIEW_BODY_DELETE = new Date().getTime() + "rrd";
        TEST_REVIEW_ID_DELETE = CaseGen.getInstance().createTestReview(HUCK_FINN_BOOK_ID, TEST_REVIEW_STARS_DELETE, TEST_REVIEW_BODY_DELETE);

        HUCK_FINN_5_STAR_REVIEW = CaseGen.getInstance().getTestReview(HUCK_FINN_BOOK_ID, 5, CaseGen.REVIEW_BODY_FIVE_STAR);
    }

    /*  Delete test records which were created in the setUpClass
     *  When we are done with the suite of tests. We get each record id created for the test suite.
     *  We go through the list and delete each test record.
     */
    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_ADD);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_MODIFY);
        CaseGen.getInstance().deleteTestReview(TEST_REVIEW_ID_DELETE);
    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(this.wac).build();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void listReviewsTest() {
        boolean noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.reviews").isArray());
                 
            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            Object obj = model.get("$.reviews");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_reviews = ((JSONArray) obj).size();
            assertTrue("verify we have records", num_reviews > 0);
            boolean RECORD_FOUND = false;
            JSONArray list = (JSONArray) obj;
            JSONObject record;
            Integer id;
            for (int nIndex= 0; nIndex < num_reviews; nIndex++) {
                record =(JSONObject) list.get(nIndex);
                id = (Integer)record.get("id");
                if (id == HUCK_FINN_5_STAR_REVIEW ) {
                    RECORD_FOUND = true;
                    break;                      
                }
            }
            assertTrue("verify we found record", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestReviewRestController.class.getName()).log(Level.SEVERE, null, ex);
             noErrorsFound = false;
        }
         assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void addReviewTest() {
          boolean noErrorsFound = true;
        try {
            String body = "A new added review"+new Date().getTime();
            int stars = 3;

            String requestUrl = String.format(ADD_REVIEW_URL, body, stars, HUCK_FINN_BOOK_ID);
            ResultActions requestResult = this.mockMvc.perform(post(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.body").value(body))
                    .andExpect(jsonPath("$.data.stars").value(stars))
                    .andExpect(jsonPath("$.data.book_id").value(HUCK_FINN_BOOK_ID))
                    .andExpect(jsonPath("$.status").value("SUCCESS"));
            
             String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            TEST_REVIEW_ID_ADD = (Integer) model.get("$.data.id");
        } catch (Exception ex) {
            Logger.getLogger(TestReviewRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
         assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void modifyReviewTest() {
          boolean noErrorsFound = true;
        try {
            String body = "this is a modified review"+ new Date().getTime();
            int stars = 2; // not 3
            String requestUrl = String.format(MODIFY_REVIEW_URL, TEST_REVIEW_ID_MODIFY, body, stars, HUCK_FINN_BOOK_ID);
            this.mockMvc.perform(put(requestUrl).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.data.id").value(TEST_REVIEW_ID_MODIFY))
                    .andExpect(jsonPath("$.data.body").value(body))
                    .andExpect(jsonPath("$.data.stars").value(stars))
                    .andExpect(jsonPath("$.data.book_id").value(HUCK_FINN_BOOK_ID))
                    .andExpect(jsonPath("$.status").value(MessageDefinitions.SUCCESS));
        } catch (Exception ex) {
            Logger.getLogger(TestReviewRestController.class.getName()).log(Level.SEVERE, null, ex);
             noErrorsFound = false;
        }
         assertTrue("verify no exceptions raised", noErrorsFound);
    }

    /*
     *  Tests the controller processes a delete Review  request properly.
     *   Verifies sucess message.
     *   Verifies the id/ket of the deleted record does not exist in the  list view.
     */
    @Test
    public void deleteReviewTest() {
          boolean noErrorsFound = true;
       

        try {
            String url = String.format(DELETE_REVIEW_URL, TEST_REVIEW_ID_DELETE);
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
            JSONObject reviewData = (JSONObject) obj.get("data");
            Integer id = (Integer) reviewData.get("id");
            assertTrue("action is delete ", action.equals(MessageDefinitions.DEL_OPERATION));
            assertTrue("status is success ", status.equals(MessageDefinitions.SUCCESS));
            assertTrue("id is for review we wanted to delete", (TEST_REVIEW_ID_DELETE == id.intValue()));
        } catch (Exception ex) {
            Logger.getLogger(TestReviewRestController.class.getName()).log(Level.SEVERE, null, ex);
            noErrorsFound = false;
        }
         assertTrue("verify no exceptions raised", noErrorsFound);

        // get the new count after the delete
         noErrorsFound = true;
        try {
            ResultActions requestResult = this.mockMvc.perform(get(LIST_URL)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON));

            String content = requestResult.andReturn().getResponse().getContentAsString();
            JsonModel model = JsonModel.create(content);
            Object obj = model.get("$.reviews");
            assertTrue("object is a json array", (obj instanceof JSONArray));
            int num_reviews_after_delete = ((JSONArray) obj).size();
            assertTrue("verify we have record", num_reviews_after_delete > 0);
            // finally verify deleted id does not exist in the list of records
            JSONArray reviewList = (JSONArray) obj;
            JSONObject reviewObj;
            boolean RECORD_FOUND = false;
            Integer review_id;
            for (int nIndex = 0; nIndex < num_reviews_after_delete; nIndex++) {
                reviewObj = (JSONObject) reviewList.get(nIndex);
                review_id = (Integer) reviewObj.get("id");
                if (review_id.intValue() == TEST_REVIEW_ID_DELETE) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify deleted record id not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestReviewRestController.class.getName()).log(Level.SEVERE, null, ex);
             noErrorsFound = false;
        }
          assertTrue("verify no exceptions raised", noErrorsFound);
    }

}
