/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.validation;

import org.pa.entity.Author;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class AuthorValidator implements Validator {
     
   @Override
    public boolean supports(Class<?> clazz) {
        return Author.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "required", "Field is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "required", "Field is required.");
        
    }
}
