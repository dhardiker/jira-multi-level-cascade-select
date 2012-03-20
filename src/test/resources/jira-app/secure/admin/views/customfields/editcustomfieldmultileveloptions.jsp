<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.issuefields.customfields.edit.options'">
            <ww:param name="'value0'"><ww:property value="/customField/name" /></ww:param>
        </ww:text></title>
    <script language="JavaScript">
<!--
function loadUri(optionId)
{
    window.location = '<ww:property value="./selectedParentOptionUrlPreifx" escape="false" />' + optionId;
    return true;
}
//-->
    </script>
</head>

<body>
<page:applyDecorator name="jirapanel">
    <page:param name="title">
        <ww:text name="'admin.issuefields.customfields.edit.options'">
            <ww:param name="'value0'"><strong><ww:property value="/customField/name" /></strong></ww:param>
        </ww:text></page:param>
    <page:param name="width">100%</page:param>
    <page:param name="instructions">
    <p>
        <ww:if test="/selectedParentOption">
            <ww:text name="'admin.issuefields.customfields.reorder.parent'">
                <ww:param name="'value0'"><strong><ww:property value="/fieldConfig/name" /></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/customField/name" /></strong></ww:param>
                <ww:param name="'value2'"><strong><ww:property value="/selectedParentOption/value" /></strong></ww:param>
            </ww:text>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.issuefields.customfields.reorder'">
                <ww:param name="'value0'"><strong><ww:property value="/fieldConfig/name" /></strong></ww:param>
                <ww:param name="'value1'"><strong><ww:property value="/customField/name" /></strong></ww:param>
            </ww:text>
        </ww:else>
    </p>
    <p><ww:text name="'admin.issuefields.customfields.html.usage'"/></p>
    <ul class="optionslist">
        <li><a title="<ww:text name="'admin.issuefields.customfields.sort.alphabetically'"/>" href="<ww:property value="/selectedParentOptionUrlPrefix('sort')" /><ww:property value="/selectedParentOptionId" />"><ww:text name="'admin.issuefields.customfields.sort.alphabetically'"/></a></li>
        <li><a title="<ww:text name="'admin.issuefields.customfields.view.custom.field.configuration'"/>" href="ConfigureCustomField!default.jspa?customFieldId=<ww:property value="/customField/idAsLong"/>"><ww:text name="'admin.issuefields.customfields.view.custom.field.configuration'"/></a></li>
    </ul>
<ww:if test="/cascadingSelect == true">
<p>
    <ww:text name="'admin.issuefields.customfields.choose.parent'"/>:
    <select name="<ww:property value="./customFieldHelper/id" />" onchange="return loadUri(this.value);">
        <option value=""><ww:text name="'admin.issuefields.customfields.edit.parent.list'"/></option>


        <ww:if test="/selectedParentOptionId">
            <ww:if test="/selectedParentOption/parentOption">
                <ww:iterator value="/selectedParentOption/parentOption/childOptions"
                                  status="'rowStatus'">
                    <option value="<ww:property value="./optionId" />"
                            <ww:if test="./optionId == /selectedParentOptionId">selected</ww:if>>
                        <ww:property value="./value"/>
                    </option>
                </ww:iterator>
            </ww:if>
            <ww:else>
        <ww:iterator value="/options" status="'rowStatus'">
            <option value="<ww:property value="./optionId" />" <ww:if test="./optionId == /selectedParentOptionId">selected</ww:if>>
                <ww:property value="./value" />
            </option>
        </ww:iterator>
            </ww:else>
        </ww:if>
        <ww:else>
            <ww:iterator value="/options" status="'rowStatus'">
                <option value="<ww:property value="./optionId" />"
                        <ww:if test="./optionId == /selectedParentOptionId">selected</ww:if>>
                    <ww:property value="./value"/>
                </option>
            </ww:iterator>
        </ww:else>

    </select>
</p>
</ww:if>


</page:param>


