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
import org.pa.dto.BookCategoryExport;
import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.pa.exception.MessageDetailDefinitions;
import org.pa.exception.RestException;
import org.pa.repository.BookCategoriesRepository;
import org.pa.repository.BooksRepository;
import org.pa.repository.CategoriesRepository;
import org.pa.rest.message.MessageDefinitions;
import org.pa.rest.message.SuccessMessage;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookCategoryValidator;
import org.pa.validation.BookValidator;
import org.pa.validation.CategoryValidator;
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
public class BookCategoryRest {

     public final static String EXPORT_ALL_URL = "/export/book_category/all";

    public final static String ADD_BOOK_CATEGORY_URL = "/add/book_category/{book_id}/{category_id}";
    public final static String MODIFY_BOOK_CATEGORY_URL = "/modify/book_category/{id}/{book_id}/{category_id}";
    public final static String DELETE_BOOK_CATEGORY_URL = "/delete/book_category/{id}";


     private BookCategoriesRepository bookCatRepo;
      private BooksRepository booksRepository;
      private CategoriesRepository categoryRepo;

    @Inject
    @Qualifier("BookCategoryRepository")
    public void setBookCatRepo(BookCategoriesRepository bookCatRepo) {
        this.bookCatRepo = bookCatRepo;
    }

    @Inject
    @Qualifier("bookRepository")
    public void setBooksRepository(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    @Inject
    @Qualifier("categoryRepository")
    public void setCategoryRepo(CategoriesRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }


    @RequestMapping(value = EXPORT_ALL_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Map<String, List> exportToJson(Model model) throws Exception {
        Map<String, List> dto = new HashMap<>();
        List<BookCategoryExport>  list = bookCatRepo.exportAll();
        dto.put("book_categories", list);

        return dto;
    }


    @RequestMapping(value = ADD_BOOK_CATEGORY_URL, method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object addBookCategory(@PathVariable Integer book_id, @PathVariable Integer category_id) {
        BookCategory bookCategory = null;

        try {
            bookCategory = new BookCategory();
            Book book = booksRepository.findById(book_id);
            Category category = categoryRepo.findById(category_id);
            bookCategory.setBookId(book);
            bookCategory.setCategoryId(category);

            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            CategoryValidator categoryValidator = new CategoryValidator();
            BookCategoryValidator bookCategoryValidator = new BookCategoryValidator(bookValidator, categoryValidator);

            Map validationMap = new HashMap();
            //validationMap.put("bookId", bookCategory.getBookId());
            validationMap.put("bookId.title", bookCategory.getBookId().getTitle());
            validationMap.put("bookId.authorId.lastName", bookCategory.getBookId().getAuthorId().getLastName());
            validationMap.put("bookId.authorId.firstName", bookCategory.getBookId().getAuthorId().getFirstName());
            validationMap.put("categoryId.title", bookCategory.getCategoryId().getTitle());

            BindingResult result = new MapBindingResult(validationMap, "bookCategory");
            bookCategoryValidator.validate(bookCategory, result);
            if (result.hasErrors()) {
                RestException re = new RestException(MessageDetailDefinitions.ADD_BOOK_CATEGORY_EXCEPTION);
                re.putTarget("bookCategory", bookCategory);
                return re.exceptionMap();
            }
            bookCatRepo.addNew(bookCategory);
            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.ADD_OPERATION, MessageDefinitions.BOOK_CATEGORY_ENTITY, new BookCategoryExport(
                    bookCategory.getId(), bookCategory.getBookId().getId(),bookCategory.getCategoryId().getId()
                    ));
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }
            re.putTarget("bookCategory", bookCategory);
            return re.exceptionMap();
        }
    }

  @RequestMapping(value = MODIFY_BOOK_CATEGORY_URL, method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Object updateCategory(@PathVariable Integer id, @PathVariable Integer book_id, @PathVariable Integer category_id) {

        BookCategory bookCategory = null;
        try {
            bookCategory = new BookCategory();
            Book book = booksRepository.findById(book_id);
            bookCategory.setBookId(book);
            Category category = categoryRepo.findById(category_id);
            bookCategory.setCategoryId(category);
            bookCategory.setId(id);
            Map validationMap = new HashMap();
            validationMap.put("id", bookCategory.getId());
            validationMap.put("bookId.title", bookCategory.getBookId().getTitle());
            validationMap.put("bookId.authorId.lastName", bookCategory.getBookId().getAuthorId().getLastName());
            validationMap.put("bookId.authorId.firstName", bookCategory.getBookId().getAuthorId().getFirstName());
            validationMap.put("categoryId.title", bookCategory.getCategoryId().getTitle());

            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            CategoryValidator categoryValidator = new CategoryValidator();
            BookCategoryValidator bookCategoryValidator = new BookCategoryValidator(bookValidator, categoryValidator);

            BindingResult result = new MapBindingResult(validationMap, "bookCategory");
            bookCategoryValidator.validate(bookCategory, result);
            if (result.hasErrors()) {
                RestException re = new RestException("Unable to Update Book Category");
                re.putTarget("bookCategory", bookCategory);
                return re.exceptionMap();
            }
            bookCatRepo.update(bookCategory.getId(), bookCategory.getBookId(),bookCategory.getCategoryId());

            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.MOD_OPERATION, MessageDefinitions.BOOK_CATEGORY_ENTITY, new BookCategoryExport(
                    bookCategory.getId(), bookCategory.getBookId().getId(),bookCategory.getCategoryId().getId()
                    ));
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException(MessageDetailDefinitions.DUPLICATE_BOOK_CATEGORY_EXCEPTION);
            } else {
                re = new RestException(MessageDetailDefinitions.SAVE_BOOK_CATEGORY_EXCEPTION);
            }
            re.putTarget("bookCategory", bookCategory);
            return re.exceptionMap();
        }
    }

 @RequestMapping(value = DELETE_BOOK_CATEGORY_URL, method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object deleteBook(@PathVariable Integer id) {
        BookCategory bookCategory = null;
        try {
            bookCategory = bookCatRepo.findById(id);
            bookCatRepo.delete(bookCategory);
            SuccessMessage successMessage = new SuccessMessage();
            successMessage.setEntity(MessageDefinitions.BOOK_CATEGORY_ENTITY);
            successMessage.setAction(MessageDefinitions.DEL_OPERATION);
            successMessage.setData(new BookCategoryExport(
                    bookCategory.getId(), bookCategory.getBookId().getId(),bookCategory.getCategoryId().getId()));
            return successMessage;

        } catch (Exception ex) {
            RestException re = new RestException(ex.getMessage());
            re.putTarget("bookCategory", bookCategory);
            return re.exceptionMap();
        }
    }
}
