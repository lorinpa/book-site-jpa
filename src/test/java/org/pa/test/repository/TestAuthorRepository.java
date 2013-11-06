/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.test.repository;

import org.pa.repository.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.pa.entity.Author;
import org.pa.test.category.RepositoryCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author
 * mwave
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Category(RepositoryCategory.class)
public class TestAuthorRepository extends TestCase {

    @Autowired
    private AuthorsRepository authorRepo;
   
    // used to seed values
    private static String className;
    
    // used for list test
    private static int MARK_TWAIN_ID;
    
    // ADD record 
    private static int TEST_AUTHOR_ADD_ID;
    
    // DELETE record
      private static int TEST_AUTHOR_DELETE_ID;
      // used to MODIFY -- 
     private static int TEST_AUTHOR_MODIFY_ID;

    @BeforeClass
    public static void setUpClass() {

   
        MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK,CaseGen.TWAIN);
       
        if (className == null) {
            className = "TestAuthorRepository";
        }
       TEST_AUTHOR_DELETE_ID = CaseGen.getInstance().createTestAuthor( new Date().getTime()+"", className);
        TEST_AUTHOR_MODIFY_ID=  CaseGen.getInstance().createTestAuthor( new Date().getTime()+"", className);
    }

    @AfterClass
    public static void tearDownClass() {
       CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_DELETE_ID);
       CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_MODIFY_ID);
       CaseGen.getInstance().deleteTestBook(TEST_AUTHOR_ADD_ID);
    }
    
    protected void setUp() throws Exception {

    }

    @Override
    protected void tearDown() throws Exception {
    }

    @Test
    public void testSelectAuthors() {
        Long current_size= authorRepo.count();
     //   int expected_size = NUM_GENERATED_AUTHORS - 2;
        List<Author> list = authorRepo.findAll();
        assertNotNull(list);
        assertTrue(list.size() > 0);
      //  assertTrue(list.size() >= expected_size);
        assertTrue("verify list equals current repo count", list.size() == current_size.intValue());
        // now let's verify the first record Mark Twain
        Author markTwain = null;
        boolean found_record = false;
        for (Author author : list) {
            if (author.getId().intValue() == MARK_TWAIN_ID) {
                found_record = true;
                markTwain = author;
                break;
            }
        }
        assertTrue("verify we found Mark Twain", found_record);
        assertNotNull("verify mark twain not null", markTwain);
        assertTrue("the first record is Mark Twain -- id matches", markTwain.getId().intValue() == MARK_TWAIN_ID);
        assertTrue("the first record is Mark Twain -- first name matches", markTwain.getFirstName().equals(CaseGen.MARK));
        assertTrue("the first record is Mark Twain -- last name matches", markTwain.getLastName().equals(CaseGen.TWAIN));

    }
    
    @Test 
        public void testSelectAuthor() {
         boolean noErrorsFound = true;
        Author markTwain = null;
        try {    
              markTwain =  authorRepo.findById(MARK_TWAIN_ID);
              assertNotNull("verify mark twain not null", markTwain);
              assertTrue("verify id", markTwain.getId().intValue() == MARK_TWAIN_ID);
        } catch (Exception e) {
            noErrorsFound = false;
        }
          assertTrue("verify we didn't raise an exception", noErrorsFound);
    }
    

    @Test
    public void testAddAuthor() {
       // Long num_records_before_add = authorRepo.count();
        boolean noErrorsFound = true;
        Author ed = new Author();
        String suffix = new Date().getTime() + "";
        String firstName = "first" + suffix;
        String lastName = "last" + suffix;
        ed.setFirstName(firstName);
        ed.setLastName(lastName);
        try {
            authorRepo.addNew(ed);
         
        } catch (Exception ex) {
            noErrorsFound = false;
            Logger.getLogger(TestAuthorRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
   //     Long num_records_after_add = authorRepo.count();
        assertNotNull(ed.getId());
        TEST_AUTHOR_ADD_ID   = ed.getId();
      //assertTrue("verify new id is greater than our last generated test case", ed.getId().intValue() > LAST_GENERATED_AUTHOR_ID);
   //     assertTrue("record count incremented by one", num_records_after_add == (1 + num_records_before_add));

    }

    @Test
    public void testDeleteAuthor() {
        
       // Long num_records_before_delete =authorRepo.count();
        boolean noErrorsFound = true;
      //  Author joeSmoe;
        List<Author> authorListAfterDelete= null;

        try {
      //      joeSmoe = (Author) authorRepo.findOne(JOE_SMOE_ID);
            authorRepo.delete(TEST_AUTHOR_DELETE_ID);
            authorListAfterDelete= authorRepo.findAll();
        } catch (Exception e) {
            noErrorsFound = false;
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
      //  Long  num_records_after_delete = authorRepo.count();
      //  assertTrue("record count incremented by one", num_records_after_delete.longValue() == (num_records_before_delete.longValue() - 1));
        // now let's verify the results of find all does not contain the deleted author
       
       for (Author author : authorListAfterDelete) {
            assertTrue(author.getId().intValue() != TEST_AUTHOR_DELETE_ID);
        }
     
    }

    @Test
    public void testUpdateAuthor() {
        boolean noErrorsFound = true;
        try {
            Author authorToModify = authorRepo.findById(new Integer(TEST_AUTHOR_MODIFY_ID));
            String suffix = new Date().getTime() + "";
            String firstName = "first" + suffix;
            String lastName = "last" + suffix;
   
            Author authorAfterUpdate = authorRepo.update(authorToModify.getId(), firstName, lastName);
            assertTrue("verify updated record has original id", authorAfterUpdate.getId().intValue() == TEST_AUTHOR_MODIFY_ID);
            assertTrue("verify updated first name", authorAfterUpdate.getFirstName().equals(firstName));
            assertTrue("verify updated last name", authorAfterUpdate.getLastName().equals(lastName));
            
        } catch (Exception ex) {
             noErrorsFound = false;
            Logger.getLogger(TestAuthorRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
         assertTrue("verify we didn't raise an exception", noErrorsFound);
         
    }
    
    @Test 
     public void testUpdateDuplicate() {
        boolean errorsFound = false;
        try {
            Author authorToModify = authorRepo.findById(new Integer(TEST_AUTHOR_MODIFY_ID));
            Author authorAfterUpdate = authorRepo.update(authorToModify.getId(),CaseGen.MARK,CaseGen.TWAIN);
        
       } catch (Exception ex) {
             errorsFound = true;
            //Logger.getLogger(AuthorRepositoryUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
         assertTrue("verify we raised an exception", errorsFound);
         // let's select the last author and verify it is not updated to Mark Twain
         errorsFound = false; // reset for new test
         try {
            Author lastAuthor = authorRepo.findById(new Integer(TEST_AUTHOR_MODIFY_ID));
            assertFalse("verify first name is not Mark", lastAuthor.getFirstName().equals(CaseGen.MARK));
            assertFalse("verify first name is not Mark", lastAuthor.getLastName().equals(CaseGen.TWAIN));
       } catch (Exception ex) {
             errorsFound = true;
            Logger.getLogger(TestAuthorRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
          assertFalse("verify we raised no exception", errorsFound);
     }

     
      @Test 
     public void testAddDuplicate() {
        boolean errorsFound = false;
    ///    Long count_before_add_attempt = authorRepo.count();
        Author markTwain = authorRepo.findById(new Integer(MARK_TWAIN_ID));
        Author newAuthor = new Author();
        newAuthor.setFirstName(CaseGen.MARK);
        newAuthor.setLastName(CaseGen.TWAIN);
        try {
            authorRepo.addNew(newAuthor);
        } catch (Exception ex) {
            errorsFound = true; 
        }
         assertTrue("verify we raised an exception", errorsFound);
         assertTrue("author was not assigned an is", newAuthor.getId() == null);
        //  Long count_after_add_attempt = authorRepo.count();
        //  assertTrue(count_after_add_attempt.longValue() == count_before_add_attempt.longValue());
     }
     
  

    @Test
    public void testAuthorsCount() {
        assertNotNull(authorRepo.count());
    }

   
}
