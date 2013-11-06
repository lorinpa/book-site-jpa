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
import junit.framework.TestCase;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.pa.AppConfig;
import org.pa.dbutil.CaseGen;
import org.pa.entity.Author;
import org.pa.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author
 * lorinpa
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AppConfig.class)
public class TestBookRepository extends TestCase {

    @Autowired
    private BooksRepository bookRepo;

    @Autowired
    private AuthorsRepository authorRepo;
    
    private static String className = "TestBookRepository";
    
    private static int HUCKLE_BERRY_FINN_BOOK_ID;
  
    private static int MARK_TWAIN_ID;
    
    // used for MODIFY test we need the author's id
    private static int JOE_SMOE_ID;
    
    // used for MODIFY
    private static int TEST_BOOK_ID_MODIFY;
     // used for DELETE test 
    private static int TEST_BOOK_ID_DELETE;
    // used for ADD test
    private static int TEST_BOOK_ID_ADD;
    
    public TestBookRepository() {
       
    }

    @BeforeClass
    public static void setUpClass() {
 
          MARK_TWAIN_ID = CaseGen.getInstance().getTestAuthor(CaseGen.MARK,CaseGen.TWAIN);
        // used for LIST test 
        
        HUCKLE_BERRY_FINN_BOOK_ID = CaseGen.getInstance().getTestBook(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, MARK_TWAIN_ID);
         
         // used for MODIFY tests
        JOE_SMOE_ID = CaseGen.getInstance().getTestAuthor(CaseGen.JOE, CaseGen.SMOE);
        if (className == null) {
            className = "TestBookRepository";
        }
        TEST_BOOK_ID_MODIFY = CaseGen.getInstance().createTestBook(className+ new Date().getTime(),JOE_SMOE_ID);
       TEST_BOOK_ID_DELETE= CaseGen.getInstance().createTestBook(className+ new Date().getTime(),JOE_SMOE_ID);
    }

