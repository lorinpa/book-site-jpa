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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito.*;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.pa.matcher.NotNull;
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


import org.pa.entity.Author;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.test.category.ControllerCategory;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import org.springframework.web.context.WebApplicationContext;

/**
 *
 * @author lorinpa
 
 Note! We use our DbUtil utility to create a set of test data.

 DbUtil is run once during this test suite. DbUtil is run prior to any test
 in this suite.
 
 Note!   We should have between 2 and 4 authors for the duration of this suite.
 DbUtil creates 3 authors.  However, we have an add author test and a delete author test.
 We don't know what sequence the tests are going to run in. Therefore, if the delete author test runs first,
 the database will only have 2 authors remaining. Likewise, if the "add author" test runs first, 
  we will have 4 authors in the database. 
 
 Our delete category test deletes the 2nd authors. 

 */
@Category(ControllerCategory.class)
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextHierarchy({
    @ContextConfiguration(classes = AppConfig.class),
    @ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/app-context.xml"),
    @ContextConfiguration("file:src/main/webapp/WEB-INF/dispatcher-context.xml")})

public class TestAuthorController {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;
    
   private final String REDIRECT_TO_AUTHOR_HOME_NAME =  "redirect:list.htm";
   
   // used for add and  modify tests
    private static Integer greatestAuthorId;
    // used for List and Duplicate test
    private static Author markTwain;
    // used for MODIFY test
    private static Integer ED_SMED_ID;
    // used for DELETE tests
    private static Integer JOE_SMOE_ID;
    
       // used for list test
    private static int MARK_TWAIN_ID;
     // used to seed values
    private static String className;
     // DELETE record
    private static int TEST_AUTHOR_DELETE_ID;
      // used to MODIFY -- 
     private static int TEST_AUTHOR_MODIFY_ID;
    // used to ADD
     private static int TEST_AUTHOR_ADD_ID;

    @BeforeClass
    public static void setUpClass() {

       MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        JOE_SMOE_ID = CaseGen.getInstance().getTestAuthor(CaseGen.JOE, CaseGen.SMOE);
       if (className == null) {
            className = "AuthorCtlr";
        }
       TEST_AUTHOR_DELETE_ID = CaseGen.getInstance().createTestAuthor( new Date().getTime()+"d", className);
        TEST_AUTHOR_MODIFY_ID=  CaseGen.getInstance().createTestAuthor( new Date().getTime()+"m", className);
        
      //  greatestAuthorId = DbUtil.getInstance().getLastAuthorId();
      //  markTwain = DbUtil.getInstance().getMarkTwain();
     //  ED_SMED_ID = DbUtil.getInstance().getEdSmeId();
      // JOE_SMOE_ID = DbUtil.getInstance().getJoeSmoeId();
    }

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

    

