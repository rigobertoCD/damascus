<#include "./valuables.ftl">
<#assign createPath = "${entityWebResourcesPath}/view.jsp">
<#assign skipTemplate = !generateWeb>
<%@ include file="/${snakecaseModel}/init.jsp"%>
<%
	String iconChecked = "checked";
	String iconUnchecked = "unchecked";
	SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatVal);
	SimpleDateFormat dateTimeFormat = new SimpleDateFormat(datetimeFormatVal);

	PortletURL navigationPortletURL = renderResponse.createRenderURL();
	PortletURL portletURL = PortletURLUtil.clone(navigationPortletURL, liferayPortletResponse);

	String keywords = ParamUtil.getString(request, DisplayTerms.KEYWORDS);
	int cur = ParamUtil.getInteger(request, SearchContainer.DEFAULT_CUR_PARAM);
	int delta = ParamUtil.getInteger(request, SearchContainer.DEFAULT_DELTA_PARAM);
	String orderByCol = ParamUtil.getString(request, SearchContainer.DEFAULT_ORDER_BY_COL_PARAM, "${primaryKeyParam}");
	String orderByType = ParamUtil.getString(request, SearchContainer.DEFAULT_ORDER_BY_TYPE_PARAM, "asc");
	String[] orderColumns = new String[] {
	"${primaryKeyParam}"
<#list application.fields as field >
	<#if field.primary?? && field.primary == false >
    ,"${field.name}"
	</#if>
</#list>
	};

	navigationPortletURL.setParameter(DisplayTerms.KEYWORDS, keywords);
	navigationPortletURL.setParameter(SearchContainer.DEFAULT_CUR_PARAM, String.valueOf(cur));
	navigationPortletURL.setParameter("mvcRenderCommandName", "/${lowercaseModel}/view");
	navigationPortletURL.setParameter(SearchContainer.DEFAULT_ORDER_BY_COL_PARAM, orderByCol);
	navigationPortletURL.setParameter(SearchContainer.DEFAULT_ORDER_BY_TYPE_PARAM, orderByType);

	${capFirstModel}ViewHelper ${uncapFirstModel}ViewHelper = (${capFirstModel}ViewHelper) request
			.getAttribute(${capFirstModel}WebKeys.${uppercaseModel}_VIEW_HELPER);
%>

<portlet:renderURL var="${lowercaseModel}AddURL">
	<portlet:param name="mvcRenderCommandName" value="/${lowercaseModel}/crud" />
	<portlet:param name="<%=Constants.CMD%>" value="<%=Constants.ADD%>" />
	<portlet:param name="redirect" value="<%=portletURL.toString()%>" />
</portlet:renderURL>
    <div class="container-fluid-1280 icons-container lfr-meta-actions">
		<div class="add-record-button-container pull-left">
			<c:if test="<%= ${capFirstModel}ResourcePermissionChecker.contains(permissionChecker, themeDisplay.getScopeGroupId(), ActionKeys.ADD_ENTRY) %>">
            <aui:button href="<%=${lowercaseModel}AddURL%>" cssClass="btn btn-default"
                icon="icon-plus" value="add-${lowercaseModel}" />
			</c:if>
        </div>
		<div class="lfr-icon-actions">
			<c:if test="<%= ${capFirstModel}ResourcePermissionChecker.contains(permissionChecker, themeDisplay.getScopeGroupId(), ActionKeys.PERMISSIONS) %>">
				<liferay-security:permissionsURL
					modelResource="${packageName}"
					modelResourceDescription="<%= HtmlUtil.escape(themeDisplay.getScopeGroupName()) %>"
					resourcePrimKey="<%= String.valueOf(themeDisplay.getScopeGroupId()) %>"
					var="modelPermissionsURL"
				/>
				<liferay-ui:icon
					cssClass="lfr-icon-action pull-right"
					icon="cog"
					label="<%= true %>"
					markupView="lexicon"
					message="permissions"
					url="<%= modelPermissionsURL %>"
				/>
			</c:if>
		</div>
    </div>

<aui:nav-bar cssClass="collapse-basic-search" markupView="lexicon">
	<aui:form action="<%=portletURL.toString()%>" name="searchFm">
		<aui:nav-bar-search>
			<liferay-ui:input-search markupView="lexicon" />
		</aui:nav-bar-search>
	</aui:form>
</aui:nav-bar>

<liferay-frontend:management-bar includeCheckBox="<%=true%>"
	searchContainerId="entryList">

	<liferay-frontend:management-bar-filters>
		<liferay-frontend:management-bar-sort orderByCol="<%=orderByCol%>"
			orderByType="<%=orderByType%>" orderColumns='<%=orderColumns%>'
			portletURL="<%=navigationPortletURL%>" />
	</liferay-frontend:management-bar-filters>

	<liferay-frontend:management-bar-action-buttons>
		<liferay-frontend:management-bar-button
			href='<%="javascript:" + renderResponse.getNamespace() + "deleteEntries();"%>'
			icon='<%=TrashUtil.isTrashEnabled(scopeGroupId) ? "trash" : "times"%>'
			label='<%=TrashUtil.isTrashEnabled(scopeGroupId) ? "recycle-bin" : "delete"%>' />
	</liferay-frontend:management-bar-action-buttons>

</liferay-frontend:management-bar>

