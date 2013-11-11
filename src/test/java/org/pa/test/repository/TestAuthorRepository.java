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
 * @author lorinpa
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

        MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);

        if (className == null) {
            className = "TestAuthorRepository";
        }
        TEST_AUTHOR_DELETE_ID = CaseGen.getInstance().createTestAuthor(new Date().getTime() + "", className);
        TEST_AUTHOR_MODIFY_ID = CaseGen.getInstance().createTestAuthor(new Date().getTime() + "", className);
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
        List<Author> list = authorRepo.findAll();
        assertNotNull("verify we have data", list);
        assertTrue("verify we have recpords", list.size() > 0);
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
            markTwain = authorRepo.findById(MARK_TWAIN_ID);
            assertNotNull("verify mark twain not null", markTwain);
            assertTrue("verify id", markTwain.getId().intValue() == MARK_TWAIN_ID);
        } catch (Exception e) {
            noErrorsFound = false;
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
    }

    @Test
    public void testAddAuthor() {
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
        assertNotNull(ed.getId());
        TEST_AUTHOR_ADD_ID = ed.getId();
    }

    @Test
    public void testDeleteAuthor() {
        boolean noErrorsFound = true;
        List<Author> authorListAfterDelete = null;
        try {
            authorRepo.delete(TEST_AUTHOR_DELETE_ID);
            authorListAfterDelete = authorRepo.findAll();
        } catch (Exception e) {
            noErrorsFound = false;
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
        boolean RECORD_FOUND = false;
        for (Author author : authorListAfterDelete) {
            if (author.getId().intValue() == TEST_AUTHOR_DELETE_ID) {
                RECORD_FOUND = true;
                break;
            }
        }
        assertFalse("verify we did not find record ", RECORD_FOUND);
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
            Author authorAfterUpdate = authorRepo.update(authorToModify.getId(), CaseGen.MARK, CaseGen.TWAIN);
        } catch (Exception ex) {
            errorsFound = true;
        }
        assertTrue("verify we raised an exception", errorsFound);
        errorsFound = false; // reset for new assertions
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
    }

    @Test
    public void testAuthorsCount() {
        assertNotNull(authorRepo.count());
    }

}
