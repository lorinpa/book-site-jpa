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
import org.pa.dto.ReviewExport;
import org.pa.entity.Book;
import org.pa.entity.Review;
import org.pa.exception.RestException;
import org.pa.repository.BooksRepository;
import org.pa.repository.ReviewsRepository;
import org.pa.rest.message.MessageDefinitions;
import org.pa.rest.message.SuccessMessage;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookValidator;
import org.pa.validation.ReviewValidator;
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
public class ReviewRest {

    public final static String EXPORT_ALL_URL = "/export/review/all";
    public final static String ADD_REVIEW_URL = "/add/review/{body}/{stars}/{book_id}";;
    public final static String MODIFY_REVIEW_URL = "/modify/review/{id}/{body}/{stars}/{book_id}";
    public final static String DELETE_REVIEW_URL = "/delete/review/{id}";
    private ReviewsRepository reviewRepo;
     private BooksRepository booksRepository;

    @Inject
    @Qualifier("reviewRepository")
    public void setReviewRepo(ReviewsRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

     @Inject
    @Qualifier("bookRepository")
    public void setBooksRepository(BooksRepository booksRepository) {
        this.booksRepository = booksRepository;
    }

    @RequestMapping(value = EXPORT_ALL_URL, method = RequestMethod.GET, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Map<String, List> exportToJson(Model model) throws Exception {

        Map<String, List> dto = new HashMap<>();
        List<ReviewExport> list = reviewRepo.exportAll();
        dto.put("reviews", list);

        return dto;
    }

      @RequestMapping(value = ADD_REVIEW_URL, method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object addReview(@PathVariable String body, @PathVariable Integer stars, @PathVariable Integer book_id) {
        Review review = null;

        try {
            review = new Review();
            review.setBody(body);
            review.setStars(stars);
            Book book = booksRepository.findById(book_id);
            review.setBookId(book);
             AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            ReviewValidator reviewValidator = new ReviewValidator(bookValidator);

            Map validationMap = new HashMap();
            validationMap.put("body", review.getBody());
            validationMap.put("stars", review.getStars());
            validationMap.put("bookId.title", review.getBookId().getTitle());
            validationMap.put("bookId.authorId.lastName", review.getBookId().getAuthorId().getLastName());
            validationMap.put("bookId.authorId.firstName", review.getBookId().getAuthorId().getFirstName());
            BindingResult result = new MapBindingResult(validationMap, "review");
            reviewValidator.validate(review, result);
            if (result.hasErrors()) {
                int numerrors  = result.getErrorCount();
                System.out.println("add review has " + numerrors + " errors");
                System.out.println("field error : " + result.getFieldError().getField());
                System.out.println("feild object: " + result.getFieldError().getObjectName());
                System.out.println("default message: " + result.getFieldError().getDefaultMessage());
                RestException re = new RestException("Unable to Add Review");
                re.putTarget("review", review);
                return re.exceptionMap();
            }
            reviewRepo.addNew(review);
            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.ADD_OPERATION, MessageDefinitions.REVIEW_ENTITY, new ReviewExport(review.getId(),
                    review.getStars(), review.getBody(),review.getBookId().getId()
                    ));
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException("Review  Already Exists! Can not add again!");
            } else {
                re = new RestException("Unexpected Exception. Unable to Save Review!");
            }
            re.putTarget("review", review);
            return re.exceptionMap();
        }
    }

     @RequestMapping(value = MODIFY_REVIEW_URL, method = RequestMethod.PUT, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Object updateCategory(@PathVariable Integer id, @PathVariable String body, @PathVariable Integer stars, @PathVariable Integer book_id) {

        Review review = null;
        try {
            review = new Review();
            review.setBody(body);
            review.setStars(stars);
            Book book = booksRepository.findById(book_id);
            review.setBookId(book);
            review.setId(id);
            Map validationMap = new HashMap();
            validationMap.put("id", review.getId());
            validationMap.put("body", review.getBody());
            validationMap.put("stars", review.getStars());
            //validationMap.put("bookId", review.getBookId());
            validationMap.put("bookId.title", review.getBookId().getTitle());
            validationMap.put("bookId.authorId.lastName", review.getBookId().getAuthorId().getLastName());
            validationMap.put("bookId.authorId.firstName", review.getBookId().getAuthorId().getFirstName());

            ReviewValidator reviewValidator = new ReviewValidator();
            BindingResult result = new MapBindingResult(validationMap, "review");
            reviewValidator.validate(review, result);
            if (result.hasErrors()) {
                RestException re = new RestException("Unable to Update Review");
                re.putTarget("review", review);
                return re.exceptionMap();
            }
            reviewRepo.update(review.getId(), review.getBookId(), review.getStars(),review.getBody());

            SuccessMessage message =
                    new SuccessMessage(MessageDefinitions.MOD_OPERATION, MessageDefinitions.REVIEW_ENTITY, new ReviewExport(review.getId(),
                    review.getStars(), review.getBody(),review.getBookId().getId()
                    ));
            return message;
        } catch (Exception pe) {
            RestException re;
            if (pe.getCause() instanceof ConstraintViolationException) {
                re = new RestException("Review Already Exists! Can not add again!");
            } else {
                re = new RestException("Unexpected Exception. Unable to Save Review!");
            }
            re.putTarget("review", review);
            return re.exceptionMap();
        }
    }

 @RequestMapping(value = DELETE_REVIEW_URL, method = RequestMethod.DELETE, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    Object deleteReview(@PathVariable Integer id) {
        Review review = null;
        try {
            review = reviewRepo.findById(id);
            reviewRepo.delete(review);
            SuccessMessage successMessage = new SuccessMessage();
            successMessage.setEntity(MessageDefinitions.REVIEW_ENTITY);
            successMessage.setAction(MessageDefinitions.DEL_OPERATION);
            successMessage.setData(review);
            return successMessage;

        } catch (Exception ex) {
            RestException re = new RestException(ex.getMessage());
            re.putTarget("review", review);
            return re.exceptionMap();
        }
    }
}
