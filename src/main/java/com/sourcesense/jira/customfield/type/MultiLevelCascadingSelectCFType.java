package com.sourcesense.jira.customfield.type;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.EasyList;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.sourcesense.jira.common.OptionsMap;
import com.sourcesense.jira.customfield.config.SettableMultiLevelOptionsConfigItem4;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * This class represents the MultiLevelCascading Select Custom Field Type.
 *
 * @author Alessandro Benedetti
 */

public class MultiLevelCascadingSelectCFType extends CascadingSelectCFType {
    public static String EMPTY_VALUE = "_none_";

    public static String EMPTY_VALUE_ID = "-2";

    public static long EMPTY_VALUE_ID_LONG = -2;

    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

    public MultiLevelCascadingSelectCFType(OptionsManager optionsManager, CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
                                           JqlSelectOptionsUtil jqlSelectOptionsUtil) {
        super(optionsManager, customFieldValuePersister, genericConfigManager);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    }

    public boolean equalsOption(Option op1, Option op2) {
        if (op1 == null || op2 == null) {
            return op1 == op2;
        }

        if (equalsOption(op1.getParentOption(), op2.getParentOption())) {
            if (op1.getOptionId() == null) {
                return op2.getOptionId() == null;
            }
            return (op1.getOptionId().equals(op2.getOptionId()));
        }
        return false;
    }

    /**
     * takes in input a fileConfig and an Option, then it extracts the list of options from the input
     * config and from this list ,it checks if the input option belong to the selected Set.
     *
     * @param config
     * @param option
     * @return
     */
    public boolean optionValidForConfig(FieldConfig config, Option option) {
        final Options options = ManagerFactory.getOptionsManager().getOptions(config);
        if (options != null && option != null) {
            Option realOption = options.getOptionById(option.getOptionId());
            return equalsOption(realOption, option);
        }
        return false;
    }

    /**
     * checks the input Option verifying that: 1)it's null 2)it's valid for the input FileConfig
     * 3)it's son of the parent in input
     *
     * @param customFieldId
     * @param option
     * @param parentOption
     * @param errorCollectionToAddTo
     * @param config
     * @return
     */
    private boolean checkOption(String customFieldId, Option option, Option parentOption, ErrorCollection errorCollectionToAddTo, FieldConfig config) {
        if (option == null) {
            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.parent", "'" + parentOption + "'", "'" + config.getName() + "'"));
            return false;
        }
        if (!optionValidForConfig(config, option)) {
            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context", "'" + parentOption + "'", "'" + config.getName() + "'"));
            return false;
        }

        return true;
    }

    /**
     * trasforms the object(Option) in input in an Option.
     *
     * @param value
     * @param object
     * @return
     */
    private Option trasformToOption(FieldConfig config, Object value) {
        if (value instanceof Option) {
            return (Option) value;
        }
        if (value instanceof String && EMPTY_VALUE_ID.equals(value)) {
            return super.optionsManager.createOption(config, EMPTY_VALUE_ID_LONG, EMPTY_VALUE_ID_LONG, EMPTY_VALUE);
        } else if (value instanceof String && !"-1".equals(value)) {
            return (Option) this.getSingularObjectFromString((String) value);
        }
        return null;
    }

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config) {
        log.debug("Pre- Validate Error collection: [" + errorCollectionToAddTo.getErrors() + "]");

        if (relevantParams == null || relevantParams.isEmpty()) {
            return;
        }

        Option parentOption = null;
        String customFieldId = config.getCustomField().getId();
        int count = relevantParams.getAllKeys().size();
        for (int i = 0; i < count; i++) {
            Option option1 = trasformToOption(config, relevantParams.getFirstValueForKey(i == 0 ? null : String.valueOf(i)));
            if (option1 != null && !option1.toString().contains(":") && !option1.toString().equals(EMPTY_VALUE)) {
                if (option1 != null) {
                    log.debug("check option: [" + option1 + "]");
                    if (!checkOption(customFieldId, option1, parentOption, errorCollectionToAddTo, config)) {
                        return;
                    }
                }
                parentOption = option1;
            } else {
                Collection<String> valueStrings = relevantParams.getAllValues();
                String[] splittedStrings = null;
                for (String s : valueStrings) {
                    splittedStrings = s.split(":");
                }
                for (int j = 0; j < splittedStrings.length; j++) {
                    // this part probably is useless, but for sure is not useful for "none" options
                    if (!splittedStrings[j].equals(EMPTY_VALUE_ID)) {
                        Long longOptionValue = new Long(splittedStrings[j]);
                        Option option = jqlSelectOptionsUtil.getOptionById(longOptionValue);
                        if (option != null) {
                            log.debug("check option: [" + option + "]");
                            if (!checkOption(customFieldId, option, parentOption, errorCollectionToAddTo, config)) {
                                return;
                            }
                        }
                        parentOption = option;
                    }
                }

            }
        }
        log.debug("Post-Validate Error collection: [" + errorCollectionToAddTo.getErrors() + "]");
    }

    /**
     * add to the default file Config the specific ConfigItem for the multi level cascading select
     * custom field
     *
     * @see com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType#getConfigurationItemTypes()
     */
    @Override
    public List getConfigurationItemTypes() {
        final List configurationItemTypes = EasyList.build(JiraUtils.loadComponent(DefaultValueConfigItem.class));
        configurationItemTypes.add(new SettableMultiLevelOptionsConfigItem4(optionsManager));
        return configurationItemTypes;
    }

    public OptionsMap getOptionMapFromOptions(Options options) {

        return new OptionsMap(options);
    }

    /**
     * return the velocity parameter for the issue and custom field in input no woking for bugged
     *
     * @see com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType#getVelocityParameters(com.atlassian.jira.issue.Issue,
     *      com.atlassian.jira.issue.fields.CustomField,
     *      com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)
     */
    @Override
    public Map getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {

        Map map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        map.put("mlcscftype", this);
        map.put("fieldLayout", fieldLayoutItem);
        return map;
    }

    private final Logger log = Logger.getLogger(MultiLevelCascadingSelectCFType.class);
}