<ww:if test="/displayOptions && /displayOptions/empty == false">

    <form name="configureOption" action="ConfigureCustomFieldOptions.jspa" method="post">
    <table class="aui aui-table-rowhover">
        <thead>
            <tr>
                <th width="1%">
                    <ww:text name="'admin.issuefields.customfields.position'"/>
                </th>
                <th>
                    <ww:text name="'admin.issuefields.customfields.option'"/>
                </th>
                <ww:if test="/displayOptions/size > 1">
                    <th width="10%">
                        <ww:text name="'admin.issuefields.customfields.order'"/>
                    </th>
                    <th width="10%" class="nowrap">
                        <ww:text name="'admin.issuefields.customfields.move.to.position'"/>
                    </th>
                </ww:if>
                <th width="10%">
                    <ww:text name="'common.words.operations'"/>
                </th>
            </tr>
        </thead>
        <tbody>
        <ww:iterator value="/displayOptions" status="'status'">
            <tr class="<ww:if test="/hlOptions/contains(./value) == true">rowHighlighted</ww:if>">
                <td>
                    <ww:property value="@status/count" />.
                </td>
                <ui:textfield name="/newLabelTextBoxName(./optionId)" label="Update label" theme="'single'"
                              value="./value" size="'30'"/>
                    <%--
                                <td class="normal">
                                <webwork:if test="/cascadingSelect == true">
                                    <a title="Edit children options for <webwork:property value="./value" />" href="<webwork:property value="/selectedParentOptionUrlPreifx" escape="false" /><webwork:property value="./optionId" />">
                                </webwork:if>
                                    <b><webwork:property value="./value" /></b>
                                    <span class="smallgrey"><webwork:if test="/defaultValue(./optionId/toString()) == true">(<webwork:text name="'admin.common.words.default'"/>)</webwork:if></span>
                                <webwork:if test="/cascadingSelect == true && !/selectedParentOptionId"></a></webwork:if>
                                </td>
                    --%>
                <ww:if test="/displayOptions/size > 1">
                    <td valign=top align=center nowrap>
                        <ww:if test="@status/first != true">
                        <a id="moveToFirst_<ww:property value="./optionId" />" href="<ww:property value="/doActionUrl(.,'moveToFirst')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_first.gif" border=0 width=16 height=16 title="<ww:text name="'admin.issuefields.customfields.move.to.first.position'"/>"></a>
                        <a id="moveUp_<ww:property value="./optionId" />" href="<ww:property value="/doActionUrl(.,'moveUp')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" border=0 width=16 height=16 title="<ww:text name="'admin.issuefields.customfields.move.this.option.up'"/>"></a></ww:if>
                        <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=20 height=16></ww:else>
                        <ww:if test="@status/last != true">
                        <a id="moveDown_<ww:property value="./optionId" />" href="<ww:property value="/doActionUrl(.,'moveDown')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" border=0 width=16 height=16 title="<ww:text name="'admin.issuefields.customfields.move.this.option.down'"/>"></a>
                        <a id="moveToLast_<ww:property value="./optionId" />" href="<ww:property value="/doActionUrl(.,'moveToLast')" escape="false" />"><img src="<%= request.getContextPath() %>/images/icons/arrow_last.gif" border=0 width=16 height=16 title="<ww:text name="'admin.issuefields.customfields.move.this.option.to.last'"/>"></a></ww:if>
                        <ww:else><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=13 height=14><image src="<%= request.getContextPath() %>/images/border/spacer.gif" border=0 width=20 height=16></ww:else>
                    </td>

                    <ui:textfield name="/newPositionTextBoxName(./optionId)" label="text('admin.issuefields.customfields.new.option.position')" theme="'single'" value="/newPositionValue(./optionId)" size="'2'">
                        <ui:param name="'class'">fullyCentered</ui:param>
                   </ui:textfield>
                </ww:if>
                <td>
                    <ul class="operations-list">
                    <ww:if test="/cascadingSelect == true">
                        <li><a title="<ww:text name="'admin.issuefields.customfields.edit.children.options'"><ww:param name="'value0'"><ww:property value="./value" /></ww:param></ww:text>"href="<ww:property value="/selectedParentOptionUrlPreifx" escape="false" /><ww:property value="./optionId" />"><ww:text name="'common.words.configure'"/></a></li>
                    </ww:if>
                    <ww:if test="/defaultValue(./optionId/toString()) != true">
                        <li><a id="del_<ww:property value="./value"/>" href="<ww:property value="/doActionUrl(.,'remove')" escape="false" />"><ww:text name="'common.words.delete'"/></a></li>
                    </ww:if>
                    <ww:else>&nbsp;</ww:else>
                    </ul>
                </td>
            </tr>
        </ww:iterator>
            <tr class="rowHeader" align="center">
                <td>&nbsp;
                    <input type="hidden" name="id" value="<ww:property value="/id" />">
                    <input type="hidden" name="fieldConfigId" value="<ww:property value="/fieldConfigId" />">
                    <input type="hidden" name="selectedParentOptionId" value="<ww:property value="/selectedParentOptionId" />">
                </td>
                <td>
                    <input class="aui-button"  type="submit" name="saveLabel" value="<ww:text name="'common.words.update'"/>">
                </td>
                <td>&nbsp;</td>
                <ww:if test="./displayOptions/size > 1">
                    <td>
                        <input class="aui-button" type="submit" name="moveOptionsToPosition" value="<ww:text name="'common.forms.move'"/>">
                    </td>
                </ww:if>
                <td></td>
            </tr>
        </tbody>
    </table>
    </form>

</ww:if>
<ww:else>
    <aui:component template="auimessage.jsp" theme="'aui'">
        <aui:param name="'messageType'">info</aui:param>
        <aui:param name="'messageHtml'">
            <p><ww:text name="'admin.issuefields.customfields.currently.no.options'"/></p>
        </aui:param>
    </aui:component>
</ww:else>


    <page:applyDecorator name="jiraform">
        <page:param name="action">EditCustomFieldMultiLevelOptions!add.jspa</page:param>
        <page:param name="submitId">add_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.add'"/></page:param>
        <page:param name="width">100%</page:param>
        <page:param name="title"><ww:text name="'admin.issuefields.customfields.add.new.option'"/></page:param>
        <page:param name="buttons"><input class="aui-button" type="button" value="Done" onclick="location.href='ConfigureCustomField!default.jspa?customFieldId=<ww:property value="/customField/idAsLong" />'"></page:param>

        <ui:textfield label="text('admin.issuefields.customfields.add.value')" name="'addValue'" />
        <ui:component name="'fieldConfigId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'selectedParentOptionId'" template="hidden.jsp" theme="'single'"  />
        <ui:component name="'addSelectValue'" value="true" template="hidden.jsp" theme="'single'"  />
    </page:applyDecorator>
</page:applyDecorator>

</body>
</html>
