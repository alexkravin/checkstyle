////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2014  Oliver Burn
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////
package com.puppycrawl.tools.checkstyle.checks.blocks;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Utils;
import com.puppycrawl.tools.checkstyle.checks.AbstractOptionCheck;

/**
 * <p>
 * Checks the placement of left curly braces on types, methods and
 * other blocks:
 *  {@link  TokenTypes#LITERAL_CATCH LITERAL_CATCH},  {@link
 * TokenTypes#LITERAL_DO LITERAL_DO},  {@link TokenTypes#LITERAL_ELSE
 * LITERAL_ELSE},  {@link TokenTypes#LITERAL_FINALLY LITERAL_FINALLY},  {@link
 * TokenTypes#LITERAL_FOR LITERAL_FOR},  {@link TokenTypes#LITERAL_IF
 * LITERAL_IF},  {@link TokenTypes#LITERAL_SWITCH LITERAL_SWITCH},  {@link
 * TokenTypes#LITERAL_SYNCHRONIZED LITERAL_SYNCHRONIZED},  {@link
 * TokenTypes#LITERAL_TRY LITERAL_TRY},  {@link TokenTypes#LITERAL_WHILE
 * LITERAL_WHILE}.
 * </p>
 *
 * <p>
 * The policy to verify is specified using the {@link LeftCurlyOption} class and
 * defaults to {@link LeftCurlyOption#EOL}. Policies {@link LeftCurlyOption#EOL}
 * and {@link LeftCurlyOption#NLOW} take into account property maxLineLength.
 * The default value for maxLineLength is 80.
 * </p>
 * <p>
 * An example of how to configure the check is:
 * </p>
 * <pre>
 * &lt;module name="LeftCurly"/&gt;
 * </pre>
 * <p>
 * An example of how to configure the check with policy
 * {@link LeftCurlyOption#NLOW} and maxLineLength 120 is:
 * </p>
 * <pre>
 * &lt;module name="LeftCurly"&gt;
 *      &lt;property name="option"
 * value="nlow"/&gt;     &lt;property name="maxLineLength" value="120"/&gt; &lt;
 * /module&gt;
 * </pre>
 * <p>
 * An example of how to configure the check to validate enum definitions:
 * </p>
 * <pre>
 * &lt;module name="LeftCurly"&gt;
 *      &lt;property name="ignoreEnums" value="false"/&gt;
 * &lt;/module&gt;
 * </pre>
 *
 * @author Oliver Burn
 * @author lkuehne
 * @author maxvetrenko
 * @version 1.0
 */
