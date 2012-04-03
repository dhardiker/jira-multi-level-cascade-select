package com.sourcesense.jira.customfield.searcher;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.util.Map;
import java.util.TreeMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A custom field indexer for the multi level cascading select custom fields.
 * This class indexes the options saved for each multi level cascading entry in each multilevel cascading select custom field.
 *
 * @since v4.0
 * @author Alessandro Benedetti
 *
 */

/**
 * @author developer
 */
@NonInjectableComponent
public class ValueLeadMultiLevelCascadingSelectIndexer extends AbstractCustomFieldIndexer {

    private final Log log = LogFactory.getLog(ValueLeadMultiLevelCascadingSelectIndexer.class);

    public static final String RAW_VALUE_SUFFIX = "_raw";

    // /CLOVER:OFF

    public ValueLeadMultiLevelCascadingSelectIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField) {
        super(fieldVisibilityManager, notNull("customField", customField));

    }

    @Override
    public void addDocumentFieldsSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    @Override
    public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue) {
        addDocumentFields(doc, issue, Field.Index.NO);
    }

    /**
     * indexes the custom field params extracting the options from the custom field and building a Document (to use in Lucene Indexing)
     *
     * @param doc
     * @param issue
     * @param indexType
     */
    private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
        final Object value = customField.getValue(issue);
        if (value instanceof Map) {
            final Map<String,Option> customFieldParams = (Map<String, Option>) value;
            indexAllLevels(customFieldParams, doc, indexType);
            doc.add(new Field(getDocumentFieldId() + ":" + RAW_VALUE_SUFFIX, stringRep(customFieldParams), Field.Store.YES, indexType));
        }

    }

    private String stringRep(Map<String, Option> customFieldParams) {
        final TreeMap<String, Option> orderedKeys = new TreeMap<String, Option>(Maps.filterKeys(customFieldParams, Predicates.notNull()));
        orderedKeys.put("0", customFieldParams.get(null));
        return Joiner.on("-").join(orderedKeys.values());
    }

    /**
     * indexes all the info contained in all child-level of the custom field.
     * Remember that the Multi level cascading select allows you to create n levels of children from the parent node.
     *
     * @param customFieldParams
     * @param doc
     * @param indexType
     */
    private void indexAllLevels(final Map<String, Option> customFieldParams, final Document doc, final Field.Index indexType) {
        for (String level : customFieldParams.keySet()) {
            final Option currentOption = customFieldParams.get(level);
            if (currentOption != null) {
                final String indexFieldName = getDocumentFieldId() + (level == null ? "" : ":" + level);
                log.debug("Indexing :" + currentOption + "With ID=" + indexFieldName);
                addField(doc, indexFieldName, currentOption.getOptionId().toString(), indexType);
            }
        }
    }

    private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType) {
        doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
    }

    // /CLOVER:ON
}
