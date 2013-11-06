/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.rest.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.hibernate.exception.ConstraintViolationException;
import org.pa.dto.BookExport;
import org.pa.entity.Author;
import org.pa.entity.Book;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.exception.RestException;
import org.pa.repository.AuthorsRepository;
import org.pa.repository.BooksRepository;
import org.pa.rest.message.MessageDefinitions;
import org.pa.rest.message.SuccessMessage;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookValidator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
@Controller
public class BookRest {

    private BooksRepository booksRepository;
    private AuthorsRepository authorsRepository;
    public final static String EXPORT_ALL_URL = "/export/book/all";
    public final static String FIND_BOOK_URL = "/export/book/find/{slug}";
    public final static String SET_BOOK_URL = "/modify/book/set/{slug}/{authorId}/{title}";
    public final static String ADD_BOOK_URL = "/add/book/set/{authorId}/{title}";
    public final static String DEL_BOOK_URL = "/delete/book/{id}";

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

    @RequestMapping(value = EXPORT_ALL_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Map<String, List> exportToJson(Model model) throws Exception {
        Map<String, List> dto = new HashMap<>();
        List<BookExport> list = booksRepository.exportAll();
        dto.put("books", list);
        return dto;
    }

    @RequestMapping(value = FIND_BOOK_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Book getBook(@PathVariable Integer slug, Model model) {
        return booksRepository.findById(slug);
    }

    @RequestMapping(value = SET_BOOK_URL, method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object updatetBook(@PathVariable Integer slug, @PathVariable Integer authorId, @PathVariable String title, Model model) {
        Book book = null;
        try {
            book = booksRepository.findById(slug);
            Author author = authorsRepository.findById(authorId);
            book.setAuthorId(author);
            book = booksRepository.update(slug, title, author);
        } catch (Exception e) {
            RestException re;
            if (e.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION);
                re.putTarget("book", book);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_BOOK_EXCEPTION);
                re.putTarget("book", book);
            }
            return re.exceptionMap();
        }
        SuccessMessage message
                = new SuccessMessage(MessageDefinitions.MOD_OPERATION,
                        MessageDefinitions.BOOK_ENTITY, new BookExport(book.getId(),
                                book.getTitle(), book.getAuthorId().getId()));

        return message;
    }

    @RequestMapping(value = ADD_BOOK_URL, method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object addBook(@PathVariable Integer authorId, @PathVariable String title) {
        Book book = null;
        try {
            Author author = authorsRepository.findById(authorId);
            if (author != null) {
                book = new Book();
                book.setAuthorId(author);
                book.setTitle(title);
            }

            Map validationMap = new HashMap();
            validationMap.put("authorId.lastName", author.getLastName());
            validationMap.put("authorId.firstName", author.getFirstName());
            validationMap.put("authorId.id", author.getId());
            validationMap.put("title", title);

            BindingResult result = new MapBindingResult(validationMap, "book");
            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            bookValidator.validate(book, result);

            if (result.hasErrors()) {
                RestException re = new RestException(MessageDetailDefinitions.SAVE_BOOK_EXCEPTION);
                re.putTarget("book", book);
                return re.exceptionMap();
            }
            booksRepository.addNew(book);
            SuccessMessage message
                    = new SuccessMessage(MessageDefinitions.ADD_OPERATION, MessageDefinitions.BOOK_ENTITY, new BookExport(book.getId(), book.getTitle(), book.getAuthorId().getId()));

            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_BOOK_EXCEPTION);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_BOOK_EXCEPTION);
            }
            re.putTarget("book", book);
            return re.exceptionMap();
        }
    }

    @RequestMapping(value = DEL_BOOK_URL, method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object deleteBook(@PathVariable Integer id) {
        Book book = null;
        try {
            book = booksRepository.findById(id);
            booksRepository.delete(book);
            SuccessMessage successMessage = new SuccessMessage();
            successMessage.setEntity(MessageDefinitions.BOOK_ENTITY);
            successMessage.setAction(MessageDefinitions.DEL_OPERATION);
            successMessage.setData(book);
            return successMessage;
        } catch (Exception ex) {
            RestException re = new RestException(MessageDetailDefinitions.DELETE_BOOK_EXCEPTION);
            re.putTarget("book", book);
            return re.exceptionMap();
        }
    }
}
