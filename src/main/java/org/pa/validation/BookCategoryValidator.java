/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.validation;

import org.pa.entity.Author;
import org.pa.entity.Book;
import org.pa.entity.BookCategory;
import org.pa.entity.Category;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class BookCategoryValidator implements Validator {
    
     private final Validator bookValidator;
     private final Validator categoryValidator;

    public BookCategoryValidator(Validator bookValidator, Validator categoryValidator) {
         if (bookValidator == null) {
            throw new IllegalArgumentException(
              "The supplied [Book Validator] is required and must not be null.");
        }
        if (!bookValidator.supports(Book.class)) {
            throw new IllegalArgumentException(
              "The supplied [Validator] must support the validation of [Book] instances.");
        }
        this.bookValidator = bookValidator;
        
         if (categoryValidator == null) {
            throw new IllegalArgumentException(
              "The supplied [Book Validator] is required and must not be null.");
        }
        if (!categoryValidator.supports(Category.class)) {
            throw new IllegalArgumentException(
              "The supplied [Validator] must support the validation of [Book] instances.");
        }
        this.categoryValidator = categoryValidator;
    }
     
     
    
     @Override
    public boolean supports(Class<?> clazz) {
        return BookCategory.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
      
        BookCategory bookCat = (BookCategory) target;
        try {
            errors.pushNestedPath("bookId");
            ValidationUtils.invokeValidator(this.bookValidator, bookCat.getBookId(), errors);
        } finally {
            errors.popNestedPath();
        }
         try {
            errors.pushNestedPath("categoryId");
            ValidationUtils.invokeValidator(this.categoryValidator, bookCat.getCategoryId(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
