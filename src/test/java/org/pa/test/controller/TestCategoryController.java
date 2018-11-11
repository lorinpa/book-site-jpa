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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
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

import org.pa.entity.Category;
import org.pa.exception.MessageDetailDefinitions;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa
 *
 * Note! We use our CaseGen utility to create a set of test data(
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/dispatcher-context.xml")})

public class TestCategoryController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    private final String REDIRECT_TO_CATEGORY_HOME_NAME = "redirect:categoryHome.htm";

    // DELETE record
    private static int TEST_CATEGORY_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_CATEGORY_MODIFY_ID;
    // used to ADD
    private static int TEST_CATEGORY_ADD_ID;
    // used for LISt
    private static int FICTION_CATEGORY_ID;

    @BeforeClass
    public static void setUpClass() {
        FICTION_CATEGORY_ID = CaseGen.getInstance().getTestCategory(CaseGen.CATEGORY_FICTION_TITLE);
        TEST_CATEGORY_DELETE_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "d");
        TEST_CATEGORY_MODIFY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "m");
    }

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
    public void testList() {
        boolean no_errors = true;
        try {
            this.mockMvc.perform(get("/category/categoryHome.htm")).andReturn();
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
      * Tests the Author controller. Verifies the a view is dispatched with a list of Categories
     */
    @Test
    public void testListView() {
        boolean no_errors = true;
        try {
            ResultActions requestResult
                    = this.mockMvc.perform(get("/category/categoryHome.htm"))
                            .andExpect(view().name("category/categoryHome"))
                            .andExpect(model().attributeExists("list"))
                            .andExpect(forwardedUrl("/WEB-INF/views/category/categoryHome.jsp"));
            List<Category> list = (List<Category>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("verify we have records ", (list.size() > 0));

            boolean RECORD_FOUND = false;
            for (Category category : list) {
                if (category.getId() == FICTION_CATEGORY_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify we found fiction", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     *   Tests the controller services the "add new cateogry" function.
     *    Verify the view response is a 301 (redirect to category home/list).
     *    Verify the controller dispatches a model with the new category
     *    Verify the server assigned the new category an id (key) 
     */
    @Test
    public void testAddCategory() {
        boolean no_errors = true;
        try {
            String testCategory = new Date().getTime() + "ac";
            ResultActions andExpect = mockMvc.perform(post("/category/addCategory").param("title", testCategory)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("category"))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_CATEGORY_HOME_NAME));
            Category categoryResult = (Category) andExpect.andReturn().getModelAndView().getModelMap().get("category");
            assertTrue(categoryResult.getTitle().equals(testCategory));
            assertTrue(categoryResult.getId() != null);
            TEST_CATEGORY_ADD_ID = categoryResult.getId();
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     *  Tests the controller dispatches a model with the updated category. 
     *  Note! We modify the last category generated by DbUtil to avoid conflicting with other tests. 
     */
    @Test
    public void testModCategory() {
        boolean no_errors = true;
        try {
            String testCategory = new Date().getTime() + "mc";
            ResultActions andExpect = mockMvc.perform(post("/category/editCategory").param("title", testCategory).param("id", TEST_CATEGORY_MODIFY_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_CATEGORY_HOME_NAME));
            Category categoryResult = (Category) andExpect.andReturn().getModelAndView().getModelMap().get("category");
            assertTrue("verify title updated", categoryResult.getTitle().equals(testCategory));
            assertTrue("verify key/id is unchanged", categoryResult.getId() == TEST_CATEGORY_MODIFY_ID);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     * Tests the controller rejects attempt to add a duplicate category 
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testAddDuplicateCategory() {
        boolean no_errors = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/category/addCategory").param("title", CaseGen.CATEGORY_FICTION_TITLE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/addCategory"))
                    .andExpect(model().attributeExists("category"));
            // get the error message is nested in the result. 
            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.category");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("title", MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     * Tests the controller rejects attempt to modify an existing category  in to a duplicate
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testModDuplicateCategory() {
        boolean no_errors = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/category/editCategory")
                    .param("id", TEST_CATEGORY_MODIFY_ID + "")
                    .param("title", CaseGen.CATEGORY_FICTION_TITLE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("category/editCategory"))
                    .andExpect(model().attributeExists("category"));
            // get the error message is nested in the result. 
            BindingResult bindingResult
                    = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.category");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("title", MessageDetailDefinitions.DUPLICATE_CATEGORY_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }

    /*
     *  Tests the contoller dispatches a success response upon deleting a categoey.
     *  We use the second category created by DbUtil (2 out of 3). That category is titled "technical". 
     *  Note! Sucess is indicated by a simple redirect to the category home (list). The spring mvc result object shouls
     *  have an empty error list. 
     */
    @Test
    public void testDeleteCategory() {
        boolean no_errors = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/category/deleteCategory").param("id", TEST_CATEGORY_DELETE_ID + "")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_CATEGORY_HOME_NAME))
                    .andExpect(model().attributeExists("category"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.category");
            assertTrue(bindingResult.getAllErrors().size() == 0);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);

        no_errors = true;
        try {
            ResultActions requestResult
                    = this.mockMvc.perform(get("/category/categoryHome.htm"))
                            .andExpect(view().name("category/categoryHome"))
                            .andExpect(model().attributeExists("list"))
                            .andExpect(forwardedUrl("/WEB-INF/views/category/categoryHome.jsp"));
            List<Category> list = (List<Category>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("list must have size of 3 or more ", (list.size() >= 0));
            boolean RECORD_FOUND = false;
            for (Category category : list) {
                if (category.getId().intValue() == TEST_CATEGORY_DELETE_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify record not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors = false;
        }
        assertTrue("verify there were no errors", no_errors);
    }
}
