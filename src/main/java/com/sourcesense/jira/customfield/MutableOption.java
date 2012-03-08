package com.sourcesense.jira.customfield;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;


/**
 * @author Alessandro Benedetti
 */
public class MutableOption implements Option {
    private Option option;

    public MutableOption(Option option) {
        this.option = option;
    }

    public Long getOptionId() {
        return option.getOptionId();
    }

    public Long getSequence() {
        return option.getSequence();
    }

    public String getValue() {
        return option.getValue();
    }

    public GenericValue getGenericValue() {
        return option.getGenericValue();
    }

    public FieldConfig getRelatedCustomField() {
        return option.getRelatedCustomField();
    }

    public Option getParentOption() {
        return option.getParentOption();
    }

    public List getChildOptions() {
        return option.getChildOptions();
    }

    public void setSequence(Long sequence) {
        option.setSequence(sequence);
    }

    public List retrieveAllChildren(List listToAddTo) {
        return option.retrieveAllChildren(listToAddTo);
    }

    public void store() {
        option.store();
    }

    public void setValue(String value) {
        option.getGenericValue().set("value", value);
    }

    @Override
    public Boolean getDisabled() {
        return option.getDisabled();
    }

    @Override
    public void setDisabled(Boolean value) {
        option.setDisabled(value);
    }

    @Override
    public String toString() {
        return option.toString();
    }
}
