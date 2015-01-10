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
package com.puppycrawl.tools.checkstyle.api;

import java.util.EventListener;


/**
 * Listener in charge of receiving events from the Checker.
 * Typical events sequence is:
 * <pre>
 * auditStarted
 *   (fileStarted
 *     (addError)*
 *   fileFinished )*
 * auditFinished
 * </pre>
 * @author <a href="mailto:stephane.bailliez@wanadoo.fr">Stephane Bailliez</a>
 */
public interface AuditListener
    extends EventListener
{
    /**
     * Notify that the audit is about to start.
     * @param evt the event details
     */
    void auditStarted(AuditEvent evt);

    /**
     * Notify that the audit is finished.
     * @param evt the event details
     */
    void auditFinished(AuditEvent evt);

    /**
     * Notify that audit is about to start on a specific file.
     * @param evt the event details
     */
    void fileStarted(AuditEvent evt);

    /**
     * Notify that audit is finished on a specific file.
     * @param evt the event details
     */
    void fileFinished(AuditEvent evt);

    /**
     * Notify that an audit error was discovered on a specific file.
     * @param evt the event details
     */
    void addError(AuditEvent evt);

    /**
     * Notify that an exception happened while performing audit.
     * @param evt the event details
     * @param throwable details of the exception
     */
    void addException(AuditEvent evt, Throwable throwable);
}
