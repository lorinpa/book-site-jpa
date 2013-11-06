/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.pa.entity.Review;
import org.pa.repository.BooksRepository;
import org.pa.repository.ReviewsRepository;
import org.pa.validation.AuthorValidator;
import org.pa.validation.BookValidator;
import org.pa.validation.ReviewValidator;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * @author
 * lorinpa
 * public-action.org
 */
@Controller
public class ReviewController {

    private ReviewsRepository reviewRepo;
    private BooksRepository bookRepo;
  //  private EntityManagerFactory emf;

    private final static String ADD_URL = "review/add";
    private final static String MOD_URL = "review/edit";
    private final static String DEL_URL = "review/delete";
    private final static String LIST_URL = "review/list";
    private final static String REDIRECT_HOME_URL = "redirect:list.htm";


    /*   @Inject
     public void setEmf(EntityManagerFactory emf) {
     this.emf = emf;
     bookRepo = new BooksRepositoryImpl();
     bookRepo.setEntityManager(emf.createEntityManager());
     reviewRepo = new ReviewsRepositoryImpl();
     reviewRepo.setEntityManager(emf.createEntityManager());
     }*/
    @Inject
    @Qualifier("reviewRepository")
    public void setReviewRepo(ReviewsRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    @Inject
    @Qualifier("bookRepository")
    public void setBookRepo(BooksRepository bookRepo) {
        this.bookRepo = bookRepo;
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

    @RequestMapping(value = MOD_URL, method = RequestMethod.GET)
    public ModelAndView displayEdit(@RequestParam("id") Integer id) throws Exception {

        Review review = reviewRepo.findById(id);
        ModelAndView mav = new ModelAndView(MOD_URL);
        mav.addObject("review", review);
        mav.addObject("books", getBookList());
        return mav;
    }

    @RequestMapping(value = MOD_URL, method = RequestMethod.POST)
    public String submitEditForm(@ModelAttribute Review review, BindingResult result, Model model) {

        //  ModelAndView mav = new ModelAndView(MOD_URL);
        try {

            Book book = bookRepo.findById(review.getBookId().getId());
            if (book != null) {
                review.setBookId(book);
            }

            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            ReviewValidator reviewValidator = new ReviewValidator(bookValidator);
            reviewValidator.validate(review, result);
            if (result.hasErrors()) {
                model.addAttribute("books", getBookList());
                return MOD_URL;
            }
            reviewRepo.update(review.getId(), review.getBookId(), review.getStars(), review.getBody());
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("title", "Cateogory Already Exists! Can not add again!");
                result.addError(oe);
            }
            return MOD_URL;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return MOD_URL;
        }
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.GET)
    public ModelAndView displayDelete(@RequestParam("id") Integer id) throws Exception {
        Review review = reviewRepo.findById(id);
        ModelAndView mav = new ModelAndView(DEL_URL);
        mav.addObject("review", review);
        return mav;
    }

    @RequestMapping(value = DEL_URL, method = RequestMethod.POST)
    public String submitDelete(@ModelAttribute Review review, BindingResult result, Model model) {
        try {
            // unexpected quirck -- we have to retreive the whold entity before we can delete
            //     review = reviewRepo.findById(review.getId());
            reviewRepo.delete(review);
            return REDIRECT_HOME_URL;
        } catch (PersistenceException pe) {
            if (pe.getCause() instanceof ConstraintViolationException) {
                ObjectError oe = new ObjectError("stars", "Constraint Violation. Unable to delete review.");
                result.addError(oe);
            } else {
                ObjectError oe = new ObjectError("stars", "Unexpected Database Exception. Unable to delete review.");
                result.addError(oe);
            }
            return DEL_URL;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return DEL_URL;
        }
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.GET)
    public ModelAndView displayAdd() throws Exception {

        Review review = new Review();
        ModelAndView mav = new ModelAndView(ADD_URL);
        mav.addObject("review", review);
        mav.addObject("books", this.getBookList());
        return mav;
    }

    @RequestMapping(value = ADD_URL, method = RequestMethod.POST)
    public ModelAndView submitAddForm(@ModelAttribute Review review, BindingResult result, SessionStatus status) {

        ModelAndView mav = new ModelAndView(ADD_URL);
        try {
            Book book = bookRepo.findById(review.getBookId().getId());
            if (book != null) {
                review.setBookId(book);
            }

            AuthorValidator authorValidator = new AuthorValidator();
            BookValidator bookValidator = new BookValidator(authorValidator);
            ReviewValidator reviewValidator = new ReviewValidator(bookValidator);
            reviewValidator.validate(review, result);
            if (result.hasErrors()) {
                mav.addObject("books", getBookList());
                return mav;
            }
            reviewRepo.addNew(review);
            return list();
        } catch (PersistenceException pe) {
            ObjectError oe = new ObjectError("body", "Unexpected Exception. Unable to Save Review!");
            result.addError(oe);
            return mav;
        } catch (Exception ex) {
            Logger.getLogger(CategoryController.class.getName()).log(Level.SEVERE, null, ex);
            return mav;
        }
    }

    /* 
     * Unfucking real!!!! So the jsp had bookId instead of bookId.id
     * that raised errors pointing to converters, property editors????????
     * I had to 
     * 1) change the "path"?????? to bookId.id (the actual key???)
     * 2) when the form is submitted go to the db and get the book ????
     * 3) add the book 
     * 4) then validate
     * ??????? 
     * I guess that makes sense?? If verifes the review has a valid book parent?
     */
    @RequestMapping(value = LIST_URL, method = RequestMethod.GET)
    public ModelAndView list() throws Exception {
        List<Review> reviewList;
        reviewList = reviewRepo.findAll();
        ModelAndView mav = new ModelAndView(LIST_URL);
        mav.addObject("list", reviewList);
        return mav;
    }
}
