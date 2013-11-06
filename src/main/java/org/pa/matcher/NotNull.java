package org.pa.matcher;



import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 *
 * @author
 * lorinpa
 * public-action.org
 */
public class NotNull extends BaseMatcher {

    @Override
    public boolean matches(Object item) {
       return item != null;
    }

    @Override
    public void describeTo(Description description) {
       
    }

  
    
}
