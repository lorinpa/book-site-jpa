/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.validation;

import org.pa.entity.Author;
import org.pa.entity.Book;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class BookValidator implements Validator {
    
     private final Validator authorValidator;

    public BookValidator() {
        authorValidator = new AuthorValidator();
    }
     
     

    public BookValidator(Validator authorValidator) {
         if (authorValidator == null) {
            throw new IllegalArgumentException(
              "The supplied [Validator] is required and must not be null.");
        }
        if (!authorValidator.supports(Author.class)) {
            throw new IllegalArgumentException(
              "The supplied [Validator] must support the validation of [Author] instances.");
        }
        this.authorValidator = authorValidator;
    }
     
     
    
     @Override
    public boolean supports(Class<?> clazz) {
        return Book.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "required", "Field is required.");
        Book book = (Book) target;
        try {
            errors.pushNestedPath("authorId");
            ValidationUtils.invokeValidator(this.authorValidator, book.getAuthorId(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
