package org.pa.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import org.hibernate.exception.ConstraintViolationException;
import org.pa.entity.Author;
import org.pa.entity.Book;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.repository.AuthorsRepository;
import org.pa.repository.BooksRepository;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookValidator;
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
 * @author lorinpa
 *
 *  This is a Spring MVC controller responsible for dispatching to traditional JSP views. 
 *  This controller performs CRUD operations for the "book" entity (model).
 */
@Controller
public class BookController {

    private final static String FULL_NAME_TEMPLATE = "%s, %s";
    private final static String ADD_URL = "book/add";
    private final static String MOD_URL = "book/edit";
    private final static String DEL_URL = "book/delete";
    private final static String LIST_URL = "book/list";
    private final static String REDIRECT_HOME_URL = "redirect:list.htm";
    private BooksRepository booksRepository;
    private AuthorsRepository authorsRepository;

    @Inject
    @Qualifier("bookRepository")
    public void setBooksRepository(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    @Inject
    @Qualifier("authorRepository")
    public void setAuthorsRepository(AuthorsRepository authorsRepository) {
        this.authorsRepository = authorsRepository;
    }

    private String authorName(Author author) {
        return String.format(FULL_NAME_TEMPLATE, author.getLastName(), author.getFirstName());
    }

    private Map<Integer, String> getAuthorList() {
        List<Author> authorList = authorsRepository.findAll();
        Map<Integer, String> books = new HashMap();
        if (authorList != null) {
            for (Author author : authorList) {
                books.put(author.getId(), authorName(author));
            }
        }
        return books;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.GET)
    public ModelAndView displayEdit(@RequestParam("id") Integer id) throws Exception {
        Book book = booksRepository.findById(id);
        ModelAndView mav = new ModelAndView(MOD_URL);
        mav.addObject("book", book);
        mav.addObject("authors", getAuthorList());
        return mav;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.POST)
    public ModelAndView submitEditForm(@ModelAttribute Book book, BindingResult result, Model model) {
        ModelAndView mav = new ModelAndView(MOD_URL);
        try {
            Author author = authorsRepository.findById(book.getAuthorId().getId());
            if (author != null) {
                book.setAuthorId(author);
            }
            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            bookValidator.validate(book, result);
            if (result.hasErrors()) {
                mav.addObject("authors", this.getAuthorList());
                return mav;
            }
            booksRepository.update(book.getId(), book.getTitle(), author);
            return list();      
        } catch (PersistenceException pe) {
            mav.addObject("authors", this.getAuthorList());
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION);
                result.addError(oe);
            } else {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.SAVE_BOOK_EXCEPTION);
                result.addError(oe);
            }        
            return mav;
        } catch (DataIntegrityViolationException de) {
            ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION);
            result.addError(oe);
            mav.addObject("authors", this.getAuthorList());
            return mav;
        } catch (Exception ex) {
            Logger.getLogger(BookController.class.getName()).log(Level.SEVERE, null, ex);
            mav.addObject("authors", this.getAuthorList());
            return mav;
        }
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.GET)
    public ModelAndView displayAddBook() throws Exception {
        Book book = new Book();
        ModelAndView mav = new ModelAndView(ADD_URL);
        mav.addObject("book", book);
        mav.addObject("authors", this.getAuthorList());
        return mav;
    }

    /*
     *  We need to return the ModelAndView in order to repaint the dependent author list
     */
    @RequestMapping(value = ADD_URL, method = RequestMethod.POST)
    public ModelAndView submitAddBookForm(@ModelAttribute Book book, BindingResult result, SessionStatus status) {
        ModelAndView mav = new ModelAndView(ADD_URL);
        try {
            Author author = authorsRepository.findById(book.getAuthorId().getId());
            if (author != null) {
                book.setAuthorId(author);
            }

            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            bookValidator.validate(book, result);

            if (result.hasErrors()) {
                mav.addObject("authors", getAuthorList());
                return mav;
            }
            booksRepository.addNew(book);
            return list();
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION);
                result.addError(oe);
            } else {
                ObjectError oe = new ObjectError("title", MessageDetailDefinitions.SAVE_BOOK_EXCEPTION);
                result.addError(oe);
            }
            mav.addObject("authors", this.getAuthorList());
            return mav;
        } catch (Exception ex) {
            Logger.getLogger(BookController.class.getName()).log(Level.SEVERE, null, ex);
            return mav;
        }
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.GET)
    public ModelAndView displayDeleteCateogory(@RequestParam("id") Integer id) throws Exception {
        Book book = booksRepository.findById(id);
        ModelAndView mav = new ModelAndView(DEL_URL);
        mav.addObject("authorName", authorName(book.getAuthorId()));
        mav.addObject("book", book);
        return mav;
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.POST)
    public String submitDeleteForm(@ModelAttribute Book book, BindingResult result, SessionStatus status) {

        try {
            booksRepository.delete(book);
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DELETE_BOOK_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        } catch (Exception ex) {
            Logger.getLogger(BookController.class.getName()).log(Level.SEVERE, null, ex);
            ObjectError oe = new ObjectError("title", MessageDetailDefinitions.DELETE_BOOK_EXCEPTION);
            result.addError(oe);
            return DEL_URL;
        }
    }

    @RequestMapping(value = {LIST_URL, "index.jsp"}, method = RequestMethod.GET)
    public ModelAndView list() throws Exception {
        List<Book> bookList;
        bookList = booksRepository.findAll();
        ModelAndView mav = new ModelAndView(LIST_URL);
        mav.addObject("list", bookList);
        return mav;
    }
}