    @AfterClass
    public static void tearDownClass() {
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_MODIFY);
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_DELETE);
        CaseGen.getInstance().deleteTestBook(TEST_BOOK_ID_ADD);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAddBook() {
        boolean errorFound = false;
    //    Long num_records_before_add = bookRepo.count();
        String title = new Date().getTime() + "";
        Book book = new Book();
        try {
            book.setTitle(title);
            Author author = authorRepo.findById(new Integer(MARK_TWAIN_ID));
            book.setAuthorId(author);
            bookRepo.addNew(book);

        } catch (Exception ex) {
            errorFound = true;
            Logger.getLogger(TestBookRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
     //   Long num_records_after_add = bookRepo.count();
        assertFalse("verify we did not raise and error", errorFound);
        assertNotNull(book.getId());
        TEST_BOOK_ID_ADD = book.getId();
     //   assertTrue("new book is greater than the generate test cases", book.getId().intValue() > LAST_GENERATE_BOOK_ID);
      //  assertTrue("record count incremented by one", num_records_after_add == (1 + num_records_before_add));

    }

    /*
     *  Tests the book repository prevents us from adding  a duplicated book. Duplicate book is where the author is the same, the
     *  book title is the same. We can't have to books by Mark Twain titled Huckleberry Finn. We could have 2 books titled Huckleberry
     *  Finn as long as the authors were different.
     */
    @Test
    public void testAddDuplicateBook() {
        boolean errorFound = false;
        Long num_records_before_add_attempt = bookRepo.count();

        Book book = new Book();
        try {
            book.setTitle(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE);
            Author author = authorRepo.findById(new Integer(MARK_TWAIN_ID));
            book.setAuthorId(author);
            bookRepo.addNew(book);
        } catch (Exception ex) {
            errorFound = true;
            assertTrue("verify the exception cause is a constrain violation", ex.getCause() instanceof ConstraintViolationException);
        }
        Long num_records_after_add_attempt = bookRepo.count();
        assertTrue("verify we an exception was raised", errorFound);
        assertNull("verify an id was not assigned", book.getId());
        assertTrue("verofy record count is the same", num_records_after_add_attempt == num_records_before_add_attempt);

    }

    /*
     * Tests the books repository allows us to change a books title and/or author.
     *  Verifes we can change Tom Sawyer to a book written by Joe Smoe with a title of current time
     */
    @Test
    public void testModifyBook() {
      
        boolean errorFound = false;
        String title = new Date().getTime() + className;
        Book updatedBook;
        Author joeSmoe;
        int author_id;
        try {
         //   joeSmoe = authorRepo.findById(new Integer(JOE_SMOE_ID));
           Book book = bookRepo.findById(TEST_BOOK_ID_MODIFY);
            author_id = book.getAuthorId().getId();
            updatedBook = bookRepo.update(TEST_BOOK_ID_MODIFY, title, book.getAuthorId());
         //  currentBookImage = bookRepo.update(TEST_BOOK_ID_MODIFY, title, joeSmoe);
            assertEquals("verify book has new title", updatedBook.getTitle(), title);
            assertTrue("verify the book maintains the original id", updatedBook.getId().intValue() == TEST_BOOK_ID_MODIFY);
        //    assertTrue("verify the book is now written by Joe Smoe", currentBookImage.getAuthorId().getId().intValue() == author_id);
        } catch (Exception e) {
            errorFound = true;
            Logger.getLogger(TestBookRepository.class.getName()).log(Level.SEVERE, null, e);
        }

        assertFalse("verify we did not raise an exception", errorFound);
    }

    /*
     * Test the book repository prevents us from modifying a book in to a duplicate. A books is a duplicate if both 
     *  the title and author are the same.
     *   We'll attempt to change Joe Smoe's book to Huckleberry Finn, by Mark Twain
     */
    @Test
    public void testModifyDuplicate() {
        boolean errorFound = false;
        Book currentBookImage = null;
        Author markTwain;
        try {
            currentBookImage = bookRepo.findById(new Integer(TEST_BOOK_ID_MODIFY));
            markTwain = authorRepo.findById(new Integer(MARK_TWAIN_ID));
            currentBookImage = bookRepo.update(TEST_BOOK_ID_MODIFY, CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE, markTwain);
        } catch (Exception e) {
            errorFound = true;
            assertTrue("verify the exception cause is a constrain violation", e.getCause() instanceof ConstraintViolationException);
        }
        assertTrue("verify we raised an exception", errorFound);
        assertFalse("verify book title is not Huckleberry Finn", currentBookImage.getTitle().equals(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE));
        assertTrue("verify the book is still written by Joe Smoe", currentBookImage.getAuthorId().getId().intValue() == JOE_SMOE_ID);
    }

    /*
     * Tests the book repository return the expected set of books.
     *  We verify the record count and the first record values. 
     */
    @Test
    public void testFindAll() {
        boolean errorFound = false;
        List<Book> list = null;
        try {
            list = bookRepo.findAll();
            assertTrue("verify number of book", (list.size() > 0));
            assertTrue("verify the first list element is a book", list.get(0) instanceof Book);
            int first_book_id = list.get(0).getId().intValue();
            assertTrue("verify the first book id is Huckleberry Finn", first_book_id == HUCKLE_BERRY_FINN_BOOK_ID);
        } catch (Exception e) {
            errorFound = true;
        }
        assertFalse("verify we did not raise an exception", errorFound);
    }

    /*
     *  Tests the book repository deletes the book we specify
     */
    @Test
    public void testDeleteBook() {
        boolean errorFound = false;
       // Long num_books_before_delete = bookRepo.count();
        try {
            bookRepo.delete(new Integer(TEST_BOOK_ID_DELETE));
         //   Long num_books_after_delete = bookRepo.count();
   //         assertTrue("verify the book count is reduced by one", num_books_after_delete == (num_books_before_delete - 1));
        } catch (Exception e) {
            errorFound = true;
        }
        assertFalse("verify we did not raise an exception", errorFound);
        // no lets loop through the book list and verify we can not find the ED Smed book 
        errorFound = false;
        List<Book> list = null;
        boolean id_found = false;
        try {
            list = bookRepo.findAll();
            for (Book book : list) {
                if (book.getId().intValue() == TEST_BOOK_ID_DELETE) {
                    id_found = true;
                    break;
                }
            }
            assertFalse("verify we did not find the deleted book id", id_found);
        } catch (Exception e) {
            errorFound = true;
        }
        assertFalse("verify we did not raise an exception", errorFound);
    }
    
    /*
    * Test that findById returns the generated test case values
    */

    @Test
    public void testFindBookById() {
        
        Book huckleBerryFinn = bookRepo.findById(HUCKLE_BERRY_FINN_BOOK_ID);
        assertTrue("verify title", huckleBerryFinn.getTitle().equals(CaseGen.HUCKLE_BERRY_FINN_BOOK_TITLE));
        assertTrue("verify author id", huckleBerryFinn.getAuthorId().getId().intValue() == MARK_TWAIN_ID);
    
    }

   
}
