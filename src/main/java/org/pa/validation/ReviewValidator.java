/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.validation;

import org.pa.entity.Book;
import org.pa.entity.Review;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
@Component("reviewValidator")
public class ReviewValidator implements Validator {
    
    
      private final Validator bookValidator;

    public ReviewValidator() {
        bookValidator  = new BookValidator();
    }
      

    public ReviewValidator(Validator bookValidator) {
        if (bookValidator == null) {
            throw new IllegalArgumentException(
              "The supplied [Validator] is required and must not be null.");
        }
        if (!bookValidator.supports(Book.class)) {
            throw new IllegalArgumentException(
              "The supplied [Validator] must support the validation of [Book] instances.");
        }
     
        this.bookValidator = bookValidator;
    }
  
    
    @Override
    public boolean supports(Class<?> clazz) {
        return Review.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "body", "required", "Review Body is required.");
     
        
        if (  ((Review)target).getStars() < 0 || ((Review)target).getStars() > 5) {
              errors.rejectValue("stars", "stars");
        }
        Review review = (Review) target;
        try {
          errors.pushNestedPath("bookId");
           ValidationUtils.invokeValidator(this.bookValidator, review.getBookId(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
