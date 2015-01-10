////////////////////////////////////////////////////////////////////////////////
// Test case file for checkstyle.
// Created: 2001
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle;

import java.io.*; // star import for instantiation tests
import java.awt.Dimension; // explicit import for instantiation tests
import java.awt.Color;

/**
 * Test case for detecting simple semantic errors.
 * @author Lars Kühne
 **/
class InputSemantic
{
    /* Boolean instantiation in a static initializer */
    static {
        Boolean x = new Boolean(true);
    }

    /* Boolean instantiation in a non-static initializer */
    {
        Boolean x = new Boolean(true);
        Boolean[] y = new Boolean[]{Boolean.TRUE, Boolean.FALSE};
    }

    /** fully qualified Boolean instantiation in a method. **/
    Boolean getBoolean()
    {
        return new java.lang.Boolean(true);
    }

    void otherInstantiations()
    {
        // instantiation of classes in the same package
        Object o1 = new InputBraces();
        Object o2 = new InputModifier();
        // classes in another package with .* import
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        File f = new File("/tmp");
        // classes in another package with explicit import
        Dimension dim = new Dimension();
        Color col = new Color(0, 0, 0);
    }

    void exHandlerTest()
    {
        try {
            ; // do stuff and don't handle exceptions in some cases
        }
        catch (IllegalStateException emptyCatchIsAlwaysAnError) {
        }
        catch (NullPointerException ex) {
            // can never happen, but only commentig this is currently an error
            // Possible future enhancement: allowEmptyCatch="commented"
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            ;
            // can never happen, semicolon makes checkstyle happy
            // this is a workaround for above problem
        }
        catch (NegativeArraySizeException ex) {
            {
            }
            // can never happen, empty compound statement is another workaround
        }
        catch (UnsupportedOperationException handledException) {
            System.out.println(handledException.getMessage());
        }
        catch (SecurityException ex) { /* hello */ }
        catch (StringIndexOutOfBoundsException ex) {}
        catch (IllegalArgumentException ex) { }

        try {
        }
        finally {
        }
        try {
        // something
        }
        finally {
            // something
        }
        try {
            ; // something
        }
        finally {
            ; // statement
        }
    }

    /** test **/
    private static final long IGNORE = 666l + 666L;





















    public class EqualsVsHashCode1
    {
        public boolean equals(int a) // wrong arg type, don't flag
        {
            return a == 1;
        }
    }

    public class EqualsVsHashCode2
    {
        public boolean equals(String a) // flag
        {
            return true;
        }
    }

    public class EqualsVsHashCode3
    {
        public boolean equals(Object a) // don't flag
        {
            return true;
        }

        public int hashCode()
        {
            return 0;
        }
    }

    public class EqualsVsHashCode4
    {
        // in anon inner class
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream()
        {
            public boolean equals(Object a) // don't flag
            {
                return true;
            }

            public int hashCode()
            {
                return 0;
            }
        };

        ByteArrayOutputStream bos2 = new ByteArrayOutputStream()
        {
            public boolean equals(Object a) // flag
            {
                return true;
            }
        };
    }

    public void triggerEmptyBlockWithoutBlock()
    {
        // an if statement without a block to increase test coverage
        if (true)
            return;
    }
    
    // empty instance initializer
    {
    }

    public class EqualsVsHashCode5
    {
        public <A> boolean equals(int a) // wrong arg type, don't flag even with generics
        {
            return a == 1;
        }
    }

    public class EqualsVsHashCode6
    {
        public <A> boolean equals(Comparable<A> a) // flag, weven with generics
        {
            return true;
        }
    }
    
    private class InputBraces {
        
    }
    
    private class InputModifier {
        
    }

}
