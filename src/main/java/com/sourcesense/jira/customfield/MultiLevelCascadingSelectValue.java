package com.sourcesense.jira.customfield;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import org.apache.log4j.Logger;

/**
 * @author Alessandro Benedetti
 */
public class MultiLevelCascadingSelectValue {
    private String value;
    private OptionsManager optionsManager;

    private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectValue.class);

    public MultiLevelCascadingSelectValue(OptionsManager optionsManager, String value) {
        this.optionsManager = optionsManager;
        this.value = value;
    }

    public String getSearchValue() {
        return value;// test comment 235
    }


    @Override
    public String toString() {
        return value;
    }


}