    /*
     *  Tests that the controller dispatches a view containing 3 categories (the number of categories generated by DbUtil).
     *  Tests that one of the  categores is indeed the fiction category object.
     *  Tests our list size is 2 or greater (greater because add or delete  test might run first).  
     *   We should have between 2 and 4 categories for the duration of this suite.
     *
     *   Note! Since we are NOT redirecting to category home, the internal Spring MVC view name does not include the
     *   redirect prefix. 
     */
    @Test
    public void testListView() {
        try {        
            ResultActions requestResult
                    = this.mockMvc.perform(get("/author/list.htm"))
                            .andExpect(view().name("author/list"))
                    .andExpect(model().attributeExists("list"))
                //    .andExpect(model().attribute("list", hasItem(markTwain)))
                    .andExpect(forwardedUrl("/WEB-INF/views/author/list.jsp"));
            List<Author> list = (List<Author>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            assertTrue("list must have size of 2 or more ", (list.size() >= 2));
            // check to see if we find Mark Twain       
             boolean RECORD_FOUND = false;
            for (Author author :list) {
                if (author.getId().intValue() == MARK_TWAIN_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify the  record id was  found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*
     *   Tests the controller services the "add new author" function.
     *    Verify the view response is a 301 (redirect to category home/list).
     *    Verify the controller dispatches a model with the new author
     *    Verify the server assigned the new author an id (key) 
     *    Note! We are assuming the new id/key is going to be a number greated than existing keys values
     */
    @Test
    public void testAddAuthor() {
        try {
            NotNull nn = new NotNull();
            String firstName = className;
            String lastName = new Date().getTime() + "a";
            ResultActions andExpect = mockMvc.perform(post("/author/add").param("firstName", firstName)
                    .param("lastName", lastName)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(model().attributeExists("author"))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_AUTHOR_HOME_NAME));
            Author authorResult = (Author) andExpect.andReturn().getModelAndView().getModelMap().get("author");
            assertTrue(authorResult.getLastName().equals(lastName));
            TEST_AUTHOR_ADD_ID = authorResult.getId();
            //assertTrue(authorResult.getId() > greatestAuthorId);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     *  Tests the controller dispatches a model with the updated author. 
     *  Note! We modify the last author generated by DbUtil to avoid conflicting with other tests. 
     */
    @Test
    public void testModAuthor() {
         boolean no_errors_found = true;
        try {
         
            String firstName =  className;
            String lastName = new Date().getTime() + "m";
            ResultActions andExpect = mockMvc.perform(post("/author/edit").param("lastName", lastName)
                     . param("id", TEST_AUTHOR_MODIFY_ID + "").param("firstName", firstName)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_AUTHOR_HOME_NAME));
            Author authorResult = (Author) andExpect.andReturn().getModelAndView().getModelMap().get("author");
            assertTrue("verify firstName updated", authorResult.getFirstName().equals(firstName));
             assertTrue("verify lastName updated", authorResult.getLastName().equals(lastName));
            // Note! We need to test the int value, the Integer objects don't match?
            assertTrue("verify key/id is unchanged", authorResult.getId().intValue() == TEST_AUTHOR_MODIFY_ID);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

    /*
     * Tests the controller rejects attempt to add a duplicate author 
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testAddDuplicateAuthor() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/author/add").param("firstName", CaseGen.MARK)
                    .param("lastName", CaseGen.TWAIN)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("author/add"))
                    .andExpect(model().attributeExists("author"));
            // get the error message is nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.author");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("firstName", MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
            no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }

     /*
     * Tests the controller rejects attempt to modify an existing author so that it is a duplicate.
     * We'll try to create a second Mark Twain through edit
     *  We verify the controller dispatches a specific  error message string.
     */
    @Test
    public void testModifyDuplicateAuthor() {
        boolean no_errors_found = true;
        try {
            ResultActions requestResult = mockMvc.perform(post("/author/edit")
                    .param("id", TEST_AUTHOR_MODIFY_ID+"")
                    .param("firstName", CaseGen.MARK)
                    .param("lastName", CaseGen.TWAIN)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk())
                    .andExpect(view().name("author/edit"))
                    .andExpect(model().attributeExists("author"));
            // get the error message is nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.author");
            assertThat(bindingResult.getAllErrors(), hasItem(new ObjectError("firstName", MessageDetailDefinitions.DUPLICATE_AUTHOR_EXCEPTION)));
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
             no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }
    
    
    
    /*
    *  Tests the contoller dispatches a success response upon deleting a author.
    *  Note! Sucess is indicated by a simple redirect to the category home (list). The spring mvc result object should
    *  have an empty error list. 
    * 
    *  We render the list authors view twice. First time to get a count of authors before delete.
    *  Second time to get count of authors after the delete.  The second time should have a list who's size
    *  is exactlry one less than our original count. 
    */
    
    @Test
    public void testDelete() {
      boolean no_errors_found = true;
       try {
            ResultActions requestResult = mockMvc.perform(post("/author/delete").param("id", TEST_AUTHOR_DELETE_ID+"")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isMovedTemporarily())
                    .andExpect(view().name(REDIRECT_TO_AUTHOR_HOME_NAME))
                    .andExpect(model().attributeExists("author"));
            // get the error message list   nested in the result. 
            BindingResult bindingResult = (BindingResult) requestResult.andReturn().getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.author");
            assertTrue(bindingResult.getAllErrors().size() == 0);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
              no_errors_found = false;
        }
         assertTrue("verify no exceptions thrown", no_errors_found);
         no_errors_found = true;
         try {          
            ResultActions requestResult
                    = this.mockMvc.perform(get("/author/list.htm"))
                            .andExpect(view().name("author/list"))
                    .andExpect(model().attributeExists("list"))
           //         .andExpect(model().attribute("list", hasItem(markTwain)))
                    .andExpect(forwardedUrl("/WEB-INF/views/author/list.jsp"));
            List<Author> list = (List<Author>) requestResult.andReturn().getModelAndView().getModelMap().get("list");
            boolean RECORD_FOUND = false;
            for (Author author :list) {
                if (author.getId().intValue() == TEST_AUTHOR_DELETE_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertFalse("verify the deleted record id was not found", RECORD_FOUND);
        } catch (Exception ex) {
            Logger.getLogger(TestAuthorController.class.getName()).log(Level.SEVERE, null, ex);
              no_errors_found = false;
        }
        assertTrue("verify no exceptions thrown", no_errors_found);
    }
    
    
}
