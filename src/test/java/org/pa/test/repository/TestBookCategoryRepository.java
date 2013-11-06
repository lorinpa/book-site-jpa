package org.pa.test.repository;

import java.util.Date;
import org.pa.repository.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;

import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author lorinpa public-action.org
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class TestBookCategoryRepository extends TestCase {

    @Autowired
    private BookCategoriesRepository bookCatRepo;

    @Autowired
    private CategoriesRepository catRepo;

    @Autowired
    private BooksRepository bookRepo;

    // DELETE record
    private static int TEST_BOOK_CAT_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_BOOK_CAT_MODIFY_ID;
    // used to ADD
    private static int TEST_BOOK_CAT_ADD_ID;
    // used for LISt
    private static int HUCK_FINN_HUMOR_BOOK_CAT_ID;
    /*** NOTE WE NEED TO CREATE TEMP CATEGORIES FOR OUT TEST **/
    // DELETE record
    private static int TEST_CATEGORY_DELETE_ID;
    // used to MODIFY -- 
    private static int TEST_CATEGORY_MODIFY_ID;
    // used to ADD
    private static int TEST_CATEGORY_ADD_ID;
    // more temp value
    private static int HUCK_FINN_BOOK_ID;
    private static int FICTION_CATEGORY_ID;

    public TestBookCategoryRepository() {
    }

    @BeforeClass
    public static void setUpClass() {
       

        // temp categories   
        TEST_CATEGORY_MODIFY_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkm");
        TEST_CATEGORY_DELETE_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bkd");
        TEST_CATEGORY_ADD_ID = CaseGen.getInstance().createTestCategory(new Date().getTime() + "bka");

        // lets use Huckleberry Finn as our book
        int mark_twain_author_id = CaseGen.getInstance().getTestAuthor(CaseGen.MARK, CaseGen.TWAIN);
        HUCK_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, mark_twain_author_id);
        // book categories
        TEST_BOOK_CAT_DELETE_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_DELETE_ID);
        TEST_BOOK_CAT_MODIFY_ID = CaseGen.getInstance().createTestBookCategory(HUCK_FINN_BOOK_ID, TEST_CATEGORY_MODIFY_ID);

        FICTION_CATEGORY_ID = CaseGen.getInstance().getTestCategory(CaseGen.CATEGORY_FICTION_TITLE);
        HUCK_FINN_HUMOR_BOOK_CAT_ID = CaseGen.getInstance().getTestBookCategory(HUCK_FINN_BOOK_ID, FICTION_CATEGORY_ID);

    }

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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindAll() {

        boolean noErrorsFound = true;
        try {
            List<BookCategory> list = bookCatRepo.findAll();
            assertNotNull("verify we got results", list);
            assertTrue("verify we go at least min records", list.size() > 0);
            boolean RECORD_FOUND = false;
            for (BookCategory bookCat : list) {
                if (bookCat.getId().intValue() == HUCK_FINN_HUMOR_BOOK_CAT_ID) {
                    RECORD_FOUND = true;
                    break;
                }
            }
            assertTrue("verify we found record", RECORD_FOUND);
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestBookCategoryRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void testAddBookCategory() {
        boolean noErrorsFound = true;
        try {
            BookCategory newBookCategory = new BookCategory();
            // use temp category value
            Category category = catRepo.findById(new Integer(TEST_CATEGORY_ADD_ID));
            Book book = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
            newBookCategory.setBookId(book);
            newBookCategory.setCategoryId(category);
            catRepo.addNew(newBookCategory);
            assertNotNull(newBookCategory.getId());
            TEST_BOOK_CAT_ADD_ID = newBookCategory.getId();
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestBookCategoryRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify no exceptions raised", noErrorsFound);
    }

    @Test
    public void testDeleteBookCategory() {
       
        boolean noErrorsFound = true;
        Category humorCat;
        List<BookCategory> listAfterDelete = null;
        try {
            bookCatRepo.delete(new Integer(TEST_BOOK_CAT_DELETE_ID));
            listAfterDelete = bookCatRepo.findAll();
        } catch (Exception e) {
            noErrorsFound = false;
            Logger.getLogger(TestBookCategoryRepository.class.getName()).log(Level.SEVERE, null, e);
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
       
        // now let's verify the results of find all does not contain the deleted author 
        assertTrue("verify we have records", listAfterDelete.size() > 0);
        boolean RECORD_FOUND = false;
        for (BookCategory bookCat : listAfterDelete) {
            if (bookCat.getId().intValue() == TEST_BOOK_CAT_DELETE_ID ) {
                RECORD_FOUND = true;
                break;
            }
        }
        assertFalse("verify we did not find record", RECORD_FOUND);
    }

    @Test
    public void testUpdateBookCategory() {
        boolean noErrorsFound = true;
        try {
            BookCategory bookCatToModify = bookCatRepo.findById(new Integer(TEST_BOOK_CAT_MODIFY_ID));
            Book book = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
            // use temp category value
            Category category = catRepo.findById(new Integer(TEST_CATEGORY_MODIFY_ID));
            BookCategory bookCatAfterUpdate = bookCatRepo.update(bookCatToModify.getId(), book, category);
            assertTrue("verify updated record has original id", bookCatAfterUpdate.getId().intValue() == TEST_BOOK_CAT_MODIFY_ID);
            assertTrue("verify updated book", bookCatAfterUpdate.getBookId() == book);
            assertTrue("verify updated category", bookCatAfterUpdate.getCategoryId() == category);

        } catch (Exception ex) {
            noErrorsFound = false;
            Logger.getLogger(TestBookCategoryRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertTrue("verify we didn't raise an exception", noErrorsFound);
    }

    @Test
    public void addDuplicateBookCateogryTest() {
        boolean errorsFound = false;
        BookCategory newBookCat = new BookCategory();
        Book book = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
        Category category = catRepo.findById(new Integer(FICTION_CATEGORY_ID));
        try {
            newBookCat.setBookId(book);
            newBookCat.setCategoryId(category);
            bookCatRepo.addNew(newBookCat);
        } catch (Exception ex) {
            errorsFound = true;
        }
        assertTrue("verify we raised an exception", errorsFound);
        assertTrue("book category  was not assigned an id", newBookCat.getId() == null);
    }
    

    @Test
    public void updateDuplicateBookCateogryTest() {
        boolean errorsFound = false;
        BookCategory updateBookCat = null;
        Book book = bookRepo.findById(new Integer(HUCK_FINN_BOOK_ID));
        Category category = catRepo.findById(new Integer(FICTION_CATEGORY_ID));
        try {
            updateBookCat = bookCatRepo.update(new Integer(TEST_BOOK_CAT_MODIFY_ID), book, category);
        } catch (Exception ex) {
            errorsFound = true;
        }
        BookCategory testBookCat = bookCatRepo.findById(new Integer(TEST_BOOK_CAT_MODIFY_ID));
        assertTrue("verify we raised an exception", errorsFound);
        // test orig book value
        assertTrue("book was not updated", testBookCat.getBookId().getId().intValue() == HUCK_FINN_BOOK_ID);
        // test new category not applied
        assertTrue("category was not updated", testBookCat.getCategoryId().getId().intValue() != FICTION_CATEGORY_ID);
    }

    @Test
    public void testBookCategoriesCount() {
       
        assertNotNull(bookCatRepo.count());
        assertTrue("verify count is at least miminum generated", bookCatRepo.count() >= 0);
    }

}
