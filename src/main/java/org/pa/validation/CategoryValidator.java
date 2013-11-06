/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pa.validation;

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
public class CategoryValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Category.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "required", "Field is required.");
    }
    
}
