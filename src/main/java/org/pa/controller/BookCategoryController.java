package org.pa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.repository.BookCategoriesRepository;
import org.pa.repository.BooksRepository;
import org.pa.repository.CategoriesRepository;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookCategoryValidator;
import org.pa.validation.BookValidator;
import org.pa.validation.CategoryValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author  lorinpa
 */
@Controller
public class BookCategoryController {

    private BookCategoriesRepository bookCatRepo;
    private BooksRepository bookRepo;
    private CategoriesRepository categoryRepo;
    private final static String ADD_URL = "book-cat/add";
    private final static String MOD_URL = "book-cat/edit";
    private final static String DEL_URL = "book-cat/delete";
    private final static String LIST_URL = "book-cat/list";
    private final static String REDIRECT_HOME_URL = "redirect:list.htm";

    @Inject
    @Qualifier("BookCategoryRepository")
    public void setBookCatRepo(BookCategoriesRepository bookCatRepo) {
        this.bookCatRepo = bookCatRepo;
    }

    @Inject
    @Qualifier("bookRepository")
    public void setBookRepo(BooksRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    @Inject
    @Qualifier("categoryRepository")
    public void setCategoryRepo(CategoriesRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    private Map<Integer, String> getBookList() {
        List<Book> bookList = bookRepo.findAll();
        Map<Integer, String> books = new HashMap();
        if (bookList != null) {
            for (Book book : bookList) {
                books.put(book.getId(), book.getTitle());
            }
        }
        return books;
    }

    private Map<Integer, String> getCategoryList() {
        List<Category> categoryList = categoryRepo.findAll();
        Map<Integer, String> categories = new HashMap();
        if (categoryList != null) {
            for (Category category : categoryList) {
                categories.put(category.getId(), category.getTitle());
            }
        }
        return categories;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.GET)
    public ModelAndView displayEdit(@RequestParam("id") Integer id) throws Exception {
        BookCategory bookCat = bookCatRepo.findById(id);
        ModelAndView mav = new ModelAndView(MOD_URL);
        mav.addObject("bookCat", bookCat);
        mav.addObject("books", getBookList());
        mav.addObject("categories", getCategoryList());
        return mav;
    }
    
    
     private ModelAndView redisplayEdit(BookCategory bookCat,String errorMessage) {
         ModelAndView mav = new ModelAndView(MOD_URL);
          mav.addObject("message",errorMessage);
          mav.addObject("bookCat", bookCat);
          mav.addObject("books", getBookList());
          mav.addObject("categories", getCategoryList());
          return mav;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.POST)
    public ModelAndView submitEditForm(@ModelAttribute BookCategory bookCat, BindingResult result, Model model) {
        try {
            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            CategoryValidator categoryValidator = new CategoryValidator();
            BookCategoryValidator bookCategoryValidator = new BookCategoryValidator(bookValidator, categoryValidator);
            Book book = bookRepo.findById(bookCat.getBookId().getId());
            Category category = categoryRepo.findById(bookCat.getCategoryId().getId());
            bookCat.setBookId(book);
            bookCat.setCategoryId(category);
            bookCategoryValidator.validate(bookCat, result);
            if (result.hasErrors()) {
                return redisplayEdit(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }
            bookCatRepo.update(bookCat.getId(), bookCat.getBookId(), bookCat.getCategoryId());
            return list();
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
                result.addError(oe);
                 return redisplayEdit(bookCat, MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
            } else {
                ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
                result.addError(oe);
                return redisplayEdit(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }
            
        }  catch(DataIntegrityViolationException de) {
              ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
               result.addError(oe);
               return redisplayEdit(bookCat, MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
        } catch (Exception ex) {
            Logger.getLogger(BookCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return redisplayEdit(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
        }
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.GET)
    public ModelAndView displayAdd() throws Exception {
        BookCategory bookCat = new BookCategory();
        ModelAndView mav = new ModelAndView(ADD_URL);
        mav.addObject("bookCat", bookCat);
        mav.addObject("books", getBookList());
        mav.addObject("categories", getCategoryList());
        return mav;
    }

    private ModelAndView redisplayAdd(BookCategory bookCat,String errorMessage) {
         ModelAndView mav = new ModelAndView(ADD_URL);
          mav.addObject("message",errorMessage);
          mav.addObject("bookCat", bookCat);
          mav.addObject("books", getBookList());
          mav.addObject("categories", getCategoryList());
          return mav;
    }
    
    @RequestMapping(value = ADD_URL, method = RequestMethod.POST)
    public ModelAndView submitAdd(@ModelAttribute BookCategory bookCat, BindingResult result, Model model) {
       //ModelAndView mav = new ModelAndView(ADD_URL);
        try {
            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            CategoryValidator categoryValidator = new CategoryValidator();
            BookCategoryValidator bookCategoryValidator = new BookCategoryValidator(bookValidator, categoryValidator);
            Book book = bookRepo.findById(bookCat.getBookId().getId());
            Category category = categoryRepo.findById(bookCat.getCategoryId().getId());
            bookCat.setBookId(book);
            bookCat.setCategoryId(category);
            bookCategoryValidator.validate(bookCat, result);
            if (result.hasErrors()) {      
                return redisplayAdd(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }
            bookCatRepo.addNew(bookCat);
            return list();
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
                result.addError(oe);
                 return redisplayAdd(bookCat, MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
            } else {
                ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
                result.addError(oe);
                return redisplayAdd(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }   
           
        } catch (Exception ex) {
            Logger.getLogger(BookCategoryController.class.getName()).log(Level.SEVERE, null, ex);
           return redisplayAdd(bookCat, MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
        }
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.GET)
    public ModelAndView displayDeleteAuthor(@RequestParam("id") Integer id) throws Exception {
        BookCategory bookCat = bookCatRepo.findById(id);
        ModelAndView mav = new ModelAndView(DEL_URL);
        mav.addObject("bookCat", bookCat);
        return mav;
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.POST)
    public String submitDeleteForm(@ModelAttribute BookCategory bookCat, BindingResult result, SessionStatus status) {
        try {
            bookCatRepo.delete(bookCat.getId());
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            ObjectError oe = new ObjectError("bookCat", MessageDetailDefinitions.DELETE_BOOK_CATEGORY_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        } catch (Exception ex) {
            Logger.getLogger(BookCategoryController.class.getName()).log(Level.SEVERE, null, ex);
            ObjectError oe = new ObjectError("bookCat",MessageDetailDefinitions.DELETE_BOOK_CATEGORY_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        }
    }

    @RequestMapping(value = LIST_URL, method = RequestMethod.GET)
    public ModelAndView list() throws Exception {
        List<BookCategory> list;
        list = bookCatRepo.findAll();
        ModelAndView mav = new ModelAndView(LIST_URL);
        mav.addObject("list", list);
        return mav;
    }
}
