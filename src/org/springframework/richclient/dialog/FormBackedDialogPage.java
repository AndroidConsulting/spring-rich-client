/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.richclient.dialog;

import javax.swing.JComponent;

import org.springframework.richclient.forms.AbstractFormPage;

/**
 * An implementation of DialogPage that delegates to an AbstractFormPage for its
 * control, pageComplete status and messages.
 * 
 * @author oliverh
 */
public class FormBackedDialogPage extends AbstractDialogPage {
    private AbstractFormPage backingFormPage;

    /**
     * Createa a new FormBackedDialogPage
     * 
     * @param backingFormPage
     *            a named form page that will provide the control for this
     *            dialog page
     */
    public FormBackedDialogPage(AbstractFormPage backingFormPage) {
        this(backingFormPage, true);
    }

    public FormBackedDialogPage(AbstractFormPage backingFormPage, boolean autoConfigure) {
        super(backingFormPage.getId(), autoConfigure);
        this.backingFormPage = backingFormPage;
    }

    /**
     * Creates a new FormPageBackedDialogPage.
     * 
     * @param parentPageId
     *            the id of a containing parent page. This will be used to
     *            configure page titles/description
     * @param backingFormPage
     *            the AbstractFormPage which will provide the control for this
     *            page.
     */
    public FormBackedDialogPage(String parentPageId,
            AbstractFormPage backingFormPage) {
        super(parentPageId
                + (backingFormPage.getId() != null ? "."
                        + backingFormPage.getId() : ""));
        this.backingFormPage = backingFormPage;
    }
    
    protected AbstractFormPage getBackingFormPage() {
        return backingFormPage;
    }

    public void onAboutToShow() {
        setEnabled(!backingFormPage.hasErrors());
    }

    protected JComponent createControl() {
        initPageValidationReporter();
        return backingFormPage.getControl();
    }

    protected void initPageValidationReporter() {
        backingFormPage.newSingleLineResultsReporter(this, this);
    }

    public void setEnabled(boolean enabled) {
        setPageComplete(enabled);
    }
}