public class LeftCurlyCheck
    extends AbstractOptionCheck<LeftCurlyOption>
{
    /** default maximum line length */
    private static final int DEFAULT_MAX_LINE_LENGTH = 80;

    /** TODO: replace this ugly hack **/
    private int maxLineLength = DEFAULT_MAX_LINE_LENGTH;

    /** If true, Check will ignore enums*/
    private boolean ignoreEnums = true;

    /**
     * Creates a default instance and sets the policy to EOL.
     */
    public LeftCurlyCheck()
    {
        super(LeftCurlyOption.EOL, LeftCurlyOption.class);
    }

    /**
     * Sets the maximum line length used in calculating the placement of the
     * left curly brace.
     * @param maxLineLength the max allowed line length
     */
    public void setMaxLineLength(int maxLineLength)
    {
        this.maxLineLength = maxLineLength;
    }

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {
            TokenTypes.INTERFACE_DEF,
            TokenTypes.CLASS_DEF,
            TokenTypes.ANNOTATION_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.CTOR_DEF,
            TokenTypes.METHOD_DEF,
            TokenTypes.ENUM_CONSTANT_DEF,
            TokenTypes.LITERAL_WHILE,
            TokenTypes.LITERAL_TRY,
            TokenTypes.LITERAL_CATCH,
            TokenTypes.LITERAL_FINALLY,
            TokenTypes.LITERAL_SYNCHRONIZED,
            TokenTypes.LITERAL_SWITCH,
            TokenTypes.LITERAL_DO,
            TokenTypes.LITERAL_IF,
            TokenTypes.LITERAL_ELSE,
            TokenTypes.LITERAL_FOR,
            // TODO: need to handle....
            //TokenTypes.STATIC_INIT,
        };
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        final DetailAST startToken;
        final DetailAST brace;

        switch (ast.getType()) {
        case TokenTypes.CTOR_DEF :
        case TokenTypes.METHOD_DEF :
            startToken = skipAnnotationOnlyLines(ast);
            brace = ast.findFirstToken(TokenTypes.SLIST);
            break;

        case TokenTypes.INTERFACE_DEF :
        case TokenTypes.CLASS_DEF :
        case TokenTypes.ANNOTATION_DEF :
        case TokenTypes.ENUM_DEF :
        case TokenTypes.ENUM_CONSTANT_DEF :
            startToken = skipAnnotationOnlyLines(ast);
            final DetailAST objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
            brace = (objBlock == null)
                ? null
                : (DetailAST) objBlock.getFirstChild();
            break;

        case TokenTypes.LITERAL_WHILE:
        case TokenTypes.LITERAL_CATCH:
        case TokenTypes.LITERAL_SYNCHRONIZED:
        case TokenTypes.LITERAL_FOR:
        case TokenTypes.LITERAL_TRY:
        case TokenTypes.LITERAL_FINALLY:
        case TokenTypes.LITERAL_DO:
        case TokenTypes.LITERAL_IF :
            startToken = ast;
            brace = ast.findFirstToken(TokenTypes.SLIST);
            break;

        case TokenTypes.LITERAL_ELSE :
            startToken = ast;
            final DetailAST candidate = ast.getFirstChild();
            brace =
                (candidate.getType() == TokenTypes.SLIST)
                ? candidate
                : null; // silently ignore
            break;

        case TokenTypes.LITERAL_SWITCH :
            startToken = ast;
            brace = ast.findFirstToken(TokenTypes.LCURLY);
            break;

        default :
            startToken = null;
            brace = null;
        }

        if ((brace != null) && (startToken != null)) {
            verifyBrace(brace, startToken);
        }
    }

    /**
     * Skip lines that only contain <code>TokenTypes.ANNOTATION</code>s.
     * If the received <code>DetailAST</code>
     * has annotations within its modifiers then first token on the line
     * of the first token afer all annotations is return. This might be
     * an annotation.
     * Otherwise, the received <code>DetailAST</code> is returned.
     * @param ast <code>DetailAST</code>.
     * @return <code>DetailAST</code>.
     */
    private DetailAST skipAnnotationOnlyLines(DetailAST ast)
    {
        final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return ast;
        }
        DetailAST lastAnnot = findLastAnnotation(modifiers);
        if (lastAnnot == null) {
            // There are no annotations.
            return ast;
        }
        final DetailAST tokenAfterLast = lastAnnot.getNextSibling() != null
                                       ? lastAnnot.getNextSibling()
                                       : modifiers.getNextSibling();
        if (tokenAfterLast.getLineNo() > lastAnnot.getLineNo()) {
            return tokenAfterLast;
        }
        final int lastAnnotLineNumber = lastAnnot.getLineNo();
        while (lastAnnot.getPreviousSibling() != null
               && (lastAnnot.getPreviousSibling().getLineNo()
                    == lastAnnotLineNumber))
        {
            lastAnnot = lastAnnot.getPreviousSibling();
        }
        return lastAnnot;
    }

    /**
     * Find the last token of type <code>TokenTypes.ANNOTATION</code>
     * under the given set of modifiers.
     * @param modifiers <code>DetailAST</code>.
     * @return <code>DetailAST</code> or null if there are no annotations.
     */
    private DetailAST findLastAnnotation(DetailAST modifiers)
    {
        DetailAST annot = modifiers.findFirstToken(TokenTypes.ANNOTATION);
        while (annot != null && annot.getNextSibling() != null
               && annot.getNextSibling().getType() == TokenTypes.ANNOTATION)
        {
            annot = annot.getNextSibling();
        }
        return annot;
    }

    /**
     * Verifies that a specified left curly brace is placed correctly
     * according to policy.
     * @param brace token for left curly brace
     * @param startToken token for start of expression
     */
    private void verifyBrace(final DetailAST brace,
                             final DetailAST startToken)
    {
        final String braceLine = getLine(brace.getLineNo() - 1);

        // calculate the previous line length without trailing whitespace. Need
        // to handle the case where there is no previous line, cause the line
        // being check is the first line in the file.
        final int prevLineLen = (brace.getLineNo() == 1)
            ? maxLineLength
            : Utils.lengthMinusTrailingWhitespace(getLine(brace.getLineNo() - 2));

        // Check for being told to ignore, or have '{}' which is a special case
        if ((braceLine.length() > (brace.getColumnNo() + 1))
            && (braceLine.charAt(brace.getColumnNo() + 1) == '}'))
        {
            ; // ignore
        }
        else if (getAbstractOption() == LeftCurlyOption.NL) {
            if (!Utils.whitespaceBefore(brace.getColumnNo(), braceLine)) {
                log(brace.getLineNo(), brace.getColumnNo(),
                    "line.new", "{");
            }
        }
        else if (getAbstractOption() == LeftCurlyOption.EOL) {
            if (Utils.whitespaceBefore(brace.getColumnNo(), braceLine)
                && ((prevLineLen + 2) <= maxLineLength))
            {
                log(brace.getLineNo(), brace.getColumnNo(),
                    "line.previous", "{");
            }
            if (!hasLineBreakAfter(brace)) {
                log(brace.getLineNo(), brace.getColumnNo(), "line.break.after");
            }
        }
        else if (getAbstractOption() == LeftCurlyOption.NLOW) {
            if (startToken.getLineNo() == brace.getLineNo()) {
                ; // all ok as on the same line
            }
            else if ((startToken.getLineNo() + 1) == brace.getLineNo()) {
                if (!Utils.whitespaceBefore(brace.getColumnNo(), braceLine)) {
                    log(brace.getLineNo(), brace.getColumnNo(),
                        "line.new", "{");
                }
                else if ((prevLineLen + 2) <= maxLineLength) {
                    log(brace.getLineNo(), brace.getColumnNo(),
                        "line.previous", "{");
                }
            }
            else if (!Utils.whitespaceBefore(brace.getColumnNo(), braceLine)) {
                log(brace.getLineNo(), brace.getColumnNo(),
                    "line.new", "{");
            }
        }
    }

    /**
     * Checks if left curly has line break after.
     * @param leftCurly
     *        Left curly token.
     * @return
     *        True, left curly has line break after.
     */
    private boolean hasLineBreakAfter(DetailAST leftCurly)
    {
        DetailAST nextToken = null;
        if (leftCurly.getType() == TokenTypes.SLIST) {
            nextToken = leftCurly.getFirstChild();
        }
        else {
            if (leftCurly.getParent().getParent().getType() == TokenTypes.ENUM_DEF)
            {
                if (!ignoreEnums) {
                    nextToken = leftCurly.getNextSibling();
                }
            }
        }
        if (nextToken != null && nextToken.getType() != TokenTypes.RCURLY) {
            if (leftCurly.getLineNo() == nextToken.getLineNo()) {
                return false;
            }
        }
        return true;
    }
}
