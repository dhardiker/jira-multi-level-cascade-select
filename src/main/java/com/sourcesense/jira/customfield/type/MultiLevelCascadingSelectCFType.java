package com.sourcesense.jira.customfield.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.config.item.DefaultValueConfigItem;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.query.operand.SingleValueOperand;
import com.google.common.collect.Lists;
import com.sourcesense.jira.common.OptionsMap;
import com.sourcesense.jira.customfield.config.SettableMultiLevelOptionsConfigItem4;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.equals;
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

    private final OptionsManager optionsManager;

    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final CustomFieldValuePersister customFieldValuePersister;

    public MultiLevelCascadingSelectCFType(OptionsManager optionsManager, CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager,
                                           JiraBaseUrls jiraBaseUrls) {
        super(optionsManager, customFieldValuePersister, genericConfigManager, jiraBaseUrls);
        this.customFieldValuePersister = customFieldValuePersister;
        this.optionsManager = optionsManager;
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", ComponentAccessor.getComponentOfType(JqlSelectOptionsUtil.class));
    }

    public static int findDepth(Option option) {
        int depth = 0;
        Option parent = option;
        while((parent = parent.getParentOption()) != null) {
            depth ++;
        }
        return depth;
    }

    private Option getOptionValueForParentId(CustomField field, String sParentOptionId, Issue issue)
    {
        Collection values;

        values = customFieldValuePersister.getValues(field, issue.getId(), CASCADE_VALUE_TYPE, sParentOptionId);


        if (values != null && !values.isEmpty())
        {
            String optionId = (String) values.iterator().next();
            return optionsManager.findByOptionId(OptionUtils.safeParseLong(optionId));
        }
        else
        {
            return null;
        }
    }

    public Map<String, Option> getValueFromIssue(CustomField field, Issue issue)
    {
        Option parentOption = getOptionValueForParentId(field, null, issue);
        if (parentOption != null)
        {
            Map<String, Option> options = new HashMap<String, Option>();

            options.put(PARENT_KEY, parentOption);
            Option option = parentOption;
            Integer index = 1;
            while((option = getOptionValueForParentId(field, option.getOptionId().toString(), issue)) != null) {
                options.put(index.toString(), option);
                index++;
                //option = getOptionValueForParentId(field, option.getOptionId().toString(), issue);
            }
            return options;
        }
        else
        {
            return null;
        }
    }
    
    public Map<String, Option> getValueFromCustomFieldParams(CustomFieldParams relevantParams) throws FieldValidationException
    {
        if (relevantParams != null && !relevantParams.isEmpty())
        {
            return getOptionMapFromCustomFieldParams(relevantParams);
        }
        else
        {
            return null;
        }

    }
    
    private Map<String, Option> getOptionMapFromCustomFieldParams(CustomFieldParams params) throws FieldValidationException
    {
        Map<String, Option> options = new HashMap<String, Option>();

        final Map keysAndValues = params.getKeysAndValues();
        for (Object key : keysAndValues.keySet()) {
            options.put((String)key, extractOptionFromParams((String)key, params));
        }

        return options;
    }
    
    private Option extractOptionFromParams(String key, CustomFieldParams relevantParams) throws FieldValidationException
    {
        Collection<String> params = relevantParams.getValuesForKey(key);
        if (params != null && !params.isEmpty())
        {
            String selectValue = params.iterator().next();
            if (ObjectUtils.isValueSelected(selectValue) && selectValue != null)
            {
                return getOptionFromStringValue(selectValue);
            }
        }

        return null;
    }

    private Option getOptionFromStringValue(String selectValue) throws FieldValidationException
    {
        final Long aLong = OptionUtils.safeParseLong(selectValue);
        if (aLong != null)
        {
            final Option option = optionsManager.findByOptionId(aLong);
            if (option != null)
            {
                return option;
            }
            else
            {
                throw new FieldValidationException("'" + aLong + "' is an invalid Option");
            }
        }
        else
        {
            throw new FieldValidationException("Value: '" + selectValue + "' is an invalid Option");
        }
    }

    public void createValue(CustomField field, Issue issue, Map<String, Option> cascadingOptions)
    {
        if (cascadingOptions != null)
        {
            Option parent = cascadingOptions.get(PARENT_KEY);

            if (parent != null)
            {
                customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(parent.getOptionId().toString()), null);

                Integer index = 1;
                while(parent != null)
                {
                    Option child = cascadingOptions.get(index.toString());
                    if (child != null)
                        customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(child.getOptionId().toString()), parent.getOptionId().toString());
                    parent = child;
                    index++;
                }
            }
        }
    }

    public String getChangelogValue(CustomField field, Map<String, Option> cascadingOptions)
    {
        if (cascadingOptions != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Parent values: ");
            Option parent = cascadingOptions.get(PARENT_KEY);
            sb.append(parent.getValue()).append("(").append(parent.getOptionId()).append(")");

            Integer childIndex = 1;
            Option child;
            while((child = cascadingOptions.get(childIndex.toString())) != null) {
                sb.append("Level ").append(childIndex).append(" values: ");
                sb.append(child.getValue()).append("(").append(child.getOptionId()).append(")");
                childIndex++;
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }
    
    public void updateValue(CustomField field, Issue issue, Map<String, Option> cascadingOptions)
    {
        // clear old stuff first
        customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, null);
        createValue(field, issue, cascadingOptions);
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

    @Override
    public int compare(@NotNull Map<String, Option> o1, @NotNull Map<String, Option> o2, FieldConfig fieldConfig) {
        return super.compare(o1, o2, fieldConfig);    //To change body of overridden methods use File | Settings | File Templates.
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
        final Options options = optionsManager.getOptions(config);
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
     */
    private Option trasformToOption(FieldConfig config, Object value) {
        if (value instanceof Option) {
            return (Option) value;
        }
        if (value instanceof String && EMPTY_VALUE_ID.equals(value)) {
            return optionsManager.createOption(config, EMPTY_VALUE_ID_LONG, EMPTY_VALUE_ID_LONG, EMPTY_VALUE);
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
            Option option1 = trasformToOption(config, relevantParams.getFirstValueForKey(Integer.toString(i)));
            if (option1 != null && !option1.toString().contains(":") && !option1.toString().equals(EMPTY_VALUE)) {
                log.debug("check option: [" + option1 + "]");
                if (!checkOption(customFieldId, option1, parentOption, errorCollectionToAddTo, config)) {
                    return;
                }
                parentOption = option1;
            } else {
                Collection<String> valueStrings = relevantParams.getAllValues();
                String[] splittedStrings = null;
                for (String s : valueStrings) {
                    splittedStrings = s.split(":");
                }
                if (splittedStrings == null) {
                    continue;
                }
                for (String splittedString : splittedStrings) {
                    // this part probably is useless, but for sure is not useful for "none" options
                    if (!splittedString.equals(EMPTY_VALUE_ID)) {
                        if (StringUtils.isNumeric(splittedString)) {
                            Long longOptionValue = new Long(splittedString);
                            final Option option = jqlSelectOptionsUtil.getOptionById(longOptionValue);
                            if (checkOption(errorCollectionToAddTo, config, parentOption, customFieldId, option))
                                return;
                            parentOption = option;
                        } else {
                            for (Option option : jqlSelectOptionsUtil.getOptions(config.getCustomField(), new QueryLiteral(new SingleValueOperand(splittedString),splittedString), true)) {
                                if (checkOption(errorCollectionToAddTo, config, parentOption, customFieldId, option))
                                    return;
                                parentOption = option;
                            }
                        }
                    }
                }

            }
        }
        log.debug("Post-Validate Error collection: [" + errorCollectionToAddTo.getErrors() + "]");
    }

    private boolean checkOption(ErrorCollection errorCollectionToAddTo, FieldConfig config, Option parentOption, String customFieldId, Option option) {
        if (option != null) {
            log.debug("check option: [" + option + "]");
            if (!checkOption(customFieldId, option, parentOption, errorCollectionToAddTo, config)) {
                return true;
            }
        }
        return false;
    }

    /**
     * add to the default file Config the specific ConfigItem for the multi level cascading select
     * custom field
     *
     * @see com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType#getConfigurationItemTypes()
     */
    @Override
    public List<FieldConfigItemType> getConfigurationItemTypes() {
        final List<FieldConfigItemType> configurationItemTypes = Lists.<FieldConfigItemType>newArrayList(JiraUtils.loadComponent(DefaultValueConfigItem.class));
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
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem) {

        Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

        map.put("mlcscftype", this);
        return map;
    }

    public Object getInitialKey() {
        return null;
    }

    private final Logger log = Logger.getLogger(MultiLevelCascadingSelectCFType.class);
}