<div class="container-fluid-1280"
	id="<portlet:namespace />formContainer">

	<aui:form action="<%=navigationPortletURL.toString()%>" method="get"
		name="fm">
		<aui:input name="<%=Constants.CMD%>" type="hidden" />
		<aui:input name="redirect" type="hidden"
			value="<%=navigationPortletURL.toString()%>" />
		<aui:input name="deleteEntryIds" type="hidden" />

		<liferay-ui:success key="${lowercaseModel}-added-successfully"
							message="${lowercaseModel}-added-successfully" />
        <liferay-ui:success key="${lowercaseModel}-updated-successfully"
                            message="${lowercaseModel}-updated-successfully" />
        <liferay-ui:success key="${lowercaseModel}-deleted-successfully"
                            message="${lowercaseModel}-deleted-successfully" />

		<liferay-ui:error exception="<%=PortletException.class%>"
			message="there-was-an-unexpected-error.-please-refresh-the-current-page" />

		<liferay-ui:search-container id="entryList" deltaConfigurable="true"
			rowChecker="<%=new EmptyOnClickRowChecker(renderResponse)%>"
			searchContainer='<%=new SearchContainer(renderRequest,
							PortletURLUtil.clone(navigationPortletURL, liferayPortletResponse), null,
							"no-recodes-were-found")%>'>

			<liferay-ui:search-container-results>
				<%@ include file="/${snakecaseModel}/search_results.jspf"%>
			</liferay-ui:search-container-results>

			<liferay-ui:search-container-row
				className="${packageName}.model.${capFirstModel}"
				escapedModel="<%= true %>" keyProperty="${primaryKeyParam}"
				rowIdProperty="${primaryKeyParam}" modelVar="${uncapFirstModel}">

			<#-- ---------------- -->
			<#-- field loop start -->
			<#-- ---------------- -->
			<#list application.fields as field >

				<#-- ---------------- -->
				<#--     Long         -->
				<#--     Varchar      -->
				<#--     Boolean      -->
				<#--     Double       -->
				<#-- Document Library -->
				<#--     Integer      -->
				<#-- ---------------- -->
					<#if
					field.type?string == "com.liferay.damascus.cli.json.fields.Long"     		||
					field.type?string == "com.liferay.damascus.cli.json.fields.Varchar"  		||
					field.type?string == "com.liferay.damascus.cli.json.fields.Boolean"  		||
					field.type?string == "com.liferay.damascus.cli.json.fields.Double"   		||
					field.type?string == "com.liferay.damascus.cli.json.fields.DocumentLibrary" ||
					field.type?string == "com.liferay.damascus.cli.json.fields.Integer"
					>
				<liferay-ui:search-container-column-text name='<%= FriendlyURLNormalizerUtil.normalize("${field.name}") %>'
														 property="${field.name}" orderable="true" orderableProperty="${field.name}"
														 align="left" />
					</#if>
				<#-- ---------------- -->
				<#--     Date         -->
				<#--     DateTime     -->
				<#-- ---------------- -->
					<#if
					field.type?string == "com.liferay.damascus.cli.json.fields.Date"     ||
					field.type?string == "com.liferay.damascus.cli.json.fields.DateTime"
					>
				<liferay-ui:search-container-column-text name='<%= FriendlyURLNormalizerUtil.normalize("${field.name}") %>'
														 value="<%= dateFormat.format(${uncapFirstModel}.get${field.name?cap_first}()) %>"
														 orderable="true" orderableProperty="${field.name}" align="left" />
					</#if>

				<#-- ---------------- -->
				<#--     RichText     -->
				<#--       Text       -->
				<#-- ---------------- -->
					<#if
					field.type?string == "com.liferay.damascus.cli.json.fields.RichText" ||
					field.type?string == "com.liferay.damascus.cli.json.fields.Text"
					>
				<liferay-ui:search-container-column-text name='<%= FriendlyURLNormalizerUtil.normalize("${field.name}") %>'
														 align="center">
					<%
					String ${field.name}Icon = iconUnchecked;
					String ${field.name} = ${uncapFirstModel}.get${field.name?cap_first}();
					if (!${field.name}.equals("")) {
						${field.name}Icon= iconChecked;
					}
					%>
					<liferay-ui:icon image="<%= ${field.name}Icon %>" />
				</liferay-ui:search-container-column-text>
					</#if>

			</#list>
			<#-- ---------------- -->
			<#-- field loop ends  -->
			<#-- ---------------- -->

				<liferay-ui:search-container-column-jsp align="right"
					path="/${snakecaseModel}/edit_actions.jsp" />

			</liferay-ui:search-container-row>
			<liferay-ui:search-iterator displayStyle="list" markupView="lexicon" />
		</liferay-ui:search-container>
	</aui:form>
</div>

<aui:script>
	function <portlet:namespace />deleteEntries() {
		if (<%=TrashUtil.isTrashEnabled(scopeGroupId)%> || confirm('<%=UnicodeLanguageUtil.get(request, "are-you-sure-you-want-to-delete-the-selected-entries")%>')) {
			var form = AUI.$(document.<portlet:namespace />fm);

			form.attr('method', 'post');
			form.fm('<%=Constants.CMD%>').val('<%=TrashUtil.isTrashEnabled(scopeGroupId) ? Constants.MOVE_TO_TRASH : Constants.DELETE%>');
			form.fm('deleteEntryIds').val(Liferay.Util.listCheckedExcept(form, '<portlet:namespace />allRowIds'));

			submitForm(form, '<portlet:actionURL name="/${lowercaseModel}/crud" />');
		}
	}
</aui:script